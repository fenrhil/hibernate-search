/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.document.model.spi;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * @author Yoann Rodiere
 */
class ExcludeAllIndexModelNestingContext implements IndexModelNestingContext {

	static final ExcludeAllIndexModelNestingContext INSTANCE = new ExcludeAllIndexModelNestingContext();

	private ExcludeAllIndexModelNestingContext() {
	}

	@Override
	public <T> Optional<T> applyIfIncluded(String relativeName, Function<String, T> action) {
		return Optional.empty();
	}

	@Override
	public <T> Optional<T> applyIfIncluded(String relativeName, BiFunction<String, IndexModelNestingContext, T> action) {
		return Optional.empty();
	}

}
