/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.building.impl;

import org.hibernate.search.v6poc.backend.document.spi.DocumentState;
import org.hibernate.search.v6poc.bridge.spi.IdentifierBridge;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexManagerBuildingState;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoTypeManager;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoTypeManagerContainer;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoProxyIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.IdentifierConverter;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoTypeNodeProcessorBuilder;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PropertyIdentifierConverter;
import org.hibernate.search.v6poc.util.SearchException;

public class PojoTypeManagerBuilder<E, D extends DocumentState> {
	private final PojoProxyIntrospector proxyIntrospector;
	private final Class<E> javaType;
	private final IndexManagerBuildingState<D> indexManagerBuildingState;

	private final PojoTypeNodeProcessorBuilder processorBuilder;
	private IdentifierConverter<?, E> idConverter;

	public PojoTypeManagerBuilder(Class<E> javaType,
			PojoIntrospector introspector, PojoProxyIntrospector proxyIntrospector,
			IndexManagerBuildingState<D> indexManagerBuildingState,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider,
			IdentifierConverter<?, E> defaultIdentifierConverter) {
		this.proxyIntrospector = proxyIntrospector;
		this.javaType = javaType;
		this.indexManagerBuildingState = indexManagerBuildingState;
		this.processorBuilder = new PojoTypeNodeProcessorBuilder(
				javaType, introspector, contributorProvider,
				indexManagerBuildingState.getModelCollector(),
				this::setIdentifierBridge );
		this.idConverter = defaultIdentifierConverter;
	}

	public PojoTypeNodeMappingCollector asCollector() {
		return processorBuilder;
	}

	private void setIdentifierBridge(PropertyHandle handle, IdentifierBridge<?> bridge) {
		this.idConverter = new PropertyIdentifierConverter<>( handle, bridge );
	}

	public void addTo(PojoTypeManagerContainer.Builder builder) {
		if ( idConverter == null ) {
			throw new SearchException( "Missing identifier mapping for indexed type '" + javaType + "'" );
		}
		PojoTypeManager<?, E, D> typeManager = new PojoTypeManager<>( proxyIntrospector, idConverter, javaType,
				processorBuilder.build(), indexManagerBuildingState.getResult() );
		builder.add( indexManagerBuildingState.getIndexName(), javaType, typeManager );
	}
}