/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.orm.search.impl;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.entity.orm.hibernate.HibernateOrmSearchQueryResultDefinitionContext;
import org.hibernate.search.entity.orm.hibernate.HibernateOrmSearchTarget;
import org.hibernate.search.entity.pojo.mapping.spi.PojoSearchTargetDelegate;
import org.hibernate.search.entity.pojo.search.PojoReference;
import org.hibernate.search.search.SearchPredicate;
import org.hibernate.search.search.SearchSort;
import org.hibernate.search.search.dsl.predicate.SearchPredicateContainerContext;
import org.hibernate.search.search.dsl.query.SearchQueryResultDefinitionContext;
import org.hibernate.search.search.dsl.sort.SearchSortContainerContext;

public class HibernateOrmSearchTargetImpl<T> implements HibernateOrmSearchTarget<T> {

	private final PojoSearchTargetDelegate<T> searchTargetDelegate;
	private final SessionImplementor sessionImplementor;

	public HibernateOrmSearchTargetImpl(PojoSearchTargetDelegate<T> searchTargetDelegate,
			SessionImplementor sessionImplementor) {
		this.searchTargetDelegate = searchTargetDelegate;
		this.sessionImplementor = sessionImplementor;
	}

	@Override
	public HibernateOrmSearchQueryResultDefinitionContext<T> jpaQuery() {
		return new HibernateOrmSearchQueryResultDefinitionContextImpl<>( searchTargetDelegate, sessionImplementor );
	}

	@Override
	public SearchQueryResultDefinitionContext<PojoReference, T> query() {
		ObjectLoaderBuilder<T> objectLoaderBuilder = new ObjectLoaderBuilder<>(
				sessionImplementor,
				searchTargetDelegate.getTargetedIndexedTypes()
		);
		MutableObjectLoadingOptions mutableObjectLoadingOptions = new MutableObjectLoadingOptions();
		return searchTargetDelegate.query( objectLoaderBuilder.build( mutableObjectLoadingOptions ) );
	}

	@Override
	public SearchPredicateContainerContext<SearchPredicate> predicate() {
		return searchTargetDelegate.predicate();
	}

	@Override
	public SearchSortContainerContext<SearchSort> sort() {
		return searchTargetDelegate.sort();
	}
}
