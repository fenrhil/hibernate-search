/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.extractor.builtin;

import java.util.OptionalInt;
import java.util.stream.Stream;

import org.hibernate.search.entity.pojo.extractor.ContainerValueExtractor;

public class OptionalIntValueExtractor implements ContainerValueExtractor<OptionalInt, Integer> {
	@Override
	public Stream<Integer> extract(OptionalInt container) {
		if ( container != null && container.isPresent() ) {
			return Stream.of( container.getAsInt() );
		}
		else {
			return Stream.empty();
		}
	}
}
