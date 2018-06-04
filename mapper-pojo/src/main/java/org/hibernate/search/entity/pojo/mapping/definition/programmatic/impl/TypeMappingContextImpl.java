/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.definition.programmatic.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BeanReference;
import org.hibernate.search.engine.spi.BuildContext;
import org.hibernate.search.engine.spi.ImmutableBeanReference;
import org.hibernate.search.entity.mapping.building.spi.MappingConfigurationCollector;
import org.hibernate.search.entity.pojo.bridge.RoutingKeyBridge;
import org.hibernate.search.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.entity.pojo.bridge.impl.BeanResolverBridgeBuilder;
import org.hibernate.search.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMappingCollectorTypeNode;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoMetadataContributor;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.entity.pojo.mapping.definition.programmatic.PropertyMappingContext;
import org.hibernate.search.entity.pojo.mapping.definition.programmatic.TypeMappingContext;
import org.hibernate.search.entity.pojo.mapping.spi.PojoMappingConfigurationContributor;
import org.hibernate.search.entity.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorTypeNode;
import org.hibernate.search.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.entity.pojo.model.spi.PropertyHandle;

public class TypeMappingContextImpl
		implements TypeMappingContext, PojoMappingConfigurationContributor, PojoTypeMetadataContributor {

	private final PojoRawTypeModel<?> typeModel;

	private String indexName;
	private BridgeBuilder<? extends RoutingKeyBridge> routingKeyBridgeBuilder;

	private final List<PojoMetadataContributor<? super PojoAdditionalMetadataCollectorTypeNode, ? super PojoMappingCollectorTypeNode>>
			children = new ArrayList<>();

	TypeMappingContextImpl(PojoRawTypeModel<?> typeModel) {
		this.typeModel = typeModel;
	}

	@Override
	public void configure(BuildContext buildContext, ConfigurationPropertySource propertySource,
			MappingConfigurationCollector<PojoTypeMetadataContributor> configurationCollector) {
		if ( indexName != null ) {
			configurationCollector.mapToIndex( typeModel, indexName );
		}
		configurationCollector.collectContributor( typeModel, this );
	}

	@Override
	public void contributeModel(PojoAdditionalMetadataCollectorTypeNode collector) {
		children.forEach( c -> c.contributeModel( collector ) );
	}

	@Override
	public void contributeMapping(PojoMappingCollectorTypeNode collector) {
		if ( routingKeyBridgeBuilder != null ) {
			collector.routingKeyBridge( routingKeyBridgeBuilder );
		}
		children.forEach( c -> c.contributeMapping( collector ) );
	}

	@Override
	public TypeMappingContext indexed() {
		return indexed( typeModel.getJavaClass().getName() );
	}

	@Override
	public TypeMappingContext indexed(String indexName) {
		this.indexName = indexName;
		return this;
	}

	@Override
	public TypeMappingContext routingKeyBridge(String bridgeName) {
		return routingKeyBridge( new ImmutableBeanReference( bridgeName ) );
	}

	@Override
	public TypeMappingContext routingKeyBridge(Class<? extends RoutingKeyBridge> bridgeClass) {
		return routingKeyBridge( new ImmutableBeanReference( bridgeClass ) );
	}

	@Override
	public TypeMappingContext routingKeyBridge(String bridgeName, Class<? extends RoutingKeyBridge> bridgeClass) {
		return routingKeyBridge( new ImmutableBeanReference( bridgeName, bridgeClass ) );
	}

	private TypeMappingContext routingKeyBridge(BeanReference bridgeReference) {
		return routingKeyBridge( new BeanResolverBridgeBuilder<>( RoutingKeyBridge.class, bridgeReference ) );
	}

	@Override
	public TypeMappingContext routingKeyBridge(BridgeBuilder<? extends RoutingKeyBridge> builder) {
		this.routingKeyBridgeBuilder = builder;
		return this;
	}

	@Override
	public TypeMappingContext bridge(String bridgeName) {
		return bridge( new ImmutableBeanReference( bridgeName ) );
	}

	@Override
	public TypeMappingContext bridge(Class<? extends TypeBridge> bridgeClass) {
		return bridge( new ImmutableBeanReference( bridgeClass ) );
	}

	@Override
	public TypeMappingContext bridge(String bridgeName, Class<? extends TypeBridge> bridgeClass) {
		return bridge( new ImmutableBeanReference( bridgeName, bridgeClass ) );
	}

	private TypeMappingContext bridge(BeanReference bridgeReference) {
		return bridge( new BeanResolverBridgeBuilder<>( TypeBridge.class, bridgeReference ) );
	}

	@Override
	public TypeMappingContext bridge(BridgeBuilder<? extends TypeBridge> builder) {
		children.add( new TypeBridgeMappingContributor( builder ) );
		return this;
	}

	@Override
	public PropertyMappingContext property(String propertyName) {
		PojoPropertyModel<?> propertyModel = typeModel.getProperty( propertyName );
		PropertyHandle propertyHandle = propertyModel.getHandle();
		PropertyMappingContextImpl child = new PropertyMappingContextImpl( this, propertyHandle );
		children.add( child );
		return child;
	}

}
