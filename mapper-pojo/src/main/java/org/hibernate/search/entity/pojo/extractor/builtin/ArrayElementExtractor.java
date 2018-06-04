/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.pojo.extractor.builtin;

import java.util.Arrays;
import java.util.stream.Stream;

import org.hibernate.search.entity.pojo.extractor.ContainerValueExtractor;

public class ArrayElementExtractor<T> implements ContainerValueExtractor<T[], T> {
	@Override
	public Stream<T> extract(T[] container) {
		return container == null ? Stream.empty() : Arrays.stream( container );
	}
}
