/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.predicate.impl;

import org.hibernate.search.search.SearchPredicate;
import org.hibernate.search.search.predicate.spi.SearchPredicateContributor;

import com.google.gson.JsonObject;

class ElasticsearchSearchPredicate
		implements SearchPredicate, SearchPredicateContributor<ElasticsearchSearchPredicateCollector> {

	private final JsonObject jsonObject;

	ElasticsearchSearchPredicate(JsonObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public void contribute(ElasticsearchSearchPredicateCollector collector) {
		collector.collectPredicate( jsonObject );
	}

}
