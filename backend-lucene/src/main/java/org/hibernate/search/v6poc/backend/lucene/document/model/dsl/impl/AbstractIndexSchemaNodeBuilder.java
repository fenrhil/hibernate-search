/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.document.model.dsl.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneIndexSchemaNodeCollector;
import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneIndexSchemaNodeContributor;
import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneIndexSchemaObjectNode;
import org.hibernate.search.v6poc.backend.lucene.logging.impl.Log;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

/**
 * A schema node builder.
 */
abstract class AbstractIndexSchemaNodeBuilder {
	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final Map<String, LuceneIndexSchemaNodeContributor> content = new HashMap<>();

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( ",absolutePath=" ).append( getAbsolutePath() )
				.append( "]" )
				.toString();
	}

	public abstract String getAbsolutePath();

	public void putProperty(String name, LuceneIndexSchemaNodeContributor contributor) {
		Object previous = content.putIfAbsent( name, contributor );
		if ( previous != null ) {
			throw log.indexSchemaNodeNameConflict( getAbsolutePath(), name);
		}
	}

	final void contributeChildren(LuceneIndexSchemaObjectNode node, LuceneIndexSchemaNodeCollector collector) {
		for ( LuceneIndexSchemaNodeContributor contributor : content.values() ) {
			contributor.contribute( collector, node );
		}
	}
}
