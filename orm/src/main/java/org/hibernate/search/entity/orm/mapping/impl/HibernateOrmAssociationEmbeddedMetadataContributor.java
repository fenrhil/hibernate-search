/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.orm.mapping.impl;

import org.hibernate.search.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMappingCollectorTypeNode;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.entity.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorTypeNode;

final class HibernateOrmAssociationEmbeddedMetadataContributor implements PojoTypeMetadataContributor {
	private final String propertyName;
	private final ContainerValueExtractorPath extractorPath;

	HibernateOrmAssociationEmbeddedMetadataContributor(String propertyName, ContainerValueExtractorPath extractorPath) {
		this.propertyName = propertyName;
		this.extractorPath = extractorPath;
	}

	@Override
	public void contributeModel(PojoAdditionalMetadataCollectorTypeNode collector) {
		collector.property( propertyName ).value( extractorPath ).associationEmbedded();
	}

	@Override
	public void contributeMapping(PojoMappingCollectorTypeNode collector) {
		// Nothing to do
	}
}
