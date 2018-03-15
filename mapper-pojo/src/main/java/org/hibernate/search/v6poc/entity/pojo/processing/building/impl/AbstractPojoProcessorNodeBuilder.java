/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.processing.building.impl;

import java.util.Optional;

import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.processing.impl.PojoIndexingProcessor;

abstract class AbstractPojoProcessorNodeBuilder<T> {

	protected final TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider;

	protected final AbstractPojoProcessorNodeBuilder<?> parent;
	protected final PojoIndexModelBinder indexModelBinder;
	protected final IndexModelBindingContext bindingContext;

	AbstractPojoProcessorNodeBuilder(AbstractPojoProcessorNodeBuilder<?> parent,
			TypeMetadataContributorProvider<PojoTypeMetadataContributor> contributorProvider,
			PojoIndexModelBinder indexModelBinder, IndexModelBindingContext bindingContext) {
		this.parent = parent;
		this.contributorProvider = contributorProvider;
		this.indexModelBinder = indexModelBinder;
		this.bindingContext = bindingContext;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( getClass().getSimpleName() )
				.append( "[" );
		appendPath( builder);
		builder.append( "]" );
		return builder.toString();
	}

	abstract Optional<? extends PojoIndexingProcessor<T>> build();

	private void appendPath(StringBuilder builder) {
		if ( parent == null ) {
			appendSelfPath( builder );
		}
		else {
			parent.appendPath( builder );
			builder.append( " => " );
			appendSelfPath( builder );
		}
	}

	protected abstract void appendSelfPath(StringBuilder builder);
}
