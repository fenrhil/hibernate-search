/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.spi;

import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BuildContext;
import org.hibernate.search.entity.mapping.building.spi.MappingConfigurationCollector;
import org.hibernate.search.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;

public interface PojoMappingConfigurationContributor {

	void configure(BuildContext buildContext, ConfigurationPropertySource propertySource,
			MappingConfigurationCollector<PojoTypeMetadataContributor> configurationCollector);

}
