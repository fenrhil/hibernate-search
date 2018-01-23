/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.index.spi.StreamIndexWorker;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

/**
 * @author Yoann Rodiere
 */
class StreamPojoTypeWorker<D extends DocumentElement, E> extends PojoTypeWorker<D, E, StreamIndexWorker<D>> {

	public StreamPojoTypeWorker(PojoTypeManager<?, E, D> typeManager, PojoSessionContext sessionContext,
			StreamIndexWorker<D> delegate) {
		super( typeManager, sessionContext, delegate );
	}

	public void flush() {
		getDelegate().flush();
	}

	public void optimize() {
		getDelegate().optimize();
	}

}
