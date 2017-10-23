/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.engine;

import org.hibernate.search.v6poc.engine.impl.SearchManagerFactoryBuilderImpl;
import org.hibernate.search.v6poc.entity.mapping.MappingKey;

/**
 * @author Yoann Rodiere
 */
public interface SearchManagerFactory extends AutoCloseable {

	<T extends SearchManager> T createSearchManager(MappingKey<T, ?> mappingKey);

	<B extends SearchManagerBuilder<?>> B withOptions(MappingKey<?, B> mappingKey);

	@Override
	default void close() {
	}

	static SearchManagerFactoryBuilder builder() {
		return new SearchManagerFactoryBuilderImpl();
	}
}
