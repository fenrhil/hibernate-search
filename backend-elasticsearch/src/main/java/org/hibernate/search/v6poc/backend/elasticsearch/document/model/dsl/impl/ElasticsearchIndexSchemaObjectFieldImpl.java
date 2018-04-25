/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.dsl.impl;

import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.IndexSchemaNestingContext;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.dsl.ElasticsearchIndexSchemaObjectField;

/**
 * @author Yoann Rodiere
 */
class ElasticsearchIndexSchemaObjectFieldImpl extends ElasticsearchIndexSchemaElementImpl
		implements ElasticsearchIndexSchemaObjectField {

	ElasticsearchIndexSchemaObjectFieldImpl(IndexSchemaObjectPropertyNodeBuilder nodeBuilder,
			IndexSchemaNestingContext nestingContext) {
		super( nodeBuilder, nestingContext );
	}

	@Override
	public IndexObjectFieldAccessor createAccessor() {
		return nodeBuilder.getAccessor();
	}

}
