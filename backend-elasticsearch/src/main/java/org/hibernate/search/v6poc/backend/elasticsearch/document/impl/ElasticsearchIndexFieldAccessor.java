/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaFieldNode;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;

import com.google.gson.JsonElement;


/**
 * @author Yoann Rodiere
 * @author Guillaume Smet
 */
public class ElasticsearchIndexFieldAccessor<T> implements IndexFieldAccessor<T> {

	private final JsonAccessor<JsonElement> accessor;

	private final ElasticsearchIndexSchemaFieldNode schemaNode;

	public ElasticsearchIndexFieldAccessor(JsonAccessor<JsonElement> accessor, ElasticsearchIndexSchemaFieldNode schemaNode) {
		this.accessor = accessor;
		this.schemaNode = schemaNode;
	}

	@Override
	public void write(DocumentElement state, T value) {
		((ElasticsearchDocumentObjectBuilder) state).add( schemaNode.getParent(), accessor, schemaNode.getFormatter().format( value ) );
	}

}
