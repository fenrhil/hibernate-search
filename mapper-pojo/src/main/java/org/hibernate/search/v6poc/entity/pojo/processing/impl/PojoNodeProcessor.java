/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;

public interface PojoNodeProcessor<T> extends AutoCloseable {

	void process(DocumentElement target, T source);

	@Override
	default void close() {
	}

	static <T> PojoNodeProcessor<T> noOp() {
		return (ignored1, ignored2) -> { };
	}

}
