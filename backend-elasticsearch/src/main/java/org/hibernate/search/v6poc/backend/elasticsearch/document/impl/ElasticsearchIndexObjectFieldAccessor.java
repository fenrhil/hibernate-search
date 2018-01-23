/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchObjectNodeModel;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;

import com.google.gson.JsonObject;


/**
 * @author Yoann Rodiere
 */
public class ElasticsearchIndexObjectFieldAccessor implements IndexObjectFieldAccessor {

	private final JsonAccessor<JsonObject> relativeAccessor;

	private final ElasticsearchObjectNodeModel model;

	public ElasticsearchIndexObjectFieldAccessor(JsonAccessor<JsonObject> relativeAccessor,
			ElasticsearchObjectNodeModel model) {
		this.relativeAccessor = relativeAccessor;
		this.model = model;
	}

	@Override
	public DocumentElement add(DocumentElement target) {
		JsonObject jsonObject = new JsonObject();
		((ElasticsearchDocumentObjectBuilder) target).add( model.getParent(), relativeAccessor, jsonObject );
		return new ElasticsearchDocumentObjectBuilder( model, jsonObject );
	}

	@Override
	public void addMissing(DocumentElement target) {
		((ElasticsearchDocumentObjectBuilder) target).add( model.getParent(), relativeAccessor, null );
	}
}
