/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import org.hibernate.search.v6poc.backend.document.impl.DeferredInitializationIndexFieldReference;
import org.hibernate.search.v6poc.backend.document.spi.IndexFieldReference;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.ElasticsearchTypedFieldModelContext;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.PropertyMapping;

/**
 * @author Yoann Rodiere
 */
public abstract class AbstractElasticsearchTypedFieldModelContext<T>
		implements ElasticsearchTypedFieldModelContext<T>, ElasticsearchIndexModelNodeContributor<PropertyMapping> {

	private DeferredInitializationIndexFieldReference<T> reference = new DeferredInitializationIndexFieldReference<>();

	@Override
	public IndexFieldReference<T> asReference() {
		return reference;
	}

	@Override
	public PropertyMapping contribute(ElasticsearchFieldModelCollector collector) {
		return contribute( reference, collector );
	}

	protected abstract PropertyMapping contribute(DeferredInitializationIndexFieldReference<T> reference,
			ElasticsearchFieldModelCollector collector);

}
