/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.DeferredInitializationIndexFieldReference;
import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.ElasticsearchIndexFieldReference;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.DataType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.PropertyMapping;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.UnknownTypeJsonAccessor;
import org.hibernate.search.v6poc.bridge.builtin.spatial.GeoPoint;

import com.google.gson.JsonObject;

/**
 * @author Yoann Rodiere
 */
class CoordinatesFieldModelContext extends AbstractScalarFieldModelContext<GeoPoint> {

	private final UnknownTypeJsonAccessor accessor;

	public CoordinatesFieldModelContext(UnknownTypeJsonAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	protected void build(DeferredInitializationIndexFieldReference<GeoPoint> reference, PropertyMapping mapping) {
		super.build( reference, mapping );
		reference.initialize( new ElasticsearchIndexFieldReference<>( accessor, CoordinatesFieldModelContext::format ) );
		mapping.setType( DataType.GEO_POINT );
	}

	protected static JsonObject format(GeoPoint value) {
		if ( value == null ) {
			return null;
		}
		JsonObject result = new JsonObject();
		result.addProperty( "lat", value.getLatitude() );
		result.addProperty( "lon", value.getLongitude() );
		return result;
	}
}
