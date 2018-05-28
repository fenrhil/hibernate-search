/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.index.impl;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.backend.elasticsearch.client.impl.ElasticsearchClient;
import org.hibernate.search.backend.elasticsearch.util.impl.URLEncodedString;
import org.hibernate.search.backend.elasticsearch.document.impl.ElasticsearchDocumentObjectBuilder;
import org.hibernate.search.backend.elasticsearch.document.model.impl.ElasticsearchIndexModel;
import org.hibernate.search.backend.elasticsearch.multitenancy.impl.MultiTenancyStrategy;
import org.hibernate.search.backend.elasticsearch.orchestration.impl.ElasticsearchWorkOrchestrator;
import org.hibernate.search.backend.elasticsearch.orchestration.impl.StubElasticsearchWorkOrchestrator;
import org.hibernate.search.backend.elasticsearch.work.impl.ElasticsearchWork;
import org.hibernate.search.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;
import org.hibernate.search.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.backend.index.spi.StreamIndexWorker;
import org.hibernate.search.backend.spi.BackendImplementor;
import org.hibernate.search.engine.spi.SessionContext;

public class IndexingBackendContext {
	// TODO use a dedicated object for the error context instead of the backend
	private final BackendImplementor<?> backend;

	private final ElasticsearchClient client;
	private final ElasticsearchWorkFactory workFactory;
	private final MultiTenancyStrategy multiTenancyStrategy;

	private final ElasticsearchWorkOrchestrator streamOrchestrator;

	public IndexingBackendContext(BackendImplementor<?> backend,
			ElasticsearchClient client,
			ElasticsearchWorkFactory workFactory,
			MultiTenancyStrategy multiTenancyStrategy,
			ElasticsearchWorkOrchestrator streamOrchestrator) {
		this.backend = backend;
		this.client = client;
		this.multiTenancyStrategy = multiTenancyStrategy;
		this.workFactory = workFactory;
		this.streamOrchestrator = streamOrchestrator;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[backend=" + backend + "]";
	}

	CompletableFuture<?> initializeIndex(URLEncodedString indexName, URLEncodedString typeName,
			ElasticsearchIndexModel model) {
		ElasticsearchWork<?> dropWork = workFactory.dropIndexIfExists( indexName );
		ElasticsearchWork<?> createWork = workFactory.createIndex( indexName, typeName, model.getMapping() );
		return streamOrchestrator.submit( Arrays.asList( dropWork, createWork ) );
	}

	ElasticsearchWorkOrchestrator createChangesetOrchestrator() {
		return new StubElasticsearchWorkOrchestrator( client );
	}

	ChangesetIndexWorker<ElasticsearchDocumentObjectBuilder> createChangesetIndexWorker(
			ElasticsearchWorkOrchestrator orchestrator,
			URLEncodedString indexName, URLEncodedString typeName,
			SessionContext sessionContext) {
		multiTenancyStrategy.checkTenantId( backend, sessionContext.getTenantIdentifier() );

		return new ElasticsearchChangesetIndexWorker( workFactory, multiTenancyStrategy, orchestrator,
				indexName, typeName, sessionContext );
	}

	StreamIndexWorker<ElasticsearchDocumentObjectBuilder> createStreamIndexWorker(
			URLEncodedString indexName, URLEncodedString typeName,
			SessionContext sessionContext) {
		multiTenancyStrategy.checkTenantId( backend, sessionContext.getTenantIdentifier() );

		return new ElasticsearchStreamIndexWorker( workFactory, multiTenancyStrategy, streamOrchestrator,
				indexName, typeName, sessionContext );
	}
}
