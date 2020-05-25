/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.search.query.impl;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.persistence.QueryTimeoutException;

import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.graph.RootGraph;
import org.hibernate.jpa.QueryHints;
import org.hibernate.query.Query;
import org.hibernate.query.ResultListTransformer;
import org.hibernate.query.TupleTransformer;
import org.hibernate.query.internal.AbstractProducedQuery;
import org.hibernate.query.internal.ParameterMetadataImpl;
import org.hibernate.query.spi.ParameterMetadataImplementor;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.spi.SearchQueryImplementor;
import org.hibernate.search.mapper.orm.logging.impl.Log;
import org.hibernate.search.mapper.orm.loading.impl.EntityGraphHint;
import org.hibernate.search.mapper.orm.loading.impl.MutableEntityLoadingOptions;
import org.hibernate.search.mapper.orm.search.query.spi.HibernateOrmSearchScrollableResultsAdapter;
import org.hibernate.search.util.common.SearchTimeoutException;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

public final class HibernateOrmSearchQueryAdapter<R> extends AbstractProducedQuery<R> {

	public static <R> HibernateOrmSearchQueryAdapter<R> create(SearchQuery<R> query) {
		return query.extension( HibernateOrmSearchQueryAdapterExtension.get() );
	}

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final SearchQueryImplementor<R> delegate;

	private final SessionImplementor sessionImplementor;
	private final MutableEntityLoadingOptions loadingOptions;

	HibernateOrmSearchQueryAdapter(SearchQueryImplementor<R> delegate, SessionImplementor sessionImplementor,
			MutableEntityLoadingOptions loadingOptions) {
		super( sessionImplementor, new ParameterMetadataImpl( null, null ) );
		this.delegate = delegate;
		this.sessionImplementor = sessionImplementor;
		this.loadingOptions = loadingOptions;
	}

	@Override
	public String toString() {
		return "HibernateOrmSearchQueryAdapter(" + getQueryString() + ")";
	}

	//-------------------------------------------------------------
	// Supported ORM/JPA query methods
	//-------------------------------------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> type) {
		if ( type.equals( SearchQuery.class ) ) {
			return (T) delegate;
		}
		else {
			return super.unwrap( type );
		}
	}

	@Override
	public List<R> list() {
		try {
			return super.list();
		}
		catch (SearchTimeoutException e) {
			throw new QueryTimeoutException( e );
		}
	}

	@Override
	public String getQueryString() {
		return delegate.queryString();
	}

	@Override
	@SuppressWarnings("deprecation")
	public HibernateOrmSearchQueryAdapter<R> setHint(String hintName, Object value) {
		switch ( hintName ) {
			case QueryHints.SPEC_HINT_TIMEOUT:
				delegate.failAfter( hintValueToLong( value ), TimeUnit.MILLISECONDS );
				break;
			case QueryHints.HINT_TIMEOUT:
				setTimeout( hintValueToInteger( value ) );
				break;
			case "javax.persistence.fetchgraph":
				applyGraph( hintValueToEntityGraph( value ), GraphSemantic.FETCH );
				break;
			case "javax.persistence.loadgraph":
				applyGraph( hintValueToEntityGraph( value ), GraphSemantic.LOAD );
				break;
			default:
				handleUnrecognizedHint( hintName, value );
				break;
		}
		return this;
	}

	@Override
	public HibernateOrmSearchQueryAdapter<R> setTimeout(int timeout) {
		delegate.failAfter( timeout, TimeUnit.SECONDS );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Query<R> applyGraph(RootGraph graph, GraphSemantic semantic) {
		loadingOptions.entityGraphHint( new EntityGraphHint<>( graph, semantic ), true );
		return this;
	}

	@Override
	public ScrollableResultsImplementor scroll() {
		return scroll( ScrollMode.FORWARD_ONLY );
	}

	@Override
	public ScrollableResultsImplementor scroll(ScrollMode scrollMode) {
		if ( !ScrollMode.FORWARD_ONLY.equals( scrollMode ) ) {
			throw log.canOnlyUseScrollWithScrollModeForwardsOnly( scrollMode );
		}

		int chunkSize = loadingOptions.fetchSize();
		return new HibernateOrmSearchScrollableResultsAdapter<>( delegate.scroll( chunkSize ), getMaxResults() );
	}

	@Override
	public SharedSessionContractImplementor getSession() {
		return sessionImplementor;
	}

	@Override
	protected boolean isNativeQuery() {
		return false;
	}

	@Override
	protected List<R> doList() {
		// Do not use getMaxRows()/getFirstRow() directly, they return weird values to comply with the JPA spec
		Integer limit = getQueryOptions().getLimit().getMaxRows();
		Integer offset = getQueryOptions().getLimit().getFirstRow();
		return delegate.fetchHits( offset, limit );
	}

	@Override
	protected void beforeQuery() {
		super.beforeQuery();

		Integer queryFetchSize = getQueryOptions().getFetchSize();
		if ( queryFetchSize != null ) {
			loadingOptions.fetchSize( queryFetchSize );
		}
		Integer queryTimeout = getQueryOptions().getTimeout();
		if ( queryTimeout != null ) {
			delegate.failAfter( queryTimeout, TimeUnit.SECONDS );
		}
	}

	//-------------------------------------------------------------
	// Unsupported ORM/JPA query methods
	//-------------------------------------------------------------

	@Override
	public Map<String, Object> getHints() {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public ParameterMetadataImplementor getParameterMetadata() {
		throw parametersNoSupported();
	}

	@Override
	public QueryParameterBindings getParameterBindings() {
		// parameters not supported in Hibernate Search queries
		return QueryParameterBindings.NO_PARAM_BINDINGS;
	}

	@Override
	protected QueryParameterBindings getQueryParameterBindings() {
		// parameters not supported in Hibernate Search queries
		return QueryParameterBindings.NO_PARAM_BINDINGS;
	}

	@Override
	public HibernateOrmSearchQueryAdapter<R> setParameterList(String name, Object[] values) {
		throw parametersNoSupported();
	}

	@Override
	public Query<R> setParameterList(String s, Collection collection, Class aClass) {
		throw parametersNoSupported();
	}

	@Override
	public Query<R> setParameterList(int i, Collection collection, Class aClass) {
		throw parametersNoSupported();
	}

	private UnsupportedOperationException parametersNoSupported() {
		return new UnsupportedOperationException( "parameters not supported in Hibernate Search queries" );
	}

	@Override
	@Deprecated
	public HibernateOrmSearchQueryAdapter<R> setResultTransformer(org.hibernate.transform.ResultTransformer transformer) {
		super.setResultTransformer( transformer );
		throw resultOrTupleTransformerNotImplemented();
	}

	@Override
	public Query<R> setTupleTransformer(TupleTransformer<R> tupleTransformer) {
		throw resultOrTupleTransformerNotImplemented();
	}

	@Override
	public Query<R> setResultListTransformer(ResultListTransformer resultListTransformer) {
		throw resultOrTupleTransformerNotImplemented();
	}

	private UnsupportedOperationException resultOrTupleTransformerNotImplemented() {
		return new UnsupportedOperationException( "Result transformers are not supported in Hibernate Search queries" );
	}

	@Override
	protected int doExecuteUpdate() {
		throw new UnsupportedOperationException( "executeUpdate is not supported in Hibernate Search queries" );
	}

	@Override
	public HibernateOrmSearchQueryAdapter<R> setLockMode(String alias, LockMode lockMode) {
		throw new UnsupportedOperationException( "Lock options are not implemented in Hibernate Search queries" );
	}

	private static long hintValueToLong(Object value) {
		if ( value instanceof Number ) {
			return ( (Number) value ).longValue();
		}
		else {
			return Long.parseLong( String.valueOf( value ) );
		}
	}

	private static int hintValueToInteger(Object value) {
		if ( value instanceof Number ) {
			return ( (Number) value ).intValue();
		}
		else {
			return Integer.parseInt( String.valueOf( value ) );
		}
	}

	private static RootGraph<?> hintValueToEntityGraph(Object value) {
		return (RootGraph) value;
	}

}
