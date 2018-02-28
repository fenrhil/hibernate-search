/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;
import org.hibernate.search.v6poc.entity.mapping.building.spi.FieldModelContributor;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.bridge.FunctionBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.IdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.CollectionValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.IterableValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.MapValueValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoPropertyNodeMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeIdentityMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoModelPropertyRootElement;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;

/**
 * @author Yoann Rodiere
 */
public class PojoPropertyNodeProcessorBuilder<P, T> extends AbstractPojoNodeProcessorBuilder<P>
		implements PojoPropertyNodeMappingCollector {

	private final PojoTypeModel<P> parentTypeModel;
	private final PropertyHandle propertyHandle;
	private final PojoGenericTypeModel<T> propertyTypeModel;
	private final PojoModelPropertyRootElement pojoModelRootElement;

	private final PojoTypeNodeIdentityMappingCollector identityMappingCollector;

	private final Collection<PropertyBridge> propertyBridges = new ArrayList<>();
	private final Collection<FunctionBridgeProcessor<? super T, ?>> functionBridgeProcessors = new ArrayList<>();
	private final Collection<AbstractPojoNodeProcessorBuilder<? super T>> nestedProcessorBuilders = new ArrayList<>();
	// Note: if this value is set, it is always also added to nestedProcessorBuilders
	private PojoContainerNodeProcessorBuilder<? super T, ?> containerProcessorBuilder = null;

	PojoPropertyNodeProcessorBuilder(
			PojoTypeNodeProcessorBuilder<P> parent, PojoTypeModel<P> parentTypeModel,
			PojoPropertyModel<T> propertyModel, PropertyHandle propertyHandle,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider,
			PojoIndexModelBinder indexModelBinder, IndexModelBindingContext bindingContext,
			PojoTypeNodeIdentityMappingCollector identityMappingCollector) {
		super( parent, contributorProvider, indexModelBinder, bindingContext );
		this.parentTypeModel = parentTypeModel;
		this.propertyHandle = propertyHandle;
		this.propertyTypeModel = propertyModel.getTypeModel();

		// FIXME do something more with the pojoModelRootElement, to be able to use it in containedIn processing in particular
		this.pojoModelRootElement = new PojoModelPropertyRootElement( propertyModel, contributorProvider );

		this.identityMappingCollector = identityMappingCollector;
	}

	@Override
	public void bridge(BridgeBuilder<? extends PropertyBridge> builder) {
		indexModelBinder.addPropertyBridge( bindingContext, pojoModelRootElement, builder )
				.ifPresent( propertyBridges::add );
	}

	@Override
	public void functionBridge(BridgeBuilder<? extends FunctionBridge<?, ?>> builder,
			String fieldName, FieldModelContributor fieldModelContributor) {
		String defaultedFieldName = fieldName;
		if ( defaultedFieldName == null ) {
			defaultedFieldName = propertyHandle.getName();
		}

		PojoContainerNodeProcessorBuilder<? super T, ?> containerProcessorBuilder = getContainerProcessorBuilder();
		if ( containerProcessorBuilder != null ) {
			containerProcessorBuilder.functionBridge( builder, defaultedFieldName, fieldModelContributor );
		}
		else {
			indexModelBinder.addFunctionBridge(
					bindingContext, propertyTypeModel, builder, defaultedFieldName,
					fieldModelContributor
			)
					.ifPresent( functionBridgeProcessors::add );
		}
	}

	@Override
	@SuppressWarnings( {"rawtypes", "unchecked"} )
	public void identifierBridge(BridgeBuilder<? extends IdentifierBridge<?>> builder) {
		IdentifierBridge<T> bridge = indexModelBinder.createIdentifierBridge( pojoModelRootElement, propertyTypeModel, builder );
		identityMappingCollector.identifierBridge( propertyTypeModel, propertyHandle, bridge );
	}

	@Override
	public void containedIn() {
		// FIXME implement ContainedIn
		// FIXME also contribute containedIns to indexedEmbeddeds using the parent's metadata here, if possible?
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public void indexedEmbedded(String relativePrefix, ObjectFieldStorage storage,
			Integer maxDepth, Set<String> includePaths) {
		// TODO handle collections

		String defaultedRelativePrefix = relativePrefix;
		if ( defaultedRelativePrefix == null ) {
			defaultedRelativePrefix = propertyHandle.getName() + ".";
		}

		Optional<IndexModelBindingContext> nestedBindingContextOptional = bindingContext.addIndexedEmbeddedIfIncluded(
				parentTypeModel.getRawType(), defaultedRelativePrefix, storage, maxDepth, includePaths );
		nestedBindingContextOptional.ifPresent( nestedBindingContext -> {
			PojoContainerNodeProcessorBuilder<? super T, ?> containerProcessorBuilder = getContainerProcessorBuilder();

			if ( containerProcessorBuilder != null ) {
				containerProcessorBuilder.indexedEmbedded( nestedBindingContext );
			}
			else {
				PojoTypeNodeProcessorBuilder<T> nestedProcessorBuilder = new PojoTypeNodeProcessorBuilder<>(
						this, propertyTypeModel, contributorProvider, indexModelBinder, nestedBindingContext,
						PojoTypeNodeIdentityMappingCollector.noOp() // Do NOT propagate the identity mapping collector to IndexedEmbeddeds
				);
				nestedProcessorBuilders.add( nestedProcessorBuilder );
				contributorProvider.get( propertyTypeModel.getRawType() )
						.forEach( c -> c.contributeMapping( nestedProcessorBuilder ) );
			}
		} );
	}

	@Override
	protected void appendSelfPath(StringBuilder builder) {
		builder.append( "." ).append( propertyHandle.getName() );
	}

	@SuppressWarnings("unchecked") // Checks are implemented using reflection
	private PojoContainerNodeProcessorBuilder<? super T, ?> getContainerProcessorBuilder() {
		if ( containerProcessorBuilder == null ) {
			Optional<? extends PojoGenericTypeModel<?>> elementTypeModelOptional =
					propertyTypeModel.getTypeArgument( Map.class, 1 );
			if ( elementTypeModelOptional.isPresent() ) {
				containerProcessorBuilder = new PojoContainerNodeProcessorBuilder(
						this, elementTypeModelOptional.get(), MapValueValueExtractor.get(),
						contributorProvider, indexModelBinder, bindingContext
				);
				nestedProcessorBuilders.add( containerProcessorBuilder );
				return containerProcessorBuilder;
			}
			elementTypeModelOptional = propertyTypeModel.getTypeArgument( Collection.class, 0 );
			if ( elementTypeModelOptional.isPresent() ) {
				containerProcessorBuilder = new PojoContainerNodeProcessorBuilder(
						this, elementTypeModelOptional.get(), CollectionValueExtractor.get(),
						contributorProvider, indexModelBinder, bindingContext
				);
				nestedProcessorBuilders.add( containerProcessorBuilder );
				return containerProcessorBuilder;
			}
			elementTypeModelOptional = propertyTypeModel.getTypeArgument( Iterable.class, 0 );
			if ( elementTypeModelOptional.isPresent() ) {
				containerProcessorBuilder = new PojoContainerNodeProcessorBuilder(
						this, elementTypeModelOptional.get(), IterableValueExtractor.get(),
						contributorProvider, indexModelBinder, bindingContext
				);
				nestedProcessorBuilders.add( containerProcessorBuilder );
				return containerProcessorBuilder;
			}
		}
		return containerProcessorBuilder;
	}

	@Override
	Optional<PojoPropertyNodeProcessor<P, T>> build() {
		Collection<PropertyBridge> immutableBridges = propertyBridges.isEmpty() ? Collections.emptyList() : new ArrayList<>( propertyBridges );
		Collection<PojoNodeProcessor<? super T>> immutableNestedProcessors =
				functionBridgeProcessors.isEmpty() && nestedProcessorBuilders.isEmpty()
				? Collections.emptyList()
				: new ArrayList<>( functionBridgeProcessors.size() + nestedProcessorBuilders.size() );
		immutableNestedProcessors.addAll( functionBridgeProcessors );
		nestedProcessorBuilders.stream()
				.map( AbstractPojoNodeProcessorBuilder::build )
				.filter( Optional::isPresent )
				.map( Optional::get )
				.forEach( immutableNestedProcessors::add );

		if ( immutableBridges.isEmpty() && immutableNestedProcessors.isEmpty() ) {
			/*
			 * If this processor doesn't have any bridge, nor any nested processor,
			 * it is useless and we don't need to build it
			 */
			return Optional.empty();
		}
		else {
			return Optional.of( new PojoPropertyNodeProcessor<>(
					propertyHandle, immutableBridges, immutableNestedProcessors
			) );
		}
	}
}
