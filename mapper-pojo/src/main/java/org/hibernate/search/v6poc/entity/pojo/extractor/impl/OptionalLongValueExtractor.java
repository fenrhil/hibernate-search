/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.extractor.impl;

import java.util.OptionalLong;
import java.util.stream.Stream;

public class OptionalLongValueExtractor implements ContainerValueExtractor<OptionalLong, Long> {
	private static final OptionalLongValueExtractor INSTANCE = new OptionalLongValueExtractor();

	public static OptionalLongValueExtractor get() {
		return INSTANCE;
	}

	@Override
	public Stream<Long> extract(OptionalLong container) {
		if ( container != null && container.isPresent() ) {
			return Stream.of( container.getAsLong() );
		}
		else {
			return Stream.empty();
		}
	}
}
