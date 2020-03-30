/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.cfg.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

import org.hibernate.search.util.common.SearchException;
import org.hibernate.search.util.impl.test.SubTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;

@RunWith(Parameterized.class)
@SuppressWarnings({"unchecked", "rawtypes"}) // Raw types are the only way to mock parameterized types with EasyMock
public class ConfigurationPropertyNonConvertibleValuesTest<T> extends EasyMockSupport {

	@Parameterized.Parameters(name = "{2}")
	public static Object[][] data() {
		return new Object[][] {
				params(
						KeyContext::asInteger, "foo", 42,
						"Invalid Integer value: expected either a Number or a String that can be parsed into an Integer.",
						"For input string: \"foo\""
				),
				params(
						KeyContext::asLong, "bar", 42L,
						"Invalid Long value: expected either a Number or a String that can be parsed into a Long.",
						"For input string: \"bar\""
				),
				params(
						KeyContext::asBoolean, "foobar", true,
						"Invalid Boolean value: expected either a Boolean, the String 'true' or the String 'false'.",
						"" // Nothing particular to add in case of a parsing error
				),
				params(
						c -> c.as( MyPropertyType.class, MyPropertyType::new ),
						MyPropertyType.INVALID_STRING_VALUE, new MyPropertyType( "foobar" ),
						"Invalid value: expected either an instance of '" + MyPropertyType.class.getName()
								+ "' or a String that can be parsed.",
						MyPropertyType.INVALID_STRING_VALUE_ERROR_MESSAGE
				)
		};
	}

	private static <T> Object[] params(Function<KeyContext, OptionalPropertyContext<T>> testedMethod,
			String invalidStringValue, T validValue,
			String expectedConversionErrorCommonMessagePrefix,
			String expectedConversionErrorFromStringMessage) {
		return new Object[] {
				testedMethod, invalidStringValue, validValue,
				expectedConversionErrorCommonMessagePrefix,
				expectedConversionErrorFromStringMessage
		};
	}

	private final Function<KeyContext, OptionalPropertyContext<T>> testedMethod;
	private final String invalidStringValue;
	private final T validValue;
	private final String expectedConversionErrorCommonMessagePrefix;
	private final String expectedConversionErrorFromStringMessage;

	private final ConfigurationPropertySource sourceMock = createMock( ConfigurationPropertySource.class );

	public ConfigurationPropertyNonConvertibleValuesTest(Function<KeyContext, OptionalPropertyContext<T>> testedMethod,
			String invalidStringValue, T validValue,
			String expectedConversionErrorCommonMessagePrefix,
			String expectedConversionErrorFromStringMessage) {
		this.testedMethod = testedMethod;
		this.invalidStringValue = invalidStringValue;
		this.validValue = validValue;
		this.expectedConversionErrorCommonMessagePrefix = expectedConversionErrorCommonMessagePrefix;
		this.expectedConversionErrorFromStringMessage = expectedConversionErrorFromStringMessage;
	}

	@Test
	public void withDefault() {
		String key = "withDefault";
		String resolvedKey = "some.prefix." + key;
		ConfigurationProperty<T> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.withDefault( validValue )
						.build();

		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidStringValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidStringValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix )
				.hasMessageContaining( expectedConversionErrorFromStringMessage );
		verifyAll();

		InvalidType invalidTypeValue = new InvalidType();
		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidTypeValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidTypeValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix );
		verifyAll();
	}

	@Test
	public void withoutDefault() {
		String key = "withoutDefault";
		String resolvedKey = "some.prefix." + key;
		ConfigurationProperty<Optional<T>> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.build();

		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidStringValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidStringValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix )
				.hasMessageContaining( expectedConversionErrorFromStringMessage );
		verifyAll();

		InvalidType invalidTypeValue = new InvalidType();
		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidTypeValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidTypeValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix );
		verifyAll();
	}

	@Test
	public void multiValued() {
		String key = "multiValued";
		String resolvedKey = "some.prefix." + key;
		ConfigurationProperty<Optional<List<T>>> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.multivalued()
						.build();

		// String value - one
		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidStringValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidStringValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix )
				.hasMessageContaining( expectedConversionErrorFromStringMessage );
		verifyAll();

		// String value - multiple
		String commaSeparatedStringValue = invalidStringValue + "," + invalidStringValue;
		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( commaSeparatedStringValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + commaSeparatedStringValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix )
				.hasMessageContaining( expectedConversionErrorFromStringMessage );
		verifyAll();

		// Invalid type value in collection
		Collection<InvalidType> invalidTypeValueCollection = createCollection( new InvalidType() );
		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidTypeValueCollection ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidTypeValueCollection
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( expectedConversionErrorCommonMessagePrefix );
		verifyAll();

		// Invalid type value instead of collection
		InvalidType invalidTypeValue = new InvalidType();
		resetAll();
		EasyMock.expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( invalidTypeValue ) );
		EasyMock.expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.hasMessageContaining(
						"Cannot use value '" + invalidTypeValue
								+ "' assigned to configuration property '" + resolvedKey + "':"
				)
				.hasMessageContaining( "Invalid multi value: expected either a Collection or a String" )
				.hasCauseInstanceOf( SearchException.class );
		verifyAll();
	}

	@SafeVarargs
	private static <T> Collection<T> createCollection(T... values) {
		// Don't create a List, that would be too easy.
		Collection<T> collection = new LinkedBlockingDeque<>( 5 );
		Collections.addAll( collection, values );
		return collection;
	}

	private static class InvalidType {
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	private static class MyPropertyType {
		private static final String INVALID_STRING_VALUE = "invalid";
		private static final String INVALID_STRING_VALUE_ERROR_MESSAGE = "Value '" + INVALID_STRING_VALUE + "' is forbidden";

		private final String value;

		private MyPropertyType(String value) {
			if ( INVALID_STRING_VALUE.equals( value ) ) {
				throw new IllegalArgumentException( INVALID_STRING_VALUE_ERROR_MESSAGE );
			}
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if ( obj == null || getClass() != obj.getClass() ) {
				return false;
			}
			MyPropertyType other = (MyPropertyType) obj;
			return Objects.equals( value, other.value );
		}

		@Override
		public int hashCode() {
			return Objects.hash( value );
		}
	}
}
