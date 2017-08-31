/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yoann Rodiere
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
// TODO repeatable
public @interface Field {

	String name() default "";

	FunctionBridgeBeanReference bridge() default @FunctionBridgeBeanReference;

	// TODO index, analyze, store, norms, termVector
	// TODO analyzer, normalizer
	// TODO indexNullAs? => Maybe we should rather use "missing" queries?

}
