/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.mapping.impl;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.v6poc.entity.orm.mapping.HibernateOrmMapping;
import org.hibernate.search.v6poc.entity.orm.mapping.HibernateOrmSearchManager;
import org.hibernate.search.v6poc.entity.orm.mapping.HibernateOrmSearchManagerBuilder;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingDelegate;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingImpl;

public class HibernateOrmMappingImpl extends PojoMappingImpl
		implements HibernateOrmMapping {

	private final SessionFactory sessionFactory;

	public HibernateOrmMappingImpl(PojoMappingDelegate mappingDelegate, SessionFactory sessionFactory) {
		super( mappingDelegate );
		this.sessionFactory = sessionFactory;
	}

	@Override
	public HibernateOrmSearchManager createSearchManager(EntityManager entityManager) {
		return createSearchManagerBuilder( entityManager ).build();
	}

	@Override
	public HibernateOrmSearchManagerBuilder createSearchManagerWithOptions(EntityManager entityManager) {
		return createSearchManagerBuilder( entityManager );
	}

	private HibernateOrmSearchManagerBuilder createSearchManagerBuilder(EntityManager entityManager) {
		SessionImplementor sessionImplementor = entityManager.unwrap( SessionImplementor.class );
		// TODO check that the session refers to the same session factory used when building the mapping
		return new HibernateOrmSearchManagerImpl.Builder( getDelegate(), sessionImplementor );
	}
}
