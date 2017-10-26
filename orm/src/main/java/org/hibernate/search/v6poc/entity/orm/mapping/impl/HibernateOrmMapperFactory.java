/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.mapping.impl;

import org.hibernate.SessionFactory;
import org.hibernate.search.v6poc.entity.orm.model.impl.HibernateOrmIntrospector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMapperFactory;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingDelegate;


/**
 * @author Yoann Rodiere
 */
public final class HibernateOrmMapperFactory extends PojoMapperFactory<HibernateOrmMappingImpl> {

	private SessionFactory sessionFactory;

	public HibernateOrmMapperFactory(SessionFactory sessionFactory) {
		super( new HibernateOrmIntrospector( sessionFactory ), false );
		this.sessionFactory = sessionFactory;
	}

	@Override
	protected HibernateOrmMappingImpl createMapping(PojoMappingDelegate mappingDelegate) {
		return new HibernateOrmMappingImpl( mappingDelegate, sessionFactory );
	}
}
