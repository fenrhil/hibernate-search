/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.search.dsl.predicate.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.search.search.predicate.spi.BooleanJunctionPredicateBuilder;
import org.hibernate.search.search.predicate.spi.SearchPredicateBuilder;
import org.hibernate.search.search.predicate.spi.SearchPredicateContributor;
import org.hibernate.search.search.predicate.spi.SearchPredicateFactory;

class MultiFieldPredicateCommonState<N, C, F extends MultiFieldPredicateCommonState.FieldSetContext<C>>
		implements SearchPredicateContributor<C> {

	private final SearchPredicateFactory<C> factory;

	private final Supplier<N> nextContextProvider;

	private final List<F> fieldSetContexts = new ArrayList<>();

	MultiFieldPredicateCommonState(SearchPredicateFactory<C> factory, Supplier<N> nextContextProvider) {
		this.factory = factory;
		this.nextContextProvider = nextContextProvider;
	}

	public SearchPredicateFactory<C> getFactory() {
		return factory;
	}

	public void add(F fieldSetContext) {
		fieldSetContexts.add( fieldSetContext );
	}

	public Supplier<N> getNextContextProvider() {
		return nextContextProvider;
	}

	List<F> getFieldSetContexts() {
		return fieldSetContexts;
	}

	@Override
	public void contribute(C collector) {
		List<SearchPredicateBuilder<? super C>> predicateBuilders = new ArrayList<>();
		for ( F fieldSetContext : fieldSetContexts ) {
			fieldSetContext.contributePredicateBuilders( predicateBuilders::add );
		}
		if ( predicateBuilders.size() > 1 ) {
			BooleanJunctionPredicateBuilder<C> boolBuilder = factory.bool();
			C shouldCollector = boolBuilder.getShouldCollector();
			predicateBuilders.forEach( b -> b.contribute( shouldCollector ) );
			boolBuilder.contribute( collector );
		}
		else {
			predicateBuilders.get( 0 ).contribute( collector );
		}
	}

	public interface FieldSetContext<C> {
		void contributePredicateBuilders(Consumer<SearchPredicateBuilder<? super C>> collector);
	}
}