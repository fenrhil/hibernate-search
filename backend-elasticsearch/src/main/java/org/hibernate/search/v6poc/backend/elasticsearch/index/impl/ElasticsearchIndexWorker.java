/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.index.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.util.impl.URLEncodedString;
import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.ElasticsearchDocumentObjectBuilder;
import org.hibernate.search.v6poc.backend.elasticsearch.multitenancy.impl.MultiTenancyStrategy;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWork;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;
import org.hibernate.search.v6poc.backend.index.spi.DocumentContributor;
import org.hibernate.search.v6poc.backend.index.spi.DocumentReferenceProvider;
import org.hibernate.search.v6poc.backend.index.spi.IndexWorker;
import org.hibernate.search.v6poc.engine.spi.SessionContext;

import com.google.gson.JsonObject;


/**
 * @author Yoann Rodiere
 */
public abstract class ElasticsearchIndexWorker implements IndexWorker<ElasticsearchDocumentObjectBuilder> {

	protected final ElasticsearchWorkFactory factory;
	protected final MultiTenancyStrategy multiTenancyStrategy;
	protected final URLEncodedString indexName;
	protected final URLEncodedString typeName;
	protected final String tenantId;

	ElasticsearchIndexWorker(ElasticsearchWorkFactory factory, MultiTenancyStrategy multiTenancyStrategy,
			URLEncodedString indexName, URLEncodedString typeName,
			SessionContext sessionContext) {
		this.factory = factory;
		this.multiTenancyStrategy = multiTenancyStrategy;
		this.indexName = indexName;
		this.typeName = typeName;
		this.tenantId = sessionContext.getTenantIdentifier();
	}

	@Override
	public void add(DocumentReferenceProvider referenceProvider,
			DocumentContributor<ElasticsearchDocumentObjectBuilder> documentContributor) {
		String id = referenceProvider.getIdentifier();
		String elasticsearchId = multiTenancyStrategy.toElasticsearchId( tenantId, id );
		String routingKey = referenceProvider.getRoutingKey();

		ElasticsearchDocumentObjectBuilder builder = new ElasticsearchDocumentObjectBuilder();
		documentContributor.contribute( builder );
		JsonObject document = builder.build( multiTenancyStrategy, tenantId, id );

		collect( factory.add( indexName, typeName, elasticsearchId, routingKey, document ) );
	}

	@Override
	public void update(DocumentReferenceProvider referenceProvider,
			DocumentContributor<ElasticsearchDocumentObjectBuilder> documentContributor) {
		String id = referenceProvider.getIdentifier();
		String elasticsearchId = multiTenancyStrategy.toElasticsearchId( tenantId, id );
		String routingKey = referenceProvider.getRoutingKey();

		ElasticsearchDocumentObjectBuilder builder = new ElasticsearchDocumentObjectBuilder();
		documentContributor.contribute( builder );
		JsonObject document = builder.build( multiTenancyStrategy, tenantId, id );

		collect( factory.update( indexName, typeName, elasticsearchId, routingKey, document ) );
	}

	@Override
	public void delete(DocumentReferenceProvider referenceProvider) {
		String elasticsearchId = multiTenancyStrategy.toElasticsearchId( tenantId, referenceProvider.getIdentifier() );
		String routingKey = referenceProvider.getRoutingKey();

		collect( factory.delete( indexName, typeName, elasticsearchId, routingKey ) );
	}

	protected abstract void collect(ElasticsearchWork<?> work);

}
