/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.spi;

import java.util.Set;

import org.hibernate.search.v6poc.entity.pojo.search.PojoReference;
import org.hibernate.search.v6poc.search.dsl.SearchResultDefinitionContext;

public interface PojoSearchTarget {

	Set<Class<?>> getTargetedIndexedTypes();

	SearchResultDefinitionContext<PojoReference> search(PojoSessionContext context);

}
