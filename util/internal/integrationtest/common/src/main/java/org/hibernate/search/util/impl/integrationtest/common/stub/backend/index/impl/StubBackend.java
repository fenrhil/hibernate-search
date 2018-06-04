/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.impl;

import org.hibernate.search.backend.Backend;
import org.hibernate.search.backend.index.spi.IndexManagerBuilder;
import org.hibernate.search.backend.spi.BackendImplementor;
import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BuildContext;
import org.hibernate.search.util.AssertionFailure;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.StubBackendBehavior;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.StubBackendUtils;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.document.impl.StubDocumentElement;

public class StubBackend implements BackendImplementor<StubDocumentElement>, Backend {

	private final String name;

	StubBackend(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return StubBackend.class.getSimpleName() + "[" + name + "]";
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		throw new AssertionFailure( getClass().getName() + " cannot be unwrapped" );
	}

	@Override
	public Backend toAPI() {
		return this;
	}

	public StubBackendBehavior getBehavior() {
		return StubBackendBehavior.get( name );
	}

	@Override
	public String normalizeIndexName(String rawIndexName) {
		return StubBackendUtils.normalizeIndexName( rawIndexName );
	}

	@Override
	public IndexManagerBuilder<StubDocumentElement> createIndexManagerBuilder(String name, boolean isMultiTenancyEnabled, BuildContext context,
			ConfigurationPropertySource propertySource) {
		return new StubIndexManagerBuilder( this, name );
	}

	@Override
	public void close() {
		// No-op
	}
}
