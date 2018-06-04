/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.index.spi;

/**
 * @author Yoann Rodiere
 */
public interface IndexWorker<D> {

	void add(DocumentReferenceProvider documentReferenceProvider, DocumentContributor<D> documentContributor);

	void update(DocumentReferenceProvider documentReferenceProvider, DocumentContributor<D> documentContributor);

	void delete(DocumentReferenceProvider documentReferenceProvider);

}
