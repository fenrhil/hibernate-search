/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.impl;

import java.util.Map;

import org.hibernate.search.backend.Backend;
import org.hibernate.search.backend.index.spi.IndexManagerImplementor;
import org.hibernate.search.backend.spi.BackendImplementor;
import org.hibernate.search.engine.SearchMappingRepository;
import org.hibernate.search.engine.spi.BeanResolver;
import org.hibernate.search.entity.mapping.spi.MappingImplementor;
import org.hibernate.search.entity.mapping.spi.MappingKey;
import org.hibernate.search.util.SearchException;
import org.hibernate.search.util.impl.common.Closer;

public class SearchMappingRepositoryImpl implements SearchMappingRepository {

	private final BeanResolver beanResolver;

	private final Map<MappingKey<?>, MappingImplementor<?>> mappings;
	private final Map<String, BackendImplementor<?>> backends;
	private final Map<String, IndexManagerImplementor<?>> indexManagers;

	SearchMappingRepositoryImpl(BeanResolver beanResolver,
			Map<MappingKey<?>, MappingImplementor<?>> mappings,
			Map<String, BackendImplementor<?>> backends,
			Map<String, IndexManagerImplementor<?>> indexManagers) {
		this.beanResolver = beanResolver;
		this.mappings = mappings;
		this.backends = backends;
		this.indexManagers = indexManagers;
	}

	@Override
	public <M> M getMapping(MappingKey<M> mappingKey) {
		// See SearchMappingRepositoryBuilderImpl: we are sure that, if there is a mapping, it implements MappingImplementor<M>
		@SuppressWarnings("unchecked")
		MappingImplementor<M> mappingImplementor = (MappingImplementor<M>) mappings.get( mappingKey );
		if ( mappingImplementor == null ) {
			throw new SearchException( "No mapping registered for mapping key '" + mappingKey + "'" );
		}
		return mappingImplementor.toAPI();
	}

	@Override
	public Backend getBackend(String backendName) {
		BackendImplementor<?> backend = backends.get( backendName );
		if ( backend == null ) {
			throw new SearchException( "No backend registered for backend name '" + backendName + "'" );
		}
		return backend.toAPI();
	}

	@Override
	public void close() {
		try ( Closer<RuntimeException> closer = new Closer<>() ) {
			closer.pushAll( MappingImplementor::close, mappings.values() );
			closer.pushAll( IndexManagerImplementor::close, indexManagers.values() );
			closer.pushAll( BackendImplementor::close, backends.values() );
			closer.pushAll( BeanResolver::close, beanResolver );
		}
	}
}
