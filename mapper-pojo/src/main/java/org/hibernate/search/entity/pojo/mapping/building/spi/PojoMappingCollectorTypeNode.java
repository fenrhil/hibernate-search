/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.building.spi;

import org.hibernate.search.entity.pojo.bridge.RoutingKeyBridge;
import org.hibernate.search.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.entity.pojo.model.spi.PropertyHandle;

public interface PojoMappingCollectorTypeNode extends PojoMappingCollector {

	void bridge(BridgeBuilder<? extends TypeBridge> builder);

	void routingKeyBridge(BridgeBuilder<? extends RoutingKeyBridge> reference);

	PojoMappingCollectorPropertyNode property(PropertyHandle propertyHandle);

}
