/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.mapping.definition.programmatic;

import org.hibernate.search.entity.pojo.bridge.mapping.BridgeBuilder;
import org.hibernate.search.entity.pojo.bridge.IdentifierBridge;

/**
 * @author Yoann Rodiere
 */
public interface PropertyDocumentIdMappingContext extends PropertyMappingContext {

	PropertyDocumentIdMappingContext identifierBridge(String bridgeName);

	PropertyDocumentIdMappingContext identifierBridge(Class<? extends IdentifierBridge<?>> bridgeClass);

	PropertyDocumentIdMappingContext identifierBridge(String bridgeName, Class<? extends IdentifierBridge<?>> bridgeClass);

	PropertyDocumentIdMappingContext identifierBridge(BridgeBuilder<? extends IdentifierBridge<?>> builder);

}
