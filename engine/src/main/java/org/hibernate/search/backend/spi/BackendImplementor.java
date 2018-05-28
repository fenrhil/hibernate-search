/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.spi;

import org.hibernate.search.backend.Backend;
import org.hibernate.search.backend.document.DocumentElement;
import org.hibernate.search.backend.index.spi.IndexManagerBuilder;
import org.hibernate.search.cfg.ConfigurationPropertySource;
import org.hibernate.search.engine.spi.BuildContext;

/**
 * @author Yoann Rodiere
 */
public interface BackendImplementor<D extends DocumentElement> extends AutoCloseable {

	/**
	 * @return The object that should be exposed as API to users.
	 */
	Backend toAPI();

	/**
	 * Normalize the name of the index, so that we cannot end up with two index names in Hibernate Search
	 * that would target the same physical index.
	 *
	 * @param rawIndexName The index name to be normalized.
	 * @return The normalized index name.
	 */
	String normalizeIndexName(String rawIndexName);

	/**
	 * @param normalizedIndexName The (already {@link #normalizeIndexName(String) normalized}) name of the index
	 * @param multiTenancyEnabled {@code true} if multi-tenancy is enabled for this index, {@code false} otherwise.
	 * @param context The build context
	 * @param propertySource A configuration property source, appropriately masked so that the backend
	 * doesn't need to care about Hibernate Search prefixes (hibernate.search.*, etc.). All the properties
	 * can be accessed at the root.
	 * <strong>CAUTION:</strong> the property key {@code backend} is reserved for use by the engine.
	 * @return A builder for index managers targeting this backend.
	 */
	IndexManagerBuilder<D> createIndexManagerBuilder(String normalizedIndexName, boolean multiTenancyEnabled, BuildContext context,
			ConfigurationPropertySource propertySource);

	@Override
	void close();

}
