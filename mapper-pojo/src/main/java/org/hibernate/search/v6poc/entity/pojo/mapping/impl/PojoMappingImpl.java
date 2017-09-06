/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.engine.SearchManagerBuilder;
import org.hibernate.search.v6poc.entity.mapping.spi.Mapping;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchManager;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoProxyIntrospector;


/**
 * @author Yoann Rodiere
 */
public class PojoMappingImpl implements Mapping<SearchManagerBuilder<PojoSearchManager>> {

	private final PojoProxyIntrospector introspector;
	private final PojoTypeManagerContainer typeManagers;

	public PojoMappingImpl(PojoProxyIntrospector introspector, PojoTypeManagerContainer typeManagers) {
		this.introspector = introspector;
		this.typeManagers = typeManagers;
	}

	@Override
	public SearchManagerBuilder<PojoSearchManager> createManagerBuilder() {
		return new PojoSearchManagerImpl.Builder( introspector, typeManagers );
	}

}
