/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.javabean;

import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.entity.javabean.mapping.impl.JavaBeanMapperFactory;
import org.hibernate.search.v6poc.entity.javabean.mapping.impl.JavaBeanMappingImpl;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingContributorImpl;


/**
 * @author Yoann Rodiere
 */
public final class JavaBeanMappingContributor extends PojoMappingContributorImpl<JavaBeanMapping, JavaBeanMappingImpl> {

	public JavaBeanMappingContributor(SearchMappingRepositoryBuilder mappingRepositoryBuilder) {
		super( mappingRepositoryBuilder, JavaBeanMapperFactory.get() );
	}

	@Override
	protected JavaBeanMapping toReturnType(JavaBeanMappingImpl mapping) {
		return mapping;
	}
}
