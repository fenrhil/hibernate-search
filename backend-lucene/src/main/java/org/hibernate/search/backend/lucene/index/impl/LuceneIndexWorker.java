/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.index.impl;

import org.hibernate.search.backend.index.spi.DocumentContributor;
import org.hibernate.search.backend.index.spi.DocumentReferenceProvider;
import org.hibernate.search.backend.index.spi.IndexWorker;
import org.hibernate.search.backend.lucene.document.impl.LuceneIndexEntry;
import org.hibernate.search.backend.lucene.document.impl.LuceneRootDocumentBuilder;
import org.hibernate.search.backend.lucene.multitenancy.impl.MultiTenancyStrategy;
import org.hibernate.search.backend.lucene.work.impl.LuceneIndexWork;
import org.hibernate.search.backend.lucene.work.impl.LuceneWorkFactory;
import org.hibernate.search.engine.spi.SessionContext;


/**
 * @author Guillaume Smet
 */
abstract class LuceneIndexWorker implements IndexWorker<LuceneRootDocumentBuilder> {

	protected final LuceneWorkFactory factory;
	protected final MultiTenancyStrategy multiTenancyStrategy;
	protected final String indexName;
	protected final String tenantId;

	protected LuceneIndexWorker(LuceneWorkFactory factory, MultiTenancyStrategy multiTenancyStrategy, String indexName,
			SessionContext sessionContext) {
		this.factory = factory;
		this.multiTenancyStrategy = multiTenancyStrategy;
		this.indexName = indexName;
		this.tenantId = sessionContext.getTenantIdentifier();
	}

	@Override
	public void add(DocumentReferenceProvider referenceProvider,
			DocumentContributor<LuceneRootDocumentBuilder> documentContributor) {
		String id = referenceProvider.getIdentifier();
		String routingKey = referenceProvider.getRoutingKey();

		LuceneRootDocumentBuilder builder = new LuceneRootDocumentBuilder();
		documentContributor.contribute( builder );
		LuceneIndexEntry indexEntry = builder.build( indexName, multiTenancyStrategy, tenantId, id );

		collect( factory.add( indexName, tenantId, id, routingKey, indexEntry ) );
		// FIXME remove this explicit commit
		collect( factory.commit( indexName ) );
	}

	@Override
	public void update(DocumentReferenceProvider referenceProvider,
			DocumentContributor<LuceneRootDocumentBuilder> documentContributor) {
		String id = referenceProvider.getIdentifier();
		String routingKey = referenceProvider.getRoutingKey();

		LuceneRootDocumentBuilder builder = new LuceneRootDocumentBuilder();
		documentContributor.contribute( builder );
		LuceneIndexEntry indexEntry = builder.build( indexName, multiTenancyStrategy, tenantId, id );

		collect( factory.update( indexName, tenantId, id, routingKey, indexEntry ) );
		// FIXME remove this explicit commit
		collect( factory.commit( indexName ) );
	}

	@Override
	public void delete(DocumentReferenceProvider referenceProvider) {
		String id = referenceProvider.getIdentifier();
		String routingKey = referenceProvider.getRoutingKey();

		collect( factory.delete( indexName, tenantId, id, routingKey ) );
		// FIXME remove this explicit commit
		collect( factory.commit( indexName ) );
	}

	protected abstract void collect(LuceneIndexWork<?> work);

}
