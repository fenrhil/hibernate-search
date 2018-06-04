/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.impl;

import org.hibernate.search.backend.spi.BackendImplementor;
import org.hibernate.search.backend.spi.BackendFactory;
import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BuildContext;

public class StubBackendFactory implements BackendFactory {
	@Override
	public BackendImplementor<?> create(String name, BuildContext context, ConfigurationPropertySource propertySource) {
		return new StubBackend( name );
	}
}
