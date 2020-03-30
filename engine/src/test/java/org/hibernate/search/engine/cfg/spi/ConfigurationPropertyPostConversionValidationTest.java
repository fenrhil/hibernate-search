/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.cfg.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.search.util.common.SearchException;
import org.hibernate.search.util.impl.test.SubTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.easymock.EasyMockSupport;

@RunWith(Parameterized.class)
@SuppressWarnings({"unchecked", "rawtypes"}) // Raw types are the only way to mock parameterized types with EasyMock
public class ConfigurationPropertyPostConversionValidationTest<T> extends EasyMockSupport {

	@Parameterized.Parameters(name = "{2}")
	public static Object[][] data() {
		return new Object[][] {
				params( KeyContext::asString, "string", "string" ),
				params( KeyContext::asInteger, "42", 42 ),
				params( KeyContext::asLong, "3000000000042", 3000000000042L ),
				params( KeyContext::asBoolean, "true", true ),
				params( KeyContext::asBoolean, "false", false ),
				params(
						c -> c.as( MyPropertyType.class, MyPropertyType::new ),
						"string", new MyPropertyType( "string" )
				)
		};
	}

	private static <T> Object[] params(Function<KeyContext, OptionalPropertyContext<T>> testedMethod,
			String stringValue, T expectedValue) {
		return new Object[] { testedMethod, stringValue, expectedValue };
	}

	private final Function<KeyContext, OptionalPropertyContext<T>> testedMethod;
	private final String stringValue;
	private final T expectedValue;

	private final Consumer<T> validatorMock = createMock( Consumer.class );
	private final ConfigurationPropertySource sourceMock = createMock( ConfigurationPropertySource.class );

	public ConfigurationPropertyPostConversionValidationTest(Function<KeyContext, OptionalPropertyContext<T>> testedMethod,
			String stringValue, T expectedValue) {
		this.testedMethod = testedMethod;
		this.stringValue = stringValue;
		this.expectedValue = expectedValue;
	}

	@Test
	public void withDefault() {
		SimulatedFailure validationException = new SimulatedFailure( "INVALID" );
		String key = "withDefault";
		String resolvedKey = "some.prefix." + key;
		ConfigurationProperty<T> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.withValidator( validatorMock )
						.withDefault( expectedValue )
						.build();

		T result;

		// No value: no validation on the default
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( Optional.empty() );
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).isEqualTo( expectedValue );

		// String value - converted is valid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( stringValue ) );
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).isEqualTo( expectedValue );

		// String value - converted is invalid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( stringValue ) );
		validatorMock.accept( expectedValue );
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContainingAll(
						"Cannot use value '" + stringValue
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();

		// Typed value - valid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( expectedValue ) );
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).isEqualTo( expectedValue );

		// Typed value - invalid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( expectedValue ) );
		validatorMock.accept( expectedValue );
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContainingAll(
						"Cannot use value '" + expectedValue
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();
	}

	@Test
	public void withoutDefault() {
		SimulatedFailure validationException = new SimulatedFailure( "INVALID" );
		String key = "withDefault";
		String resolvedKey = "some.prefix." + key;
		OptionalConfigurationProperty<T> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.withValidator( validatorMock )
						.build();

		Optional<T> result;

		// No value: no validation
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( Optional.empty() );
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).isEmpty();

		// String value - converted is valid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( stringValue ) );
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).contains( expectedValue );

		// String value - converted is invalid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( stringValue ) );
		validatorMock.accept( expectedValue );
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContainingAll(
						"Cannot use value '" + stringValue
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();

		// Typed value - valid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( expectedValue ) );
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).contains( expectedValue );

		// Typed value - invalid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( expectedValue ) );
		validatorMock.accept( expectedValue );
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContainingAll(
						"Cannot use value '" + expectedValue
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();
	}

	@Test
	public void withoutDefault_getOrThrow() {
		SimulatedFailure validationException = new SimulatedFailure( "INVALID" );
		String key = "withoutDefault_getOrThrow";
		String resolvedKey = "some.prefix." + key;
		OptionalConfigurationProperty<T> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.withValidator( validatorMock )
						.build();

		// No value -> exception
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( Optional.empty() );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.getOrThrow( sourceMock, SimulatedFailure::new ) )
				.assertThrown()
				.isInstanceOf( SimulatedFailure.class )
				.hasMessage( resolvedKey );
		verifyAll();

		// Valid value -> no exception
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( expectedValue ) );
		validatorMock.accept( expectedValue );
		replayAll();
		T result = property.getOrThrow( sourceMock, SimulatedFailure::new );
		verifyAll();
		assertThat( result ).isEqualTo( expectedValue );

		// Invalid value -> exception from validator
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( expectedValue ) );
		validatorMock.accept( expectedValue );
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.getOrThrow( sourceMock, SimulatedFailure::new ) )
				.assertThrown()
				.hasMessageContainingAll(
						"Cannot use value '" + expectedValue
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();
	}

	@Test
	public void multiValued() {
		SimulatedFailure validationException = new SimulatedFailure( "INVALID" );
		String key = "multiValued";
		String resolvedKey = "some.prefix." + key;
		ConfigurationProperty<Optional<List<T>>> property =
				testedMethod.apply(
						ConfigurationProperty.forKey( key )
				)
						.withValidator( validatorMock )
						.multivalued()
						.build();

		Optional<List<T>> result;

		// Typed value - one - valid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( createCollection( expectedValue ) ) );
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).isNotEmpty();
		assertThat( result.get() ).containsExactly( expectedValue );

		// Typed value - one - invalid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( createCollection( expectedValue ) ) );
		validatorMock.accept( expectedValue );
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContainingAll(
						"Cannot use value '" + createCollection( expectedValue )
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();

		// Typed value - multiple - valid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( createCollection( expectedValue, expectedValue ) ) );
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		validatorMock.accept( expectedValue ); // Valid: don't throw an exception
		replayAll();
		result = property.get( sourceMock );
		verifyAll();
		assertThat( result ).isNotEmpty();
		assertThat( result.get() ).containsExactly( expectedValue, expectedValue );

		// Typed value - multiple - invalid
		resetAll();
		expect( sourceMock.get( key ) ).andReturn( (Optional) Optional.of( createCollection( expectedValue, expectedValue ) ) );
		validatorMock.accept( expectedValue ); // Let's imagine the first value is valid: don't throw an exception
		validatorMock.accept( expectedValue ); // ... but the second value is invalid
		expectLastCall().andThrow( validationException );
		expect( sourceMock.resolve( key ) ).andReturn( Optional.of( resolvedKey ) );
		replayAll();
		SubTest.expectException( () -> property.get( sourceMock ) )
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContainingAll(
						"Cannot use value '" + createCollection( expectedValue, expectedValue )
								+ "' assigned to configuration property '" + resolvedKey + "':",
						validationException.getMessage()
				)
				.hasCause( validationException );
		verifyAll();
	}

	@SafeVarargs
	private static <T> Collection<T> createCollection(T... values) {
		// Don't create a List, that would be too easy.
		Collection<T> collection = new LinkedBlockingDeque<>( 5 );
		Collections.addAll( collection, values );
		return collection;
	}

	private static class MyPropertyType {
		private final String value;

		private MyPropertyType(String value) {
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

	private static class SimulatedFailure extends RuntimeException {
		SimulatedFailure(String message) {
			super( message );
		}
	}

}
