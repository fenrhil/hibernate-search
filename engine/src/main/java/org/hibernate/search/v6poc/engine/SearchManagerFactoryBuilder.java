/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.engine;

import java.util.Properties;

import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingContributor;

/**
 * @author Yoann Rodiere
 */
public interface SearchManagerFactoryBuilder {

	SearchManagerFactoryBuilder setProperty(String name, String value);

	SearchManagerFactoryBuilder setProperties(Properties properties);

	SearchManagerFactoryBuilder addMapping(MappingContributor<?> mappingContributor);

	SearchManagerFactory build();

}
