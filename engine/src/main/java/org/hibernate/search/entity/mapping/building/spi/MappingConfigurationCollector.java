/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.mapping.building.spi;

import org.hibernate.search.entity.model.spi.MappableTypeModel;

/**
 * @param <C> The Java type of type metadata contributors
 */
public interface MappingConfigurationCollector<C> {

	void mapToIndex(MappableTypeModel typeModel, String indexName);

	void collectContributor(MappableTypeModel typeModel, C contributor);

	void collectDiscoverer(TypeMetadataDiscoverer<C> metadataDiscoverer);

	void enableMultiTenancy();

}
