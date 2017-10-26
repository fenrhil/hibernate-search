/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.cfg.spi;

import java.util.Optional;
import java.util.Properties;

import org.hibernate.search.v6poc.cfg.impl.EmptyConfigurationPropertySource;
import org.hibernate.search.v6poc.cfg.impl.FallbackConfigurationPropertySource;
import org.hibernate.search.v6poc.cfg.impl.MaskedConfigurationPropertySource;
import org.hibernate.search.v6poc.cfg.impl.PropertiesConfigurationPropertySource;

public interface ConfigurationPropertySource {

	Optional<?> get(String key);

	default ConfigurationPropertySource withMask(String mask) {
		return new MaskedConfigurationPropertySource( this, mask );
	}

	default ConfigurationPropertySource withFallback(ConfigurationPropertySource fallback) {
		return new FallbackConfigurationPropertySource( this, fallback );
	}

	static ConfigurationPropertySource fromProperties(Properties properties) {
		return new PropertiesConfigurationPropertySource( properties );
	}

	static ConfigurationPropertySource empty() {
		return EmptyConfigurationPropertySource.get();
	}
}
