/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.search.v6poc.bridge.mapping.BridgeDefinition;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingIndexModelCollector;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.IdentifierMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoNodeMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeNodeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoIndexableModel;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoRootIndexableModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.TypeModel;
import org.hibernate.search.v6poc.entity.processing.spi.ValueProcessor;

/**
 * @author Yoann Rodiere
 */
abstract class AbstractPojoProcessorBuilder implements PojoNodeMappingCollector {

	protected final TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider;

	protected final PojoIndexableModel indexableModel;
	protected final MappingIndexModelCollector indexModelCollector;

	protected final IdentifierMappingCollector identifierBridgeCollector;

	protected final Collection<ValueProcessor> processors = new ArrayList<>();

	public AbstractPojoProcessorBuilder(
			TypeModel<?> typeModel,
			TypeMetadataContributorProvider<PojoTypeNodeMetadataContributor> contributorProvider,
			MappingIndexModelCollector indexModelCollector,
			IdentifierMappingCollector identifierBridgeCollector) {
		this.contributorProvider = contributorProvider;

		// FIXME do something more with the indexable model, to be able to use it in containedIn processing in particular
		this.indexableModel = new PojoRootIndexableModel( typeModel, contributorProvider );
		this.indexModelCollector = indexModelCollector;

		this.identifierBridgeCollector = identifierBridgeCollector;
	}

	@Override
	public void bridge(BridgeDefinition<?> definition) {
		processors.add( indexModelCollector.addBridge( indexableModel, definition ) );
	}

}
