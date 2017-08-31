/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.mapping;

import org.hibernate.search.v6poc.entity.orm.mapping.impl.HibernateOrmMapperImplementor;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMapperImpl;

/**
 * @author Yoann Rodiere
 */
/*
 * TODO create a Hibernate ORM specific mapper, with the following additions:
 *  1. When processing annotations, use @Id as a fallback when no document ID was found
 *  2. Save additional information regarding containedIn, and make it available in the mapping
 *  3. Use a specific introspector that will comply with Hibernate ORM's access mode
 *  4. When the @DocumentId is the @Id, use the provided ID in priority and only if it's missing, unproxy the entity and get the ID;
 *     when the @DocumentId is NOT the @Id, always ignore the provided ID. See org.hibernate.search.engine.impl.WorkPlan.PerClassWork.extractProperId(Work)
 *  5. And more?
 */
public class HibernateOrmMapper extends PojoMapperImpl {

	public static final HibernateOrmMapper INSTANCE = new HibernateOrmMapper();

	public static HibernateOrmMapper get() {
		return INSTANCE;
	}

	private HibernateOrmMapper() {
		super( HibernateOrmMapperImplementor.get() );
	}

}
