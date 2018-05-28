/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.multitenancy.impl;

import java.lang.invoke.MethodHandles;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.search.Query;
import org.hibernate.search.backend.lucene.document.impl.LuceneIndexEntry;
import org.hibernate.search.backend.lucene.util.impl.LuceneFields;
import org.hibernate.search.backend.lucene.logging.impl.Log;
import org.hibernate.search.backend.lucene.search.impl.LuceneQueries;
import org.hibernate.search.backend.lucene.work.impl.QueryBasedDeleteEntryLuceneWork;
import org.hibernate.search.backend.lucene.work.impl.QueryBasedUpdateEntryLuceneWork;
import org.hibernate.search.backend.spi.BackendImplementor;
import org.hibernate.search.util.impl.common.LoggerFactory;

public class DiscriminatorMultiTenancyStrategyImpl implements MultiTenancyStrategy {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	@Override
	public boolean isMultiTenancySupported() {
		return true;
	}

	@Override
	public void contributeToIndexedDocument(Document document, String tenantId) {
		document.add( new StringField( LuceneFields.tenantIdFieldName(), tenantId, Store.YES ) );
	}

	@Override
	public Query decorateLuceneQuery(Query originalLuceneQuery, String tenantId) {
		return LuceneQueries.wrapWithDiscriminatorTenantIdQuery( originalLuceneQuery, tenantId );
	}

	@Override
	public QueryBasedUpdateEntryLuceneWork createUpdateEntryLuceneWork(String indexName, String tenantId, String id, LuceneIndexEntry indexEntry) {
		return new QueryBasedUpdateEntryLuceneWork( indexName, tenantId, id, indexEntry );
	}

	@Override
	public QueryBasedDeleteEntryLuceneWork createDeleteEntryLuceneWork(String indexName, String tenantId, String id) {
		return new QueryBasedDeleteEntryLuceneWork( indexName, tenantId, id );
	}

	@Override
	public void checkTenantId(BackendImplementor<?> backend, String tenantId) {
		if ( tenantId == null ) {
			throw log.multiTenancyEnabledButNoTenantIdProvided( backend );
		}
	}
}
