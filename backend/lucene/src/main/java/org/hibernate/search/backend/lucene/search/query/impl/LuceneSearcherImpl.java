/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.query.impl;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.search.backend.lucene.logging.impl.Log;
import org.hibernate.search.backend.lucene.search.extraction.impl.LuceneCollectors;
import org.hibernate.search.backend.lucene.search.extraction.impl.LuceneCollectorsBuilder;
import org.hibernate.search.backend.lucene.search.extraction.impl.LuceneResult;
import org.hibernate.search.backend.lucene.search.extraction.impl.ReusableDocumentStoredFieldVisitor;
import org.hibernate.search.backend.lucene.search.impl.LuceneQueries;
import org.hibernate.search.backend.lucene.search.projection.impl.LuceneSearchProjection;
import org.hibernate.search.backend.lucene.search.projection.impl.SearchProjectionExtractContext;
import org.hibernate.search.backend.lucene.util.impl.LuceneFields;
import org.hibernate.search.backend.lucene.work.impl.LuceneSearcher;
import org.hibernate.search.engine.search.loading.spi.ProjectionHitMapper;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.QueryBitSetProducer;
import org.apache.lucene.search.join.ToChildBlockJoinQuery;

class LuceneSearcherImpl<H> implements LuceneSearcher<LuceneLoadableSearchResult<H>> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final Set<String> ID_FIELD_SET = Collections.singleton( LuceneFields.idFieldName() );

	private final LuceneSearchQueryRequestContext requestContext;

	private final ReusableDocumentStoredFieldVisitor storedFieldVisitor;
	private final LuceneSearchProjection<?, H> rootProjection;

	LuceneSearcherImpl(LuceneSearchQueryRequestContext requestContext,
			ReusableDocumentStoredFieldVisitor storedFieldVisitor,
			LuceneSearchProjection<?, H> rootProjection) {
		this.requestContext = requestContext;
		this.storedFieldVisitor = storedFieldVisitor;
		this.rootProjection = rootProjection;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "luceneQuery=" ).append( requestContext.getLuceneQuery() )
				.append( ", luceneSort=" ).append( requestContext.getLuceneSort() )
				.append( "]" );
		return sb.toString();
	}

	@Override
	public LuceneLoadableSearchResult<H> search(IndexSearcher indexSearcher, int offset, Integer limit)
			throws IOException {
		LuceneCollectors luceneCollectors = buildCollectors( indexSearcher, offset, limit );

		luceneCollectors.collect( indexSearcher, requestContext.getLuceneQuery(), offset, limit );

		LuceneSearchQueryExtractContext extractContext = requestContext.createExtractContext(
				indexSearcher, luceneCollectors
		);

		List<Object> extractedData = extractHits( extractContext );

		return new LuceneLoadableSearchResult<>(
				extractContext, rootProjection,
				luceneCollectors.getTotalHits(), extractedData
		);
	}

	@Override
	public int count(IndexSearcher indexSearcher) throws IOException {
		// TODO HSEARCH-3352 implement timeout handling... somehow?
		//  We may have to use search() instead of count() with a TotalHitCountCollector wrapped with the timeout limiting one
		return indexSearcher.count( requestContext.getLuceneQuery() );
	}

	@Override
	public Explanation explain(IndexSearcher indexSearcher, int luceneDocId) throws IOException {
		return indexSearcher.explain( requestContext.getLuceneQuery(), luceneDocId );
	}

	@Override
	public Query getLuceneQueryForExceptions() {
		return requestContext.getLuceneQuery();
	}

	private LuceneCollectors buildCollectors(IndexSearcher indexSearcher, int offset, Integer limit) {
		// TODO HSEARCH-3323 this is very naive for now, we will probably need to implement some scrolling in the collector
		//  as it is done in Search 5.
		//  Note that Lucene initializes data structures of this size so setting it to a large value consumes memory.
		int maxDocs = getMaxDocs( indexSearcher.getIndexReader(), offset, limit );

		// TODO HSEARCH-3352 implement timeout handling by wrapping the collector with the timeout limiting one
		LuceneCollectorsBuilder luceneCollectorsBuilder = new LuceneCollectorsBuilder( requestContext.getLuceneSort(), maxDocs );
		rootProjection.contributeCollectors( luceneCollectorsBuilder );
		return luceneCollectorsBuilder.build();
	}

	private int getMaxDocs(IndexReader reader, int offset, Integer limit) {
		if ( limit == null ) {
			return reader.maxDoc();
		}
		else if ( limit.equals( 0 ) ) {
			return 0;
		}
		else {
			return Math.min( offset + limit, reader.maxDoc() );
		}
	}

	private List<Object> extractHits(LuceneSearchQueryExtractContext extractContext) throws IOException {
		ProjectionHitMapper<?, ?> projectionHitMapper = extractContext.getProjectionHitMapper();
		IndexSearcher indexSearcher = extractContext.getIndexSearcher();

		TopDocs topDocs = extractContext.getTopDocs();
		if ( topDocs == null ) {
			return Collections.emptyList();
		}

		List<Object> extractedData = new ArrayList<>( topDocs.scoreDocs.length );
		Map<Integer, Set<Integer>> nestedDocs = fetchNestedDocs( indexSearcher, topDocs.scoreDocs, extractContext );

		SearchProjectionExtractContext projectionExtractContext = extractContext.createProjectionExtractContext( nestedDocs );

		for ( ScoreDoc hit : topDocs.scoreDocs ) {
			// add root object contribution
			indexSearcher.doc( hit.doc, storedFieldVisitor );
			if ( nestedDocs.containsKey( hit.doc ) ) {
				for ( Integer child : nestedDocs.get( hit.doc ) ) {
					indexSearcher.doc( child, storedFieldVisitor );
				}
			}

			Document document = storedFieldVisitor.getDocumentAndReset();
			LuceneResult luceneResult = new LuceneResult( document, hit.doc, hit.score );

			extractedData.add( rootProjection.extract( projectionHitMapper, luceneResult, projectionExtractContext ) );
		}

		return extractedData;
	}

	private Map<Integer, Set<Integer>> fetchNestedDocs(IndexSearcher indexSearcher, ScoreDoc[] scoreDocs,
			LuceneSearchQueryExtractContext extractContext)
			throws IOException {
		// if the projection does not need any nested object skip their fetching
		if ( storedFieldVisitor.getNestedDocumentPaths().isEmpty() ) {
			return new HashMap<>();
		}

		// TODO HSEARCH-3657 this could be avoided
		Map<String, Integer> parentIds = new HashMap<>();
		for ( ScoreDoc hit : scoreDocs ) {
			Document doc = indexSearcher.doc( hit.doc, ID_FIELD_SET );
			String parentId = doc.getField( LuceneFields.idFieldName() ).stringValue();
			if ( parentId == null ) {
				continue;
			}
			parentIds.put( parentId, hit.doc );
		}

		Map<String, Set<Integer>> stringSetMap = fetchChildren(
				indexSearcher, storedFieldVisitor.getNestedDocumentPaths(),
				extractContext.getCollectorsForNestedDocuments()
		);
		HashMap<Integer, Set<Integer>> result = new HashMap<>();
		for ( Map.Entry<String, Set<Integer>> entry : stringSetMap.entrySet() ) {
			result.put( parentIds.get( entry.getKey() ), entry.getValue() );
		}
		return result;
	}

	private Map<String, Set<Integer>> fetchChildren(IndexSearcher indexSearcher, Set<String> nestedDocumentPaths,
			Collection<Collector> collectorsForChildren) {
		BooleanQuery booleanQuery = getChildQuery( nestedDocumentPaths );

		try {
			ArrayList<Collector> luceneCollectors = new ArrayList<>();
			LuceneChildrenCollector childrenCollector = new LuceneChildrenCollector();
			luceneCollectors.add( childrenCollector );
			luceneCollectors.addAll( collectorsForChildren );

			indexSearcher.search( booleanQuery, MultiCollector.wrap( luceneCollectors ) );
			return childrenCollector.getChildren();
		}
		catch (IOException e) {
			throw log.errorFetchingNestedDocuments( booleanQuery, e );
		}
	}

	private BooleanQuery getChildQuery(Set<String> nestedDocumentPaths) {
		QueryBitSetProducer parentsFilter = new QueryBitSetProducer( LuceneQueries.mainDocumentQuery() );
		ToChildBlockJoinQuery parentQuery = new ToChildBlockJoinQuery( requestContext.getLuceneQuery(), parentsFilter );

		return new BooleanQuery.Builder()
				.add( parentQuery, BooleanClause.Occur.MUST )
				.add( createNestedDocumentPathSubQuery( nestedDocumentPaths ), BooleanClause.Occur.FILTER )
				.add( LuceneQueries.childDocumentQuery(), BooleanClause.Occur.FILTER )
				.build();
	}

	private BooleanQuery createNestedDocumentPathSubQuery(Set<String> nestedDocumentPaths) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for ( String nestedDocumentPath : nestedDocumentPaths ) {
			builder.add( LuceneQueries.nestedDocumentPathQuery( nestedDocumentPath ), BooleanClause.Occur.SHOULD );
		}
		builder.setMinimumNumberShouldMatch( 1 );
		return builder.build();
	}
}