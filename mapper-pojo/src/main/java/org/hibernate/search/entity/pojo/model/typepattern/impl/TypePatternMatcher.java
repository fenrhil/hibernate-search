/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.model.typepattern.impl;

import java.util.Optional;

import org.hibernate.search.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.entity.pojo.model.spi.PojoBootstrapIntrospector;

/**
 * A pattern-matching implementation for generic types.
 * <p>
 * For example, such a pattern could be described with the expression {@code Collection<T> => T}.
 * It would only match against {@code Collection} and its subclasses,
 * and would return the resolved type for parameter {@code T} in the event of a match.
 */
public interface TypePatternMatcher {

	/**
	 * Attempts to match a given type against this pattern,
	 * and if matched, returns an upper bound of the resulting type.
	 *
	 * @param introspector An introspector to use for reflection, mainly for {@link PojoBootstrapIntrospector#getGenericTypeModel(Class)}
	 * @param typeToMatch A type to be matched against
	 * @return The resulting type if there was a match, or an empty {@link Optional} otherwise.
	 */
	Optional<? extends PojoGenericTypeModel<?>> match(
			PojoBootstrapIntrospector introspector, PojoGenericTypeModel<?> typeToMatch);

}
