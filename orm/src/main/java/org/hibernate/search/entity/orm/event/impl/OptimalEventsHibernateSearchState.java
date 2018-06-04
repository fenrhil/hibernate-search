/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.orm.event.impl;

import java.util.Objects;

import org.hibernate.search.entity.orm.impl.HibernateSearchContextService;

/**
 * The implementation of EventsHibernateSearchState used at runtime,
 * after initialization of the ExtendedSearchIntegrator has been
 * performed.
 *
 * @author Sanne Grinovero
 */
final class OptimalEventsHibernateSearchState implements EventsHibernateSearchState {

	private final HibernateSearchContextService context;

	public OptimalEventsHibernateSearchState(HibernateSearchContextService context) {
		Objects.requireNonNull( context );
		this.context = context;
	}

	@Override
	public HibernateSearchContextService getHibernateSearchContext() {
		return context;
	}

}
