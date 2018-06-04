/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.mapping.building.spi;

import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BuildContext;
import org.hibernate.search.entity.mapping.spi.MappingKey;

/**
 * An object responsible for initiating a mapping by contributing its basic configuration (indexed types, type metadata),
 * then creating the mapper based on the configuration processed by the engine.
 *
 * @param <C> The Java type of type metadata contributors
 * @param <M> The Java type of the produced mapping
 */
public interface MappingInitiator<C, M> {

	MappingKey<M> getMappingKey();

	void configure(BuildContext buildContext, ConfigurationPropertySource propertySource,
			MappingConfigurationCollector<C> configurationCollector);

	Mapper<M> createMapper(BuildContext buildContext, ConfigurationPropertySource propertySource,
			TypeMetadataContributorProvider<C> contributorProvider);

}
