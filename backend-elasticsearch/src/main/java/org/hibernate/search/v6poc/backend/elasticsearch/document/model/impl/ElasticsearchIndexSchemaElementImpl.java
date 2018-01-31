/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import org.hibernate.search.v6poc.backend.document.model.FieldModelContext;
import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;
import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaNestingContext;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.ElasticsearchIndexSchemaElement;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.ElasticsearchIndexSchemaObjectField;

/**
 * @author Yoann Rodiere
 */
class ElasticsearchIndexSchemaElementImpl
		implements ElasticsearchIndexSchemaElement {

	protected final AbstractIndexSchemaObjectNodeBuilder nodeBuilder;
	private final IndexSchemaNestingContext nestingContext;

	ElasticsearchIndexSchemaElementImpl(AbstractIndexSchemaObjectNodeBuilder nodeBuilder,
			IndexSchemaNestingContext nestingContext) {
		this.nodeBuilder = nodeBuilder;
		this.nestingContext = nestingContext;
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "absolutePath=" ).append( nodeBuilder.getAbsolutePath() )
				.append( ",nestingContext=" ).append( nestingContext )
				.append( "]" )
				.toString();
	}

	@Override
	public FieldModelContext field(String relativeName) {
		return nestingContext.nest(
				relativeName,
				// If the field is included
				prefixedName -> {
					ElasticsearchFieldModelContextImpl fieldContext =
							new ElasticsearchFieldModelContextImpl( prefixedName );
					// Only take the contributor into account if the field is included
					nodeBuilder.putProperty( prefixedName, fieldContext );
					return fieldContext;
				},
				// If the field is filtered out
				ElasticsearchFieldModelContextImpl::new
				);
	}

	@Override
	public ElasticsearchIndexSchemaObjectField objectField(String relativeName, ObjectFieldStorage storage) {
		return nestingContext.nest(
				relativeName,
				// If the field is included
				(prefixedName, filter) -> {
					IndexSchemaObjectPropertyNodeBuilder nodeBuilder =
							new IndexSchemaObjectPropertyNodeBuilder( this.nodeBuilder.getAbsolutePath(), prefixedName );
					nodeBuilder.setStorage( storage );
					// Only take the contributor into account if the field is included
					this.nodeBuilder.putProperty( prefixedName, nodeBuilder );
					return new ElasticsearchIndexSchemaObjectFieldImpl( nodeBuilder, filter );
				},
				// If the field is filtered out
				(prefixedName, filter) -> {
					IndexSchemaObjectPropertyNodeBuilder nodeBuilder =
							new IndexSchemaObjectPropertyNodeBuilder( this.nodeBuilder.getAbsolutePath(), prefixedName );
					nodeBuilder.setStorage( storage );
					return new ElasticsearchIndexSchemaObjectFieldImpl( nodeBuilder, filter );
				} );
	}

}
