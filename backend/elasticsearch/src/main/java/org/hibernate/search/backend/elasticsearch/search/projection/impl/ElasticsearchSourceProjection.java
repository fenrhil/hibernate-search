/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.projection.impl;

import java.util.Optional;

import org.hibernate.search.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.backend.elasticsearch.gson.impl.JsonArrayAccessor;
import org.hibernate.search.backend.elasticsearch.gson.impl.JsonObjectAccessor;
import org.hibernate.search.backend.elasticsearch.search.impl.ElasticsearchSearchContext;
import org.hibernate.search.engine.search.loading.spi.LoadingResult;
import org.hibernate.search.engine.search.loading.spi.ProjectionHitMapper;
import org.hibernate.search.engine.search.projection.SearchProjection;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class ElasticsearchSourceProjection extends AbstractElasticsearchProjection<JsonObject, JsonObject> {

	private static final JsonArrayAccessor REQUEST_SOURCE_ACCESSOR = JsonAccessor.root().property( "_source" ).asArray();
	private static final JsonObjectAccessor HIT_SOURCE_ACCESSOR = JsonAccessor.root().property( "_source" ).asObject();
	private static final JsonPrimitive WILDCARD_ALL = new JsonPrimitive( "*" );

	private ElasticsearchSourceProjection(ElasticsearchSearchContext searchContext) {
		super( searchContext );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public void request(JsonObject requestBody, SearchProjectionRequestContext context) {
		REQUEST_SOURCE_ACCESSOR.addElementIfAbsent( requestBody, WILDCARD_ALL );
	}

	@Override
	public JsonObject extract(ProjectionHitMapper<?, ?> projectionHitMapper, JsonObject hit,
			SearchProjectionExtractContext context) {
		Optional<JsonObject> sourceElement = HIT_SOURCE_ACCESSOR.get( hit );
		if ( sourceElement.isPresent() ) {
			return sourceElement.get();
		}
		else {
			return null;
		}
	}

	@Override
	public JsonObject transform(LoadingResult<?, ?> loadingResult, JsonObject extractedData,
			SearchProjectionTransformContext context) {
		return extractedData;
	}

	static class Builder extends AbstractElasticsearchProjection.AbstractBuilder<JsonObject> {

		private final ElasticsearchSourceProjection projection;

		Builder(ElasticsearchSearchContext searchContext) {
			super( searchContext );
			this.projection = new ElasticsearchSourceProjection( searchContext );
		}

		@Override
		public SearchProjection<JsonObject> build() {
			return projection;
		}
	}
}
