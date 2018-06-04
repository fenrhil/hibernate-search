/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.orm.search.impl;

import java.util.List;
import java.util.function.Function;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.entity.orm.hibernate.FullTextQuery;
import org.hibernate.search.entity.orm.hibernate.HibernateOrmSearchQueryResultDefinitionContext;
import org.hibernate.search.entity.orm.impl.FullTextQueryImpl;
import org.hibernate.search.entity.pojo.mapping.spi.PojoSearchTargetDelegate;
import org.hibernate.search.search.dsl.query.SearchQueryResultContext;

public class HibernateOrmSearchQueryResultDefinitionContextImpl<O>
		implements HibernateOrmSearchQueryResultDefinitionContext<O> {
	private final PojoSearchTargetDelegate<O> searchTargetDelegate;
	private final SessionImplementor sessionImplementor;
	private final ObjectLoaderBuilder<O> objectLoaderBuilder;

	public HibernateOrmSearchQueryResultDefinitionContextImpl(
			PojoSearchTargetDelegate<O> searchTargetDelegate,
			SessionImplementor sessionImplementor) {
		this.searchTargetDelegate = searchTargetDelegate;
		this.sessionImplementor = sessionImplementor;
		this.objectLoaderBuilder = new ObjectLoaderBuilder<>( sessionImplementor, searchTargetDelegate.getTargetedIndexedTypes() );
	}

	@Override
	public SearchQueryResultContext<? extends FullTextQuery<O>> asEntities() {
		MutableObjectLoadingOptions loadingOptions = new MutableObjectLoadingOptions();
		return searchTargetDelegate.query( objectLoaderBuilder.build( loadingOptions ) )
				.asObjects()
				.asWrappedQuery( q -> new FullTextQueryImpl<>( q, sessionImplementor, loadingOptions ) );
	}

	@Override
	public <T> SearchQueryResultContext<? extends FullTextQuery<T>> asEntities(Function<O, T> hitTransformer) {
		MutableObjectLoadingOptions loadingOptions = new MutableObjectLoadingOptions();
		return searchTargetDelegate.query( objectLoaderBuilder.build( loadingOptions, hitTransformer ) )
				.asObjects()
				.asWrappedQuery( q -> new FullTextQueryImpl<>( q, sessionImplementor, loadingOptions ) );
	}

	@Override
	public <T> SearchQueryResultContext<? extends FullTextQuery<T>> asProjections(
			Function<List<?>, T> hitTransformer, String... projections) {
		MutableObjectLoadingOptions loadingOptions = new MutableObjectLoadingOptions();
		return searchTargetDelegate.query( objectLoaderBuilder.build( loadingOptions ) )
				.asProjections( hitTransformer, projections )
				.asWrappedQuery( q -> new FullTextQueryImpl<>( q, sessionImplementor, loadingOptions ) );
	}
}
