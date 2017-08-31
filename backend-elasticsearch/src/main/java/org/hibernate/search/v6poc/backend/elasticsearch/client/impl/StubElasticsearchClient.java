/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.client.impl;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author Yoann Rodiere
 */
public class StubElasticsearchClient implements ElasticsearchClient {

	private static final Log log = LoggerFactory.make( Log.class );

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private static final Deque<Request> requests = new LinkedList<>();

	public static Map<String, List<Request>> drainRequestsByIndex() {
		Map<String, List<Request>> result = new HashMap<>();
		Request next = requests.pollFirst();
		while ( next != null ) {
			result.computeIfAbsent( next.getParameters().get( "indexName" ), ignored -> new LinkedList<>() ).add( next );
			next = requests.pollFirst();
		}
		return result;
	}

	public static class Request {
		private final String host;
		private final String workType;
		private final Map<String, String> parameters;
		private final String body;

		public Request(String host, String workType, Map<String, String> parameters, String body) {
			super();
			this.host = host;
			this.workType = workType;
			this.parameters = parameters;
			this.body = body;
		}

		public String getHost() {
			return host;
		}

		public String getWorkType() {
			return workType;
		}

		public Map<String, String> getParameters() {
			return parameters;
		}

		public String getBody() {
			return body;
		}
	}

	private final String host;

	public StubElasticsearchClient(String host) {
		this.host = host;
	}

	public <T> CompletableFuture<T> execute(String workType, Map<String, String> parameters, JsonObject body) {
		String bodyAsString = body == null ? null : gson.toJson( body );
		log.executingWork( host, workType, parameters, bodyAsString );
		requests.addLast( new Request( host, workType, parameters, bodyAsString ) );
		return CompletableFuture.completedFuture( null );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + host + "]";
	}

}
