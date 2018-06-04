/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.definition.programmatic.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BuildContext;
import org.hibernate.search.entity.mapping.building.spi.MappingConfigurationCollector;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.entity.pojo.mapping.definition.programmatic.ProgrammaticMappingDefinition;
import org.hibernate.search.entity.pojo.mapping.definition.programmatic.TypeMappingContext;
import org.hibernate.search.entity.pojo.mapping.spi.PojoMappingConfigurationContributor;
import org.hibernate.search.entity.pojo.model.spi.PojoBootstrapIntrospector;

public class ProgrammaticMappingDefinitionImpl
		implements ProgrammaticMappingDefinition, PojoMappingConfigurationContributor {

	private final PojoBootstrapIntrospector introspector;

	// Use a LinkedHashMap for deterministic iteration
	private final Map<Class<?>, TypeMappingContextImpl> entities = new LinkedHashMap<>();

	public ProgrammaticMappingDefinitionImpl(PojoBootstrapIntrospector introspector) {
		this.introspector = introspector;
	}

	@Override
	public void configure(BuildContext buildContext, ConfigurationPropertySource propertySource,
			MappingConfigurationCollector<PojoTypeMetadataContributor> configurationCollector) {
		for ( TypeMappingContextImpl contextImpl : entities.values() ) {
			contextImpl.configure( buildContext, propertySource, configurationCollector );
		}
	}

	@Override
	public TypeMappingContext type(Class<?> clazz) {
		return entities.computeIfAbsent( clazz, c -> new TypeMappingContextImpl( introspector.getTypeModel( c ) ) );
	}

}
