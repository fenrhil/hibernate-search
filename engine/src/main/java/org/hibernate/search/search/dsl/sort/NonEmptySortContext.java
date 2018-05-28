/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.search.dsl.sort;

import org.hibernate.search.search.dsl.ExplicitEndContext;

/**
 * A superinterface for contexts allowing to define sorts.
 *
 * @param <N> The type of the end context (returned by {@link NonEmptySortContext#end()}).
 *
 * @author Emmanuel Bernard emmanuel@hibernate.org
 * @author Yoann Rodiere
 */
public interface NonEmptySortContext<N> extends ExplicitEndContext<N> {

	SearchSortContainerContext<N> then();

}
