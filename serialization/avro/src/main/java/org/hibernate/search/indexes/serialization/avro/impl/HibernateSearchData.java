/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.indexes.serialization.avro.impl;

import org.hibernate.search.indexes.serialization.spi.SerializableDocValuesType;
import org.hibernate.search.indexes.serialization.spi.SerializableIndex;
import org.hibernate.search.indexes.serialization.spi.SerializableStore;
import org.hibernate.search.indexes.serialization.spi.SerializableTermVector;

import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;

class HibernateSearchData extends GenericData {

	private static final String LOGICAL_TYPE_NAME_PREFIX = "HSEARCH_";

	static String toLogicalTypeName(Class<?> clazz) {
		return LOGICAL_TYPE_NAME_PREFIX + clazz.getSimpleName();
	}

	private static final HibernateSearchData INSTANCE = new HibernateSearchData();

	public static HibernateSearchData get() {
		return INSTANCE;
	}

	private HibernateSearchData() {
		registerEnumType( SerializableStore.class );
		registerEnumType( SerializableTermVector.class );
		registerEnumType( SerializableIndex.class );
		registerEnumType( SerializableDocValuesType.class );
	}

	private <E extends Enum<E>> void registerEnumType(Class<E> enumClass) {
		String logicalTypeName = LOGICAL_TYPE_NAME_PREFIX + enumClass.getSimpleName();
		// This forces Avro to take the "logicalType" attribute in the schema into account
		LogicalTypes.register( logicalTypeName, new EnumLogicalTypeFactory( logicalTypeName ) );
		// This allows automatic conversion between the enum type and GenericEnumSymbol
		// in readers/writers using this GenericData
		addLogicalTypeConversion( new EnumConversion<>( enumClass, logicalTypeName ) );
	}

	private static class EnumConversion<E extends Enum<E>> extends Conversion<E> {
		
		private final Class<E> enumClass;
		private final String logicalTypeName;

		private EnumConversion(Class<E> enumClass, String logicalTypeName) {
			this.enumClass = enumClass;
			this.logicalTypeName = logicalTypeName;
		}

		@Override
		public Class<E> getConvertedType() {
			return enumClass;
		}

		@Override
		public String getLogicalTypeName() {
			return logicalTypeName;
		}

		@Override
		public E fromCharSequence(CharSequence value, Schema schema, LogicalType type) {
			return Enum.valueOf( enumClass, value.toString() );
		}

		@Override
		public CharSequence toCharSequence(E value, Schema schema, LogicalType type) {
			return value.toString();
		}

		@Override
		public E fromEnumSymbol(GenericEnumSymbol value, Schema schema, LogicalType type) {
			return Enum.valueOf( enumClass, value.toString() );
		}

		@Override
		public GenericEnumSymbol toEnumSymbol(E value, Schema schema, LogicalType type) {
			return new GenericData.EnumSymbol( schema, value.name() );
		}
	}

	private static class EnumLogicalTypeFactory implements LogicalTypes.LogicalTypeFactory {
		private final LogicalType logicalType;

		<E extends Enum<E>> EnumLogicalTypeFactory(String logicalTypeName) {
			this.logicalType = new LogicalType( logicalTypeName );
		}

		@Override
		public LogicalType fromSchema(Schema schema) {
			return logicalType;
		}
	}
}
