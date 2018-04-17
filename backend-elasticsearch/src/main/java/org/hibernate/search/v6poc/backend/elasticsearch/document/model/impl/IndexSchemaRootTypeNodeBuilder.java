/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.ElasticsearchIndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.DynamicType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.RootTypeMapping;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.RoutingType;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.MultiTenancyStrategy;

class IndexSchemaRootTypeNodeBuilder extends AbstractIndexSchemaObjectNodeBuilder {

	private MultiTenancyStrategy multiTenancyStrategy;

	private RoutingType routing = null;

	IndexSchemaRootTypeNodeBuilder(MultiTenancyStrategy multiTenancyStrategy) {
		this.multiTenancyStrategy = multiTenancyStrategy;
	}

	public void setRouting(RoutingType routing) {
		this.routing = routing;
	}

	@Override
	public String getAbsolutePath() {
		return null;
	}

	protected RootTypeMapping contribute(ElasticsearchIndexSchemaNodeCollector collector) {
		ElasticsearchIndexSchemaObjectNode node = ElasticsearchIndexSchemaObjectNode.root();

		accessor.initialize( new ElasticsearchIndexObjectFieldAccessor( JsonAccessor.root(), node ) );

		RootTypeMapping mapping = new RootTypeMapping();
		if ( routing != null ) {
			mapping.setRouting( routing );
		}

		multiTenancyStrategy.contributeToMapping( mapping );

		// TODO allow to configure this, both at index level (configuration properties) and at field level (ElasticsearchExtension)
		mapping.setDynamic( DynamicType.STRICT );

		contributeChildren( mapping, node, collector );

		return mapping;
	}
}
