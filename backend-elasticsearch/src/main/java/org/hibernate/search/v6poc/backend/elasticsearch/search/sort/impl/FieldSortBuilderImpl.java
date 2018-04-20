/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.sort.impl;

import java.util.function.Function;

import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.types.codec.impl.ElasticsearchFieldCodec;
import org.hibernate.search.v6poc.search.sort.spi.FieldSortBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class FieldSortBuilderImpl extends AbstractSearchSortBuilder
		implements FieldSortBuilder<ElasticsearchSearchSortCollector> {

	private static final JsonAccessor<JsonElement> MISSING = JsonAccessor.root().property( "missing" );
	private static final JsonPrimitive MISSING_FIRST_KEYWORD_JSON = new JsonPrimitive( "_first" );
	private static final JsonPrimitive MISSING_LAST_KEYWORD_JSON = new JsonPrimitive( "_last" );

	private final String absoluteFieldPath;
	private final Function<String, ElasticsearchFieldCodec> fieldCodecFunction;
	private ElasticsearchFieldCodec fieldCodec;

	FieldSortBuilderImpl(String absoluteFieldPath,
			Function<String, ElasticsearchFieldCodec> fieldCodecFunction) {
		this.absoluteFieldPath = absoluteFieldPath;
		this.fieldCodecFunction = fieldCodecFunction;
	}

	@Override
	public void missingFirst() {
		MISSING.set( getInnerObject(), MISSING_FIRST_KEYWORD_JSON );
	}

	@Override
	public void missingLast() {
		MISSING.set( getInnerObject(), MISSING_LAST_KEYWORD_JSON );
	}

	@Override
	public void missingAs(Object value) {
		MISSING.set( getInnerObject(), getFieldCodec().encode( value ) );
	}

	@Override
	public void contribute(ElasticsearchSearchSortCollector collector) {
		JsonObject innerObject = getInnerObject();
		if ( innerObject.size() == 0 ) {
			collector.collectSort( new JsonPrimitive( absoluteFieldPath ) );
		}
		else {
			JsonObject outerObject = new JsonObject();
			outerObject.add( absoluteFieldPath, innerObject );
			collector.collectSort( outerObject );
		}
	}

	private ElasticsearchFieldCodec getFieldCodec() {
		if ( fieldCodec == null ) {
			fieldCodec = fieldCodecFunction.apply( absoluteFieldPath );
		}
		return fieldCodec;
	}
}
