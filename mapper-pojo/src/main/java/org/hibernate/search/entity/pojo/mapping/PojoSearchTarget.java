/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping;

import org.hibernate.search.entity.pojo.search.PojoReference;
import org.hibernate.search.search.SearchPredicate;
import org.hibernate.search.search.SearchSort;
import org.hibernate.search.search.dsl.predicate.SearchPredicateContainerContext;
import org.hibernate.search.search.dsl.query.SearchQueryResultDefinitionContext;
import org.hibernate.search.search.dsl.sort.SearchSortContainerContext;

public interface PojoSearchTarget<T> {

	SearchQueryResultDefinitionContext<PojoReference, T> query();

	SearchPredicateContainerContext<SearchPredicate> predicate();

	SearchSortContainerContext<SearchSort> sort();

}
