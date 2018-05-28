/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.definition.programmatic.impl;

import org.hibernate.search.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMetadataContributor;
import org.hibernate.search.entity.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollector;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMappingCollectorPropertyNode;


/**
 * @author Yoann Rodiere
 */
public class PropertyBridgeMappingContributor
		implements PojoMetadataContributor<PojoAdditionalMetadataCollector, PojoMappingCollectorPropertyNode> {

	private final BridgeBuilder<? extends PropertyBridge> bridgeBuilder;

	PropertyBridgeMappingContributor(BridgeBuilder<? extends PropertyBridge> bridgeBuilder) {
		this.bridgeBuilder = bridgeBuilder;
	}

	@Override
	public void contributeModel(PojoAdditionalMetadataCollector collector) {
		// Nothing to do
	}

	@Override
	public void contributeMapping(PojoMappingCollectorPropertyNode collector) {
		collector.bridge(
				bridgeBuilder
				/*
				 * Ignore mapped types, we don't need to discover new mappings automatically
				 * like in the annotation mappings.
				 */
		);
	}

}
