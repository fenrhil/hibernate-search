/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.cfg.SearchBackendElasticsearchSettings;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.DefaultElasticsearchClientFactory;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.ElasticsearchClientFactory;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.ElasticsearchClientImplementor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.FieldDataType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.IndexType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.NormsType;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.DefaultGsonProvider;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.ES5FieldDataTypeJsonAdapter;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.ES5IndexTypeJsonAdapter;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.ES5NormsTypeJsonAdapter;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.GsonProvider;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.StubElasticsearchWorkFactory;
import org.hibernate.search.v6poc.backend.spi.Backend;
import org.hibernate.search.v6poc.backend.spi.BackendFactory;
import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.cfg.spi.ConfigurationProperty;
import org.hibernate.search.v6poc.engine.spi.BuildContext;

import com.google.gson.GsonBuilder;


/**
 * @author Yoann Rodiere
 */
public class ElasticsearchBackendFactory implements BackendFactory {

	private static final ConfigurationProperty<Boolean> LOG_JSON_PRETTY_PRINTING =
			ConfigurationProperty.forKey( SearchBackendElasticsearchSettings.LOG_JSON_PRETTY_PRINTING )
					.asBoolean()
					.withDefault( SearchBackendElasticsearchSettings.Defaults.LOG_JSON_PRETTY_PRINTING )
					.build();

	@Override
	public Backend<?> create(String name, BuildContext context, ConfigurationPropertySource propertySource) {
		ElasticsearchClientFactory clientFactory = new DefaultElasticsearchClientFactory();

		boolean logPrettyPrinting = LOG_JSON_PRETTY_PRINTING.get( propertySource );
		GsonProvider initialGsonProvider = DefaultGsonProvider.create( GsonBuilder::new, logPrettyPrinting );

		ElasticsearchClientImplementor client = clientFactory.create( propertySource, initialGsonProvider );

		// TODO implement and detect dialects
		// Assume ES5 for now
		GsonProvider dialectSpecificGsonProvider =
				DefaultGsonProvider.create( this::createES5GsonBuilderBase, logPrettyPrinting );
		client.init( dialectSpecificGsonProvider );

		ElasticsearchWorkFactory workFactory = new StubElasticsearchWorkFactory( dialectSpecificGsonProvider );

		return new ElasticsearchBackend( client, name, workFactory );
	}

	private GsonBuilder createES5GsonBuilderBase() {
		return new GsonBuilder()
				.registerTypeAdapter( IndexType.class, new ES5IndexTypeJsonAdapter().nullSafe() )
				.registerTypeAdapter( FieldDataType.class, new ES5FieldDataTypeJsonAdapter().nullSafe() )
				.registerTypeAdapter( NormsType.class, new ES5NormsTypeJsonAdapter().nullSafe() );
	}

}
