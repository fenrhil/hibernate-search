/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.definition.programmatic.impl;

import org.hibernate.search.entity.pojo.bridge.mapping.MarkerBuilder;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMappingCollector;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMetadataContributor;
import org.hibernate.search.entity.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorPropertyNode;


/**
 * @author Yoann Rodiere
 */
public class MarkerMappingContributor
		implements PojoMetadataContributor<PojoAdditionalMetadataCollectorPropertyNode, PojoMappingCollector> {

	private final MarkerBuilder markerBuilder;

	public MarkerMappingContributor(MarkerBuilder markerBuilder) {
		this.markerBuilder = markerBuilder;
	}

	@Override
	public void contributeModel(PojoAdditionalMetadataCollectorPropertyNode collector) {
		collector.marker( markerBuilder );
	}

	@Override
	public void contributeMapping(PojoMappingCollector collector) {
		// Nothing to do
	}

}
