/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.entity.model.SearchModel;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;

/**
 * @author Yoann Rodiere
 */
public interface Bridge extends AutoCloseable {

	void contribute(IndexSchemaElement indexSchemaElement, PojoModelElement bridgedPojoModelElement,
			SearchModel searchModel);

	void write(DocumentElement target, PojoElement source);

	@Override
	default void close() {
	}

}
