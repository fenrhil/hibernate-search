/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.javabean.impl;

import org.hibernate.search.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.entity.javabean.JavaBeanMapping;
import org.hibernate.search.entity.javabean.JavaBeanMappingInitiator;
import org.hibernate.search.entity.javabean.mapping.impl.JavaBeanMappingFactory;
import org.hibernate.search.entity.javabean.mapping.impl.JavaBeanMappingKey;
import org.hibernate.search.entity.javabean.model.impl.JavaBeanBootstrapIntrospector;
import org.hibernate.search.entity.pojo.mapping.spi.PojoMappingInitiatorImpl;

public final class JavaBeanMappingInitiatorImpl extends PojoMappingInitiatorImpl<JavaBeanMapping>
		implements JavaBeanMappingInitiator {

	public JavaBeanMappingInitiatorImpl(SearchMappingRepositoryBuilder mappingRepositoryBuilder,
			JavaBeanBootstrapIntrospector introspector,
			boolean annotatedTypeDiscoveryEnabled, boolean multiTenancyEnabled) {
		super(
				mappingRepositoryBuilder, new JavaBeanMappingKey(),
				new JavaBeanMappingFactory(),
				introspector, false,
				multiTenancyEnabled
		);
		if ( annotatedTypeDiscoveryEnabled ) {
			enableAnnotatedTypeDiscovery();
		}
	}

}
