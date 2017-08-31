/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.DeferredInitializationIndexFieldReference;
import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.ElasticsearchIndexFieldReference;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.DataType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.PropertyMapping;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.UnknownTypeJsonAccessor;

import com.google.gson.JsonPrimitive;

/**
 * @author Yoann Rodiere
 */
class LocalDateFieldModelContext extends AbstractScalarFieldModelContext<LocalDate> {

	private static final Function<LocalDate, JsonPrimitive> DEFAULT_FORMATTER = new LocalDateFormatter(
					new DateTimeFormatterBuilder()
							.appendValue( YEAR, 4, 9, SignStyle.EXCEEDS_PAD )
							.appendLiteral( '-' )
							.appendValue( MONTH_OF_YEAR, 2 )
							.appendLiteral( '-' )
							.appendValue( DAY_OF_MONTH, 2 )
							.toFormatter( Locale.ROOT )
							.withResolverStyle( ResolverStyle.STRICT )
			);

	private final UnknownTypeJsonAccessor accessor;
	private final Function<LocalDate, JsonPrimitive> formatter = DEFAULT_FORMATTER; // TODO add method to allow customization

	public LocalDateFieldModelContext(UnknownTypeJsonAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	protected void build(DeferredInitializationIndexFieldReference<LocalDate> reference, PropertyMapping mapping) {
		super.build( reference, mapping );
		reference.initialize( new ElasticsearchIndexFieldReference<>( accessor, formatter ) );
		mapping.setType( DataType.DATE );
		mapping.setFormat( Arrays.asList( "strict_date", "yyyyyyyyy-MM-dd" ) );
	}

	private static class LocalDateFormatter implements Function<LocalDate, JsonPrimitive> {

		private final DateTimeFormatter delegate;

		protected LocalDateFormatter(DateTimeFormatter delegate) {
			this.delegate = delegate;
		}

		@Override
		public JsonPrimitive apply(LocalDate value) {
			return value == null ? null : new JsonPrimitive( delegate.format( value ) );
		}

	}
}
