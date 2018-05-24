/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.index.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.lucene.document.impl.LuceneRootDocumentBuilder;
import org.hibernate.search.v6poc.backend.lucene.multitenancy.impl.MultiTenancyStrategy;
import org.hibernate.search.v6poc.backend.lucene.orchestration.impl.LuceneIndexWorkOrchestrator;
import org.hibernate.search.v6poc.backend.lucene.work.impl.LuceneIndexWork;
import org.hibernate.search.v6poc.backend.lucene.work.impl.LuceneWorkFactory;
import org.hibernate.search.v6poc.engine.spi.SessionContext;


/**
 * @author Guillaume Smet
 */
class LuceneChangesetIndexWorker extends LuceneIndexWorker implements ChangesetIndexWorker<LuceneRootDocumentBuilder> {

	private final LuceneIndexWorkOrchestrator orchestrator;
	private final List<LuceneIndexWork<?>> works = new ArrayList<>();

	LuceneChangesetIndexWorker(LuceneWorkFactory factory, MultiTenancyStrategy multiTenancyStrategy,
			LuceneIndexWorkOrchestrator orchestrator,
			String indexName, SessionContext sessionContext) {
		super( factory, multiTenancyStrategy, indexName, sessionContext );
		this.orchestrator = orchestrator;
	}

	@Override
	protected void collect(LuceneIndexWork<?> work) {
		works.add( work );
	}

	@Override
	public void prepare() {
		// Nothing to do: we only have to send the works to the orchestrator
	}

	@Override
	public CompletableFuture<?> execute() {
		try {
			CompletableFuture<?> future = orchestrator.submit( works );
			return future;
		}
		finally {
			works.clear();
		}
	}
}
