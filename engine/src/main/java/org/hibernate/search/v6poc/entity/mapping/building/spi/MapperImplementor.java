/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.building.spi;

import org.hibernate.search.v6poc.engine.SearchManager;
import org.hibernate.search.v6poc.engine.SearchManagerBuilder;
import org.hibernate.search.v6poc.entity.mapping.MappingType;
import org.hibernate.search.v6poc.entity.model.spi.IndexableTypeOrdering;

/**
 * @author Yoann Rodiere
 */
public interface MapperImplementor<C, T extends SearchManager, B extends SearchManagerBuilder<T>>
		extends MappingType<T, B> {

	IndexableTypeOrdering getTypeOrdering();

	MappingBuilder<C, B> createBuilder();

}
