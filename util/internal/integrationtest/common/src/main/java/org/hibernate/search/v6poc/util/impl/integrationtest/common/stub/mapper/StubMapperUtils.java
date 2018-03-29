/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.mapper;

import org.hibernate.search.v6poc.backend.index.spi.DocumentReferenceProvider;

public final class StubMapperUtils {

	private StubMapperUtils() {
	}

	public static DocumentReferenceProvider referenceProvider(String identifier) {
		return referenceProvider( identifier, null );
	}

	public static DocumentReferenceProvider referenceProvider(String identifier, String routingKey) {
		return new StubDocumentReferenceProvider( identifier, routingKey );
	}
}
