/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.orm.impl;

import org.hibernate.search.entity.orm.hibernate.FullTextSearchTarget;
import org.hibernate.search.entity.orm.hibernate.HibernateOrmSearchQueryResultDefinitionContext;
import org.hibernate.search.entity.orm.hibernate.HibernateOrmSearchTarget;
import org.hibernate.search.search.SearchPredicate;
import org.hibernate.search.search.SearchSort;
import org.hibernate.search.search.dsl.predicate.SearchPredicateContainerContext;
import org.hibernate.search.search.dsl.sort.SearchSortContainerContext;

class FullTextSearchTargetImpl<T> implements FullTextSearchTarget<T> {

	private final HibernateOrmSearchTarget<T> delegate;

	public FullTextSearchTargetImpl(HibernateOrmSearchTarget<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public HibernateOrmSearchQueryResultDefinitionContext<T> query() {
		return delegate.jpaQuery();
	}

	@Override
	public SearchPredicateContainerContext<SearchPredicate> predicate() {
		return delegate.predicate();
	}

	@Override
	public SearchSortContainerContext<SearchSort> sort() {
		return delegate.sort();
	}
}
