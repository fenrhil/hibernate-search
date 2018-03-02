/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.mapper.pojo.bridge;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.model.SearchModel;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.mapping.AnnotationBridgeBuilder;
import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.bridge.annotation.CustomMarkerConsumingPropertyBridgeAnnotation;

public final class CustomMarkerConsumingPropertyBridge implements PropertyBridge {

	public static final String OBJECT_FIELD_NAME = "customMarkerField";

	public static final class Builder
			implements AnnotationBridgeBuilder<PropertyBridge, CustomMarkerConsumingPropertyBridgeAnnotation> {
		@Override
		public void initialize(CustomMarkerConsumingPropertyBridgeAnnotation annotation) {
			// Nothing to do
		}

		@Override
		public PropertyBridge build(BuildContext buildContext) {
			return new CustomMarkerConsumingPropertyBridge();
		}
	}

	private IndexObjectFieldAccessor objectFieldAccessor;

	private CustomMarkerConsumingPropertyBridge() {
	}

	@Override
	public void bind(IndexSchemaElement indexSchemaElement, PojoModelProperty bridgedPojoModelProperty,
			SearchModel searchModel) {
		if ( bridgedPojoModelProperty.properties().flatMap( property -> property.markers( CustomMarker.class ) )
				.findAny().isPresent() ) {
			objectFieldAccessor = indexSchemaElement.objectField( OBJECT_FIELD_NAME ).createAccessor();
		}
		else {
			throw new IllegalArgumentException( "Missing CustomMarker marker on the type's properties" );
		}
	}

	@Override
	public void write(DocumentElement target, PojoElement source) {
		objectFieldAccessor.add( target );
	}
}
