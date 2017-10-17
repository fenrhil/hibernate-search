package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative;

import java.util.Map;

import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.AbstractConfiguredExtraPropertiesJsonAdapterFactory;

import com.google.gson.reflect.TypeToken;

public class TypeMappingJsonAdapterFactory extends AbstractConfiguredExtraPropertiesJsonAdapterFactory {

	private static final TypeToken<Map<String, PropertyMapping>> PROPERTY_MAP_TYPE_TOKEN =
			new TypeToken<Map<String, PropertyMapping>>() {
			};

	@Override
	protected <T> void addFields(Builder<T> builder) {
		builder.add( "properties", PROPERTY_MAP_TYPE_TOKEN );
		builder.add( "dynamic", boolean.class );
	}

}