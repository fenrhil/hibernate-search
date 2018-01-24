/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge;

import org.hibernate.search.v6poc.backend.document.model.FieldModelContext;
import org.hibernate.search.v6poc.backend.document.model.TypedFieldModelContext;

/**
 * A bridge between a POJO property of type {@code T} and an index field of type {@code R}.
 * <p>
 * The {@code FunctionBridge} interface is a simpler version of {@link Bridge},
 * in which a given value can only be mapped to a single field, in particular.
 *
 * @param <T> The type of values on the POJO side of the bridge.
 * @param <R> The type of values on the index side of the bridge.
 *
 * @author Yoann Rodiere
 */
public interface FunctionBridge<T, R> extends AutoCloseable {

	/**
	 * Bind this bridge instance to the given index field model.
	 * <p>
	 * This method is called exactly once for each bridge instance, before any other method.
	 * It allows the bridge to declare its expectations regarding the index field (type, storage options, ...).
	 *
	 * @param context An entry point to declaring expectations and retrieving accessors to the index schema.
	 * @return The result provided by {@code context} after setting the expectations regarding the index field
	 * (for instance {@code return context.asString()}). {@code null} to let Hibernate Search derive the expectations
	 * from the {@code FunctionBridge}'s generic type parameters.
	 */
	default TypedFieldModelContext<R> bind(FieldModelContext context) {
		return null; // Auto-detect the return type and use default encoding
	}

	/**
	 * Transform the given POJO property value to the value of the indexed field.
	 *
	 * @param propertyValue The POJO property value to be transformed.
	 * @return The value of the indexed field.
	 */
	R toIndexedValue(T propertyValue);

	/**
	 * Transform the given indexed field value back to the value of the POJO property,
	 * or to any implementation-defined value to be returned in projections on the POJO property.
	 * <p>
	 * For instance, a {@code FunctionBridge} indexing JPA entities by putting their identifier in a field
	 * might not be able to retrieve the entity, so it could just return the identifier as-is.
	 *
	 * @param fieldValue The field value to be transformed.
	 * @return The value returned in projections on the POJO property.
	 */
	default Object fromIndexedValue(R fieldValue) {
		return fieldValue;
	}

	/**
	 * Close any resource before the bridge is discarded.
	 */
	@Override
	default void close() {
	}

}
