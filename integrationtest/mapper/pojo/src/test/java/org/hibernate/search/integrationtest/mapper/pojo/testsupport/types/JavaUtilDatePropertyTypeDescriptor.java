/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.testsupport.types;

import static org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations.TestEnvironment.withDefaultTimeZone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations.DefaultIdentifierBridgeExpectations;
import org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations.DefaultValueBridgeExpectations;
import org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations.TestEnvironment;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

public class JavaUtilDatePropertyTypeDescriptor extends PropertyTypeDescriptor<Date> {

	JavaUtilDatePropertyTypeDescriptor() {
		super( Date.class );
	}

	@Override
	public List<TestEnvironment> getTestEnvironments() {
		return Arrays.asList(
				withDefaultTimeZone( "UTC" ),
				withDefaultTimeZone( "UTC-8" ),
				withDefaultTimeZone( "UTC+10" ),
				// Test workaround for JDK-8061577/JDK-6281408
				withDefaultTimeZone( "Europe/Oslo" ),
				withDefaultTimeZone( "Europe/Paris" ),
				withDefaultTimeZone( "Europe/Amsterdam" )
		);
	}

	@Override
	public Optional<DefaultIdentifierBridgeExpectations<Date>> getDefaultIdentifierBridgeExpectations() {
		return Optional.empty();
	}

	@Override
	public Optional<DefaultValueBridgeExpectations<Date, ?>> getDefaultValueBridgeExpectations() {
		return Optional.of( new DefaultValueBridgeExpectations<Date, Instant>() {
			@Override
			public Class<Date> getProjectionType() {
				return Date.class;
			}

			@Override
			public Class<Instant> getIndexFieldJavaType() {
				return Instant.class;
			}

			@Override
			public List<Date> getEntityPropertyValues() {
				return Arrays.asList(
						date( Long.MIN_VALUE ),
						date( 1970, 1, 1, 0, 0, 0, 0 ),
						date( 1970, 1, 9, 13, 28, 59, 0 ),
						date( 2017, 11, 6, 19, 19, 0, 540 ),
						date( Long.MAX_VALUE ),

						// A february 29th on a leap year
						date( 2000, 2, 29, 12, 0, 0, 0 ),
						// A february 29th on a leap year in the Julian calendar (java.util), but not the Gregorian calendar (java.time)
						date( 1500, 2, 29, 12, 0, 0, 0 ),

						// Test workaround for JDK-8061577/JDK-6281408
						date( 1900, 1, 1, 0, 0, 0, 0 ),
						date( 1892, 1, 1, 14, 32, 0, 0 ),
						date( 1600, 1, 1, 14, 32, 0, 0 )
				);
			}

			@Override
			public List<Instant> getDocumentFieldValues() {
				return Arrays.asList(
						Instant.ofEpochMilli( Long.MIN_VALUE ),
						LocalDateTime.parse( "1970-01-01T00:00:00.00Z" ).atZone( ZoneId.systemDefault() ).toInstant(),
						LocalDateTime.parse( "1970-01-09T13:28:59.00Z" ).atZone( ZoneId.systemDefault() ).toInstant(),
						LocalDateTime.parse( "2017-11-06T19:19:00.54Z" ).atZone( ZoneId.systemDefault() ).toInstant(),
						Instant.ofEpochMilli( Long.MAX_VALUE ),

						LocalDateTime.parse( "2000-02-29T12:00:00.0Z" ).atZone( ZoneId.systemDefault() ).toInstant(),
						// The Julian calendar is 10 days late at this point
						// See https://en.wikipedia.org/wiki/Proleptic_Gregorian_calendar#Difference_between_Julian_and_proleptic_Gregorian_calendar_dates
						LocalDateTime.parse( "1500-03-10T12:00:00.0Z" ).atZone( ZoneId.systemDefault() ).toInstant(),

						LocalDateTime.parse( "1900-01-01T00:00:00.00" ).atZone( ZoneId.systemDefault() ).toInstant(),
						LocalDateTime.parse( "1892-01-01T14:32:00.00" ).atZone( ZoneId.systemDefault() ).toInstant(),
						LocalDateTime.parse( "1600-01-01T14:32:00.00" ).atZone( ZoneId.systemDefault() ).toInstant()
				);
			}

			@Override
			public Class<?> getTypeWithValueBridge1() {
				return TypeWithValueBridge1.class;
			}

			@Override
			public Object instantiateTypeWithValueBridge1(int identifier, Date propertyValue) {
				TypeWithValueBridge1 instance = new TypeWithValueBridge1();
				instance.id = identifier;
				instance.myProperty = propertyValue;
				return instance;
			}

			@Override
			public Class<?> getTypeWithValueBridge2() {
				return TypeWithValueBridge2.class;
			}
		} );
	}

	private static Date date(long epochMilli) {
		return new Date( epochMilli );
	}

	private static Date date(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		Calendar calendar = new GregorianCalendar( Locale.ROOT );
		calendar.clear();
		calendar.set( year, month - 1, day, hour, minute, second );
		calendar.set( Calendar.MILLISECOND, millisecond );
		return calendar.getTime();
	}

	@Indexed(index = DefaultValueBridgeExpectations.TYPE_WITH_VALUE_BRIDGE_1_INDEX_NAME)
	public static class TypeWithValueBridge1 {
		Integer id;
		Date myProperty;
		@DocumentId
		public Integer getId() {
			return id;
		}
		@GenericField
		public Date getMyProperty() {
			return myProperty;
		}
	}

	@Indexed(index = DefaultValueBridgeExpectations.TYPE_WITH_VALUE_BRIDGE_2_INDEX_NAME)
	public static class TypeWithValueBridge2 {
		Integer id;
		Date myProperty;
		@DocumentId
		public Integer getId() {
			return id;
		}
		@GenericField
		public Date getMyProperty() {
			return myProperty;
		}
	}
}
