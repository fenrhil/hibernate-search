/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.bridge.builtin.impl;

import org.hibernate.search.backend.document.model.dsl.IndexSchemaFieldContext;
import org.hibernate.search.backend.document.model.dsl.IndexSchemaFieldTypedContext;
import org.hibernate.search.entity.pojo.bridge.ValueBridge;

public final class DefaultStringValueBridge implements ValueBridge<String, String> {

	@Override
	public IndexSchemaFieldTypedContext<String> bind(IndexSchemaFieldContext context) {
		return context.asString();
	}

	@Override
	public String toIndexedValue(String value) {
		return value;
	}

}