/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.dialect.protocol.impl;

import org.hibernate.search.backend.elasticsearch.gson.spi.GsonProvider;
import org.hibernate.search.backend.elasticsearch.search.impl.Elasticsearch7JsonSyntaxHelper;
import org.hibernate.search.backend.elasticsearch.search.impl.ElasticsearchJsonSyntaxHelper;
import org.hibernate.search.backend.elasticsearch.search.query.impl.Elasticsearch7SearchResultExtractorFactory;
import org.hibernate.search.backend.elasticsearch.search.query.impl.ElasticsearchSearchResultExtractorFactory;
import org.hibernate.search.backend.elasticsearch.work.builder.factory.impl.Elasticsearch7WorkBuilderFactory;
import org.hibernate.search.backend.elasticsearch.work.builder.factory.impl.ElasticsearchWorkBuilderFactory;

import com.google.gson.GsonBuilder;

/**
 * The protocol dialect for Elasticsearch 7.0.
 */
public class Elasticsearch70ProtocolDialect implements ElasticsearchProtocolDialect {

	@Override
	public GsonBuilder createGsonBuilderBase() {
		return new GsonBuilder();
	}

	@Override
	public ElasticsearchJsonSyntaxHelper createJsonSyntaxHelper() {
		return new Elasticsearch7JsonSyntaxHelper();
	}

	@Override
	public ElasticsearchWorkBuilderFactory createWorkBuilderFactory(GsonProvider gsonProvider) {
		return new Elasticsearch7WorkBuilderFactory( gsonProvider );
	}

	@Override
	public ElasticsearchSearchResultExtractorFactory createSearchResultExtractorFactory() {
		return new Elasticsearch7SearchResultExtractorFactory();
	}
}