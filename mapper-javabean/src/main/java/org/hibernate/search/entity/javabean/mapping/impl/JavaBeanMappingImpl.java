/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.javabean.mapping.impl;

import org.hibernate.search.entity.javabean.JavaBeanMapping;
import org.hibernate.search.entity.javabean.JavaBeanSearchManagerBuilder;
import org.hibernate.search.entity.pojo.mapping.PojoSearchManager;
import org.hibernate.search.entity.pojo.mapping.spi.PojoMappingDelegate;
import org.hibernate.search.entity.pojo.mapping.spi.PojoMappingImpl;

public class JavaBeanMappingImpl extends PojoMappingImpl<JavaBeanMapping> implements JavaBeanMapping {

	JavaBeanMappingImpl(PojoMappingDelegate mappingDelegate) {
		super( mappingDelegate );
	}

	@Override
	public JavaBeanMapping toAPI() {
		return this;
	}

	@Override
	public PojoSearchManager createSearchManager() {
		return createSearchManagerBuilder().build();
	}

	@Override
	public JavaBeanSearchManagerBuilder createSearchManagerWithOptions() {
		return createSearchManagerBuilder();
	}

	private JavaBeanSearchManagerBuilder createSearchManagerBuilder() {
		return new JavaBeanSearchManagerImpl.Builder( getDelegate() );
	}
}
