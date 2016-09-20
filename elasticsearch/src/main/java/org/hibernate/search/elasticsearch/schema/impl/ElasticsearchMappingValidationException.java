/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.schema.impl;

import org.hibernate.search.exception.SearchException;


/**
 * @author Yoann Rodiere
 */
public class ElasticsearchMappingValidationException extends SearchException {

	public ElasticsearchMappingValidationException() {
	}

	public ElasticsearchMappingValidationException(String message) {
		super( message );
	}

	public ElasticsearchMappingValidationException(String message, Throwable cause) {
		super( message, cause );
	}

	public ElasticsearchMappingValidationException(Throwable cause) {
		super( cause );
	}

}
