/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.backend.document.spi.DocumentState;
import org.hibernate.search.v6poc.backend.index.spi.StreamIndexWorker;

/**
 * @author Yoann Rodiere
 */
class StreamPojoTypeWorker<D extends DocumentState> extends PojoTypeWorker<D, StreamIndexWorker<D>> {

	public StreamPojoTypeWorker(PojoTypeManager<?, ?, D> typeManager, StreamIndexWorker<D> delegate) {
		super( typeManager, delegate );
	}

	public void flush() {
		getDelegate().flush();
	}

	public void optimize() {
		getDelegate().optimize();
	}

}
