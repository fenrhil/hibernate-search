/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.building.impl;

import java.util.Optional;

import org.hibernate.search.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.backend.document.model.dsl.IndexSchemaFieldContext;
import org.hibernate.search.entity.mapping.building.spi.FieldModelContributor;
import org.hibernate.search.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.entity.model.SearchModel;
import org.hibernate.search.entity.pojo.bridge.IdentifierBridge;
import org.hibernate.search.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.entity.pojo.bridge.RoutingKeyBridge;
import org.hibernate.search.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.entity.pojo.model.PojoModelType;
import org.hibernate.search.entity.pojo.model.path.impl.BoundPojoModelPathPropertyNode;
import org.hibernate.search.entity.pojo.model.path.impl.BoundPojoModelPathTypeNode;
import org.hibernate.search.entity.pojo.model.path.impl.BoundPojoModelPathValueNode;
import org.hibernate.search.entity.pojo.model.spi.PojoGenericTypeModel;

/**
 * Binds a mapping to a given entity model and index model
 * by creating the appropriate {@link ContainerValueExtractor extractors} and bridges.
 * <p>
 * Also binds the bridges where appropriate:
 * {@link TypeBridge#bind(IndexSchemaElement, PojoModelType, SearchModel)},
 * {@link PropertyBridge#bind(IndexSchemaElement, PojoModelProperty, SearchModel)},
 * {@link ValueBridge#bind(IndexSchemaFieldContext)}.
 * <p>
 * Incidentally, this will also generate the index model,
 * due to bridges contributing to the index model as we bind them.
 *
 * @author Yoann Rodiere
 */
public interface PojoIndexModelBinder {

	<C> BoundContainerValueExtractorPath<C, ?> bindExtractorPath(
			PojoGenericTypeModel<C> pojoGenericTypeModel, ContainerValueExtractorPath extractorPath);

	<C> Optional<BoundContainerValueExtractorPath<C, ?>> tryBindExtractorPath(
			PojoGenericTypeModel<C> pojoGenericTypeModel, ContainerValueExtractorPath extractorPath);

	<C, V> ContainerValueExtractor<? super C, V> createExtractors(
			BoundContainerValueExtractorPath<C, V> boundExtractorPath);

	<C, V> Optional<? extends ContainerValueExtractor<? super C, V>> tryCreateExtractors(
			BoundContainerValueExtractorPath<C, V> boundExtractorPath);

	<P> IdentifierBridge<P> createIdentifierBridge(BoundPojoModelPathPropertyNode<?, P> modelPath,
			BridgeBuilder<? extends IdentifierBridge<?>> bridgeBuilder);

	<T> BoundRoutingKeyBridge<T> addRoutingKeyBridge(IndexModelBindingContext bindingContext,
			BoundPojoModelPathTypeNode<T> modelPath, BridgeBuilder<? extends RoutingKeyBridge> bridgeBuilder);

	<T> Optional<BoundTypeBridge<T>> addTypeBridge(IndexModelBindingContext bindingContext,
			BoundPojoModelPathTypeNode<T> modelPath, BridgeBuilder<? extends TypeBridge> bridgeBuilder);

	<P> Optional<BoundPropertyBridge<P>> addPropertyBridge(IndexModelBindingContext bindingContext,
			BoundPojoModelPathPropertyNode<?, P> modelPath, BridgeBuilder<? extends PropertyBridge> bridgeBuilder);

	<V> Optional<BoundValueBridge<? super V, ?>> addValueBridge(IndexModelBindingContext bindingContext,
			BoundPojoModelPathValueNode<?, ?, V> modelPath, BridgeBuilder<? extends ValueBridge<?, ?>> bridgeBuilder,
			String relativeFieldName, FieldModelContributor contributor);

}
