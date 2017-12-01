/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.function.Supplier;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoProxyIntrospector;

public class CachingCastingEntitySupplier<E> implements Supplier<E> {

	private final Class<E> entityType;
	private final PojoProxyIntrospector proxyIntrospector;
	private final Object potentiallyProxiedEntity;

	private E unproxiedEntity;

	public CachingCastingEntitySupplier(
			Class<E> entityType,
			PojoProxyIntrospector proxyIntrospector,
			Object potentiallyProxiedEntity) {
		this.entityType = entityType;
		this.proxyIntrospector = proxyIntrospector;
		this.potentiallyProxiedEntity = potentiallyProxiedEntity;
	}

	@Override
	public E get() {
		if ( unproxiedEntity == null ) {
			unproxiedEntity = entityType.cast( proxyIntrospector.unproxy( potentiallyProxiedEntity ) );
		}
		return unproxiedEntity;
	}
}
