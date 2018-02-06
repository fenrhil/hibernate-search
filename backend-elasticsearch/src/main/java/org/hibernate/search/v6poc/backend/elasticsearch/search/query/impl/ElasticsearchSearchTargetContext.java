/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.query.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexModel;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.ElasticsearchBackend;
import org.hibernate.search.v6poc.backend.elasticsearch.search.impl.ElasticsearchSearchQueryElementCollector;
import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.ElasticsearchSearchPredicateCollector;
import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.SearchPredicateFactoryImpl;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;
import org.hibernate.search.v6poc.search.predicate.spi.SearchPredicateFactory;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryFactory;

public class ElasticsearchSearchTargetContext
		implements SearchTargetContext<ElasticsearchSearchQueryElementCollector> {

	private final SearchPredicateFactory<ElasticsearchSearchPredicateCollector> searchPredicateFactory;
	private final SearchQueryFactory<ElasticsearchSearchQueryElementCollector> searchQueryFactory;

	public ElasticsearchSearchTargetContext(ElasticsearchBackend backend, Set<ElasticsearchIndexModel> indexModels) {
		this.searchPredicateFactory = new SearchPredicateFactoryImpl( indexModels );
		Set<String> indexNames = indexModels.stream()
				.map( ElasticsearchIndexModel::getIndexName )
				.collect( Collectors.toSet() );
		this.searchQueryFactory = new SearchQueryFactoryImpl( backend, indexModels, indexNames );
	}

	@Override
	public SearchPredicateFactory<ElasticsearchSearchPredicateCollector> getSearchPredicateFactory() {
		return searchPredicateFactory;
	}

	@Override
	public SearchQueryFactory<ElasticsearchSearchQueryElementCollector> getSearchQueryFactory() {
		return searchQueryFactory;
	}
}
