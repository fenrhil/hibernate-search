/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.entity.orm.util.impl;

import java.util.Arrays;
import java.util.stream.Stream;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.search.entity.pojo.util.spi.AbstractPojoTypeOrdering;

public final class XClassOrdering extends AbstractPojoTypeOrdering<XClass> {

	private static final XClassOrdering INSTANCE = new XClassOrdering();

	public static XClassOrdering get() {
		return INSTANCE;
	}

	private XClassOrdering() {
	}

	@Override
	protected XClass getSuperClass(XClass subType) {
		return subType.getSuperclass();
	}

	@Override
	protected Stream<XClass> getDeclaredInterfaces(XClass subType) {
		return Arrays.stream( subType.getInterfaces() );
	}
}
