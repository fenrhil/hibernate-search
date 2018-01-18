/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge;

import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoState;

/**
 * @author Yoann Rodiere
 */
public interface RoutingKeyBridge extends AutoCloseable {

	void bind(PojoModelElement pojoModelElement);

	String toRoutingKey(String tenantIdentifier, Object entityIdentifier, PojoState source);

	@Override
	default void close() {
	}

}
