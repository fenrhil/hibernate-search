/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.impl;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.v6poc.engine.spi.BeanReference;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingIndexModelCollector;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeIdentityMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoPropertyNodeMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.TypeModel;
import org.hibernate.search.v6poc.entity.processing.RoutingKeyBridge;

/**
 * @author Yoann Rodiere
 */
public class PojoTypeNodeProcessorBuilder extends AbstractPojoProcessorBuilder
		implements PojoTypeNodeMappingCollector {

	private final Map<String, PojoPropertyNodeProcessorBuilder> propertyProcessorBuilders = new HashMap<>();

	public PojoTypeNodeProcessorBuilder(
			TypeModel<?> typeModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider,
			MappingIndexModelCollector indexModelBuilder,
			PojoTypeNodeIdentityMappingCollector identityMappingCollector) {
		super( typeModel, contributorProvider, indexModelBuilder,
				identityMappingCollector );
	}

	@Override
	public void routingKeyBridge(BeanReference<? extends RoutingKeyBridge> reference) {
		RoutingKeyBridge bridge = indexModelCollector.addRoutingKeyBridge( indexableModel, reference );
		identityMappingCollector.routingKeyBridge( bridge );
	}

	@Override
	public PojoPropertyNodeMappingCollector property(String name) {
		return propertyProcessorBuilders.computeIfAbsent( name, this::createPropertyProcessorBuilder );
	}

	private PojoPropertyNodeProcessorBuilder createPropertyProcessorBuilder(String name) {
		PropertyModel<?> propertyModel = indexableModel.property( name ).getPropertyModel();
		return new PojoPropertyNodeProcessorBuilder( propertyModel,
				contributorProvider, indexModelCollector, identityMappingCollector
		);
	}

	public PojoTypeNodeProcessor build() {
		return new PojoTypeNodeProcessor( processors, propertyProcessorBuilders.values() );
	}

}
