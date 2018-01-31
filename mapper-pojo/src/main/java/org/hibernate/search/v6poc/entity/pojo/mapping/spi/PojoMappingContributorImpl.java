/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.spi;

import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingImplementor;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoMapping;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoMappingContributor;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMapperFactory;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.AnnotationMappingDefinition;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.impl.AnnotationMappingDefinitionImpl;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.ProgrammaticMappingDefinition;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.impl.ProgrammaticMappingDefinitionImpl;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoIntrospector;

/**
 * @author Yoann Rodiere
 */
public abstract class PojoMappingContributorImpl<M extends PojoMapping, MI extends MappingImplementor>
		implements PojoMappingContributor<M> {

	private final SearchMappingRepositoryBuilder mappingRepositoryBuilder;

	private final PojoMapperFactory<MI> mapperFactory;

	private final PojoIntrospector introspector;

	private AnnotationMappingDefinitionImpl annotationMappingDefinition;

	protected PojoMappingContributorImpl(SearchMappingRepositoryBuilder mappingRepositoryBuilder,
			PojoMapperFactory<MI> mapperFactory,
			PojoIntrospector introspector) {
		this.mappingRepositoryBuilder = mappingRepositoryBuilder;
		this.mapperFactory = mapperFactory;
		this.introspector = introspector;
	}

	@Override
	public ProgrammaticMappingDefinition programmaticMapping() {
		ProgrammaticMappingDefinitionImpl definition = new ProgrammaticMappingDefinitionImpl( mapperFactory, introspector );
		mappingRepositoryBuilder.addMapping( definition );
		return definition;
	}

	@Override
	public AnnotationMappingDefinition annotationMapping() {
		/*
		 * Make sure to re-use the same mapping, so as not to parse annotations on a given type twice,
		 * which would lead to duplicate field definitions.
		 */
		if ( annotationMappingDefinition == null ) {
			AnnotationMappingDefinitionImpl definition = new AnnotationMappingDefinitionImpl(
					mapperFactory, introspector
			);
			mappingRepositoryBuilder.addMapping( definition );
			annotationMappingDefinition = definition;
		}
		return annotationMappingDefinition;
	}

	@Override
	public M getResult() {
		return toReturnType( mappingRepositoryBuilder.getBuiltResult().getMapping( mapperFactory ) );
	}

	protected abstract M toReturnType(MI mapping);
}
