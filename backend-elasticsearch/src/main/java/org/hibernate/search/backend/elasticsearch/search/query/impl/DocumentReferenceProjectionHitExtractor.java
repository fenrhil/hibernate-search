/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.query.impl;

import org.hibernate.search.backend.elasticsearch.multitenancy.impl.MultiTenancyStrategy;
import org.hibernate.search.engine.spi.SessionContext;
import org.hibernate.search.search.query.spi.HitAggregator;
import org.hibernate.search.search.query.spi.ProjectionHitCollector;
import org.hibernate.search.search.query.spi.SearchQueryFactory;

import com.google.gson.JsonObject;

/**
 * A hit extractor used when projecting on the document reference,
 * when we don't want the reference to be transformed,
 * but we just want the raw reference to be inserted into the projection.
 *
 * @see org.hibernate.search.search.ProjectionConstants#DOCUMENT_REFERENCE
 * @see SearchQueryFactory#asProjections(SessionContext, HitAggregator, String...)
 */
class DocumentReferenceProjectionHitExtractor extends AbstractDocumentReferenceHitExtractor<ProjectionHitCollector> {

	DocumentReferenceProjectionHitExtractor(MultiTenancyStrategy multiTenancyStrategy) {
		super( multiTenancyStrategy );
	}

	@Override
	public void extract(ProjectionHitCollector collector, JsonObject responseBody, JsonObject hit) {
		collector.collectProjection( extractDocumentReference( hit ) );
	}

}
