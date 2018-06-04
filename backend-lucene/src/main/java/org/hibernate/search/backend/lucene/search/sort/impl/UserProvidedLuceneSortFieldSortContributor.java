/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.sort.impl;

import org.apache.lucene.search.SortField;
import org.hibernate.search.search.sort.spi.SearchSortContributor;


class UserProvidedLuceneSortFieldSortContributor implements SearchSortContributor<LuceneSearchSortCollector> {

	private final SortField luceneSortField;

	UserProvidedLuceneSortFieldSortContributor(SortField luceneSortField) {
		this.luceneSortField = luceneSortField;
	}

	@Override
	public void contribute(LuceneSearchSortCollector collector) {
		collector.collectSortField( luceneSortField );
	}
}
