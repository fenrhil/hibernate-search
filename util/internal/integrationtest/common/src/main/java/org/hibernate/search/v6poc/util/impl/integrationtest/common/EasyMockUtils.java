/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common;

import static org.hibernate.search.v6poc.util.impl.integrationtest.common.NormalizationUtils.normalizeProjection;
import static org.hibernate.search.v6poc.util.impl.integrationtest.common.NormalizationUtils.normalizeReference;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hibernate.search.v6poc.search.DocumentReference;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;

/**
 * Utils useful when using EasyMock in backend tests.
 */
public final class EasyMockUtils {

	private EasyMockUtils() {
	}

	public static DocumentReference referenceMatcher(DocumentReference expected) {
		DocumentReference normalizedExpected = normalizeReference( expected );
		EasyMock.reportMatcher( new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				return argument instanceof DocumentReference
						&& Objects.equals( normalizedExpected, normalizeReference( (DocumentReference) argument ) );
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append( normalizedExpected );
			}
		} );
		return normalizedExpected;
	}

	public static List<?> projectionMatcher(Object ... expected) {
		return projectionMatcher( Arrays.asList( expected ) );
	}

	public static List<?> projectionMatcher(List<?> expected) {
		List<?> normalizedExpected = normalizeProjection( expected );
		EasyMock.reportMatcher( new IArgumentMatcher() {
			@Override
			public boolean matches(Object argument) {
				return argument instanceof List
						&& Objects.equals( normalizedExpected, normalizeProjection( (List<?>) argument ) );
			}

			@Override
			public void appendTo(StringBuffer buffer) {
				buffer.append( normalizedExpected );
			}
		} );
		return normalizedExpected;
	}
}
