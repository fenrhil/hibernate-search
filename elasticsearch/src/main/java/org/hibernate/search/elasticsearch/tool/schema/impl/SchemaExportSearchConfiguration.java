/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.tool.schema.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.search.cfg.SearchMapping;
import org.hibernate.search.cfg.spi.SearchConfigurationBase;
import org.hibernate.search.engine.service.classloading.spi.ClassLoaderService;
import org.hibernate.search.engine.service.spi.Service;


/**
 * @author Yoann Rodiere
 */
public class SchemaExportSearchConfiguration extends SearchConfigurationBase {

	@Override
	public Iterator<Class<?>> getClassMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getClassMapping(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProperty(String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReflectionManager getReflectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchMapping getProgrammaticMapping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends Service>, Object> getProvidedServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoaderService getClassLoaderService() {
		// TODO Auto-generated method stub
		return null;
	}

}
