/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.building.spi;

import java.util.Set;

import org.hibernate.search.backend.document.model.dsl.ObjectFieldStorage;
import org.hibernate.search.entity.mapping.building.spi.FieldModelContributor;
import org.hibernate.search.entity.pojo.bridge.ValueBridge;
import org.hibernate.search.entity.pojo.bridge.mapping.BridgeBuilder;

public interface PojoMappingCollectorValueNode extends PojoMappingCollector {

	void valueBridge(BridgeBuilder<? extends ValueBridge<?, ?>> builder,
			String relativeFieldName, FieldModelContributor fieldModelContributor);

	void indexedEmbedded(String relativePrefix, ObjectFieldStorage storage, Integer maxDepth, Set<String> includePaths);

}
