/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.schema.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.hibernate.search.analyzer.impl.AnalyzerReference;
import org.hibernate.search.analyzer.impl.RemoteAnalyzerReference;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.elasticsearch.impl.GsonService;
import org.hibernate.search.elasticsearch.logging.impl.Log;
import org.hibernate.search.elasticsearch.schema.impl.model.DataType;
import org.hibernate.search.elasticsearch.schema.impl.model.DynamicType;
import org.hibernate.search.elasticsearch.schema.impl.model.IndexType;
import org.hibernate.search.elasticsearch.schema.impl.model.PropertyMapping;
import org.hibernate.search.elasticsearch.schema.impl.model.TypeMapping;
import org.hibernate.search.elasticsearch.util.impl.FieldHelper;
import org.hibernate.search.elasticsearch.util.impl.FieldHelper.ExtendedFieldType;
import org.hibernate.search.engine.metadata.impl.BridgeDefinedField;
import org.hibernate.search.engine.metadata.impl.DocumentFieldMetadata;
import org.hibernate.search.engine.metadata.impl.FacetMetadata;
import org.hibernate.search.engine.metadata.impl.PropertyMetadata;
import org.hibernate.search.engine.metadata.impl.TypeMetadata;
import org.hibernate.search.engine.service.spi.ServiceManager;
import org.hibernate.search.engine.service.spi.Startable;
import org.hibernate.search.engine.service.spi.Stoppable;
import org.hibernate.search.engine.spi.DocumentBuilderIndexedEntity;
import org.hibernate.search.engine.spi.EntityIndexBinding;
import org.hibernate.search.exception.AssertionFailure;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.spatial.impl.SpatialHelper;
import org.hibernate.search.spi.BuildContext;
import org.hibernate.search.util.logging.impl.LoggerFactory;

import com.google.gson.Gson;

/**
 * The default {@link ElasticsearchSchemaTranslator} implementation.
 * @author Gunnar Morling
 * @author Yoann Rodiere
 */
public class DefaultElasticsearchSchemaTranslator implements ElasticsearchSchemaTranslator, Startable, Stoppable {

	private static final Log LOG = LoggerFactory.make( Log.class );

	private ServiceManager serviceManager;
	private GsonService gsonService;

	@Override
	public void start(Properties properties, BuildContext context) {
		serviceManager = context.getServiceManager();
		gsonService = serviceManager.requestService( GsonService.class );
	}

	@Override
	public void stop() {
		gsonService = null;
		serviceManager.releaseService( GsonService.class );
		serviceManager = null;
	}

	public TypeMapping translate(EntityIndexBinding descriptor, ExecutionOptions executionOptions) {
		TypeMapping result = new TypeMapping();

		result.setDynamic( DynamicType.STRICT );

		if ( executionOptions.isMultitenancyEnabled() ) {
			PropertyMapping tenantId = new PropertyMapping();
			tenantId.setType( DataType.STRING );
			tenantId.setIndex( IndexType.NOT_ANALYZED );
			result.addProperty( DocumentBuilderIndexedEntity.TENANT_ID_FIELDNAME, tenantId );
		}

		// normal document fields
		for ( DocumentFieldMetadata fieldMetadata : descriptor.getDocumentBuilder().getTypeMetadata().getAllDocumentFieldMetadata() ) {
			try {
				addPropertyMapping( result, descriptor, fieldMetadata );
			}
			catch (IncompleteDataException e) {
				LOG.debug( "Not adding a mapping for field " + fieldMetadata.getFieldName() + " because of incomplete data", e );
			}
		}

		// bridge-defined fields
		for ( BridgeDefinedField bridgeDefinedField : getAllBridgeDefinedFields( descriptor ) ) {
			try {
				addPropertyMapping( result, descriptor, bridgeDefinedField );
			}
			catch (IncompleteDataException e) {
				LOG.debug( "Not adding a mapping for field " + bridgeDefinedField.getName() + " because of incomplete data", e );
			}
		}

		return result;
	}

	private static class IncompleteDataException extends SearchException {
		public IncompleteDataException(String message) {
			super( message );
		}
	}

	private String analyzerName(Class<?> entityType, String fieldName, AnalyzerReference analyzerReference) {
		if ( analyzerReference.is( RemoteAnalyzerReference.class ) ) {
			return analyzerReference.unwrap( RemoteAnalyzerReference.class ).getAnalyzer().getName( fieldName );
		}
		LOG.analyzerIsNotRemote( entityType, fieldName, analyzerReference );
		return null;
	}

	/**
	 * Adds a property mapping for the given field to the given type mapping.
	 */
	private void addPropertyMapping(TypeMapping rootMapping, EntityIndexBinding descriptor, DocumentFieldMetadata fieldMetadata) {
		if ( fieldMetadata.isId() || fieldMetadata.getFieldName().isEmpty() || fieldMetadata.getFieldName().endsWith( "." )
				|| fieldMetadata.isSpatial() ) {
			return;
		}

		String propertyPath = fieldMetadata.getName();
		String propertyName = FieldHelper.getEmbeddedFieldPropertyName( propertyPath );

		PropertyMapping propertyMapping = new PropertyMapping();

		DataType type = addTypeOptions( propertyMapping, descriptor, fieldMetadata );

		propertyMapping.setStore( fieldMetadata.getStore() == Store.NO ? false : true );

		addIndexOptions( propertyMapping, descriptor, propertyPath,
				type, fieldMetadata.getIndex(), fieldMetadata.getAnalyzerReference() );

		propertyMapping.setBoost( fieldMetadata.getBoost() );

		addNullValue( propertyMapping, descriptor, type, fieldMetadata );

		// Create facet fields if needed: if the facet has the same name as the field, we don't need to create an
		// extra field for it
		for ( FacetMetadata facetMetadata : fieldMetadata.getFacetMetadata() ) {
			if ( !facetMetadata.getFacetName().equals( fieldMetadata.getFieldName() ) ) {
				try {
					addPropertyMapping( rootMapping, facetMetadata );
				}
				catch (IncompleteDataException e) {
					LOG.debug( "Not adding a mapping for facet " + facetMetadata.getFacetName() + " because of incomplete data", e );
				}
			}
		}

		// Do this last, when we're sure no exception will be thrown for this mapping
		TypeMapping parentMapping = getOrCreateParentMapping( rootMapping, propertyPath );
		parentMapping.addProperty( propertyName, propertyMapping );
	}

	/**
	 * Adds a type mapping for the given field to the given request payload.
	 */
	private void addPropertyMapping(TypeMapping rootMapping, EntityIndexBinding binding, BridgeDefinedField bridgeDefinedField) {
		String propertyPath = bridgeDefinedField.getName();
		String propertyName = FieldHelper.getEmbeddedFieldPropertyName( propertyPath );

		if ( !SpatialHelper.isSpatialField( propertyName ) ) {
			PropertyMapping propertyMapping = new PropertyMapping();
			DataType type = addTypeOptions( propertyMapping, bridgeDefinedField );
			propertyMapping.setType( type );
			addIndexOptions( propertyMapping, binding, propertyName, type, bridgeDefinedField.getIndex(), null );

			TypeMapping parentMapping = getOrCreateParentMapping( rootMapping, propertyPath );
			// we don't overwrite already defined fields. Typically, in the case of spatial, the geo_point field
			// is defined before the double field and we want to keep the geo_point one
			Map<String, PropertyMapping> parentMappingProperties = parentMapping.getProperties();
			if ( parentMappingProperties == null || !parentMapping.getProperties().containsKey( propertyName ) ) {
				parentMapping.addProperty( propertyName, propertyMapping );
			}
		}
		else {
			if ( SpatialHelper.isSpatialFieldLongitude( propertyName ) ) {
				// we ignore the longitude field, we will create the geo_point mapping only once with the latitude field
				return;
			}
			else if ( SpatialHelper.isSpatialFieldLatitude( propertyName ) ) {
				// we only add the geo_point for the latitude field
				PropertyMapping propertyMapping = new PropertyMapping();

				propertyMapping.setType( DataType.GEO_POINT );

				// in this case, the spatial field has precedence over an already defined field
				TypeMapping parentMapping = getOrCreateParentMapping( rootMapping, propertyPath );
				parentMapping.addProperty( SpatialHelper.getSpatialFieldRootName( propertyName ), propertyMapping );
			}
			else {
				// the fields potentially created for the spatial hash queries
				PropertyMapping propertyMapping = new PropertyMapping();
				propertyMapping.setType( DataType.STRING );
				propertyMapping.setIndex( IndexType.NOT_ANALYZED );

				TypeMapping parentMapping = getOrCreateParentMapping( rootMapping, propertyPath );
				parentMapping.addProperty( propertyName, propertyMapping );
			}
		}
	}

	private void addPropertyMapping(TypeMapping rootMapping, FacetMetadata facetMetadata) {
		String propertyPath = facetMetadata.getFacetName();
		String propertyName = FieldHelper.getEmbeddedFieldPropertyName( propertyPath );

		PropertyMapping propertyMapping = new PropertyMapping();

		addTypeOptions( propertyMapping, facetMetadata );
		propertyMapping.setStore( false );
		propertyMapping.setIndex( IndexType.NOT_ANALYZED );

		// Do this last, when we're sure no exception will be thrown for this mapping
		TypeMapping parentMapping = getOrCreateParentMapping( rootMapping, propertyPath );
		parentMapping.addProperty( propertyName, propertyMapping );
	}

	/**
	 * Adds the main indexing-related options to the given field: "index", "doc_values", "analyzer", ...
	 */
	private void addIndexOptions(PropertyMapping propertyMapping, EntityIndexBinding binding, String propertyPath,
			DataType fieldType, Field.Index index, AnalyzerReference analyzerReference) {
		IndexType elasticsearchIndex;
		switch ( index ) {
			case ANALYZED:
			case ANALYZED_NO_NORMS:
				elasticsearchIndex = canTypeBeAnalyzed( fieldType ) ? IndexType.ANALYZED : IndexType.NOT_ANALYZED;
				break;
			case NOT_ANALYZED:
			case NOT_ANALYZED_NO_NORMS:
				elasticsearchIndex = IndexType.NOT_ANALYZED;
				break;
			case NO:
				elasticsearchIndex = IndexType.NO;
				break;
			default:
				throw new AssertionFailure( "Unexpected index type: " + index );
		}
		propertyMapping.setIndex( elasticsearchIndex );

		if ( IndexType.NO.equals( elasticsearchIndex ) && FieldHelper.isSortableField( binding, propertyPath ) ) {
			// We must use doc values in order to enable sorting on non-indexed fields
			propertyMapping.setDocValues( true );
		}

		if ( IndexType.ANALYZED.equals( elasticsearchIndex ) && analyzerReference != null ) {
			String analyzerName = analyzerName( binding.getDocumentBuilder().getBeanClass(), propertyPath, analyzerReference );
			propertyMapping.setAnalyzer( analyzerName );
		}
	}

	private boolean canTypeBeAnalyzed(DataType fieldType) {
		return DataType.STRING.equals( fieldType );
	}

	private DataType addTypeOptions(PropertyMapping propertyMapping, EntityIndexBinding descriptor, DocumentFieldMetadata fieldMetadata) {
		return addTypeOptions( fieldMetadata.getFieldName(), propertyMapping, FieldHelper.getType( descriptor, fieldMetadata ) );
	}

	private DataType addTypeOptions(PropertyMapping propertyMapping, BridgeDefinedField bridgeDefinedField) {
		ExtendedFieldType type = FieldHelper.getType( bridgeDefinedField );

		if ( ExtendedFieldType.UNKNOWN.equals( type ) ) {
			throw LOG.unexpectedFieldType( bridgeDefinedField.getType().name(), bridgeDefinedField.getName() );
		}

		return addTypeOptions( bridgeDefinedField.getName(), propertyMapping, type );
	}

	private DataType addTypeOptions(PropertyMapping propertyMapping, FacetMetadata facetMetadata) {
		ExtendedFieldType type;

		switch ( facetMetadata.getEncoding() ) {
			case DOUBLE:
				type = ExtendedFieldType.DOUBLE;
				break;
			case LONG:
				type = ExtendedFieldType.LONG;
				break;
			case STRING:
				type = ExtendedFieldType.STRING;
				break;
			case AUTO:
				throw new AssertionFailure( "The facet type should have been resolved during bootstrapping" );
			default: {
				throw new AssertionFailure(
						"Unexpected facet encoding type '"
								+ facetMetadata.getEncoding()
								+ "' Has the enum been modified?"
				);
			}
		}

		return addTypeOptions( facetMetadata.getFacetName(), propertyMapping, type );
	}

	private DataType addTypeOptions(String fieldName, PropertyMapping propertyMapping, ExtendedFieldType extendedType) {
		DataType elasticsearchType;
		List<String> formats = new ArrayList<>();

		/* Note: for date formats, we use a 4-digit year format as the first format
		 * (which is the output format), so that Elasticsearch outputs are more
		 * human-readable.
		 */
		switch ( extendedType ) {
			case BOOLEAN:
				elasticsearchType = DataType.BOOLEAN;
				break;
			case CALENDAR:
			case DATE:
			case INSTANT:
				elasticsearchType = DataType.DATE;
				// Use default formats ("strict_date_optional_time||epoch_millis")
				break;
			case LOCAL_DATE:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_date" );
				formats.add( "yyyyyyyyy-MM-dd" );
				break;
			case LOCAL_DATE_TIME:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_date_hour_minute_second_fraction" );
				formats.add( "yyyyyyyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS" );
				break;
			case LOCAL_TIME:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_hour_minute_second_fraction" );
				break;
			case OFFSET_DATE_TIME:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_date_time" );
				formats.add( "yyyyyyyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ" );
				break;
			case OFFSET_TIME:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_time" );
				break;
			case ZONED_DATE_TIME:
				elasticsearchType = DataType.DATE;
				formats.add( "yyyy-MM-dd'T'HH:mm:ss.SSSZZ'['ZZZ']'" );
				formats.add( "yyyyyyyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZZ'['ZZZ']'" );
				break;
			case YEAR:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_year" );
				formats.add( "yyyyyyyyy" );
				break;
			case YEAR_MONTH:
				elasticsearchType = DataType.DATE;
				formats.add( "strict_year_month" );
				formats.add( "yyyyyyyyy-MM" );
				break;
			case MONTH_DAY:
				elasticsearchType = DataType.DATE;
				/*
				 * This seems to be the ISO-8601 format for dates without year.
				 * It's also the default format for Java's MonthDay, see MonthDay.PARSER.
				 */
				formats.add( "--MM-dd" );
				break;
			case INTEGER:
				elasticsearchType = DataType.INTEGER;
				break;
			case LONG:
				elasticsearchType = DataType.LONG;
				break;
			case FLOAT:
				elasticsearchType = DataType.FLOAT;
				break;
			case DOUBLE:
				elasticsearchType = DataType.DOUBLE;
				break;
			case UNKNOWN_NUMERIC:
				// Likely a custom field bridge which does not expose the type of the given field; either correctly
				// so (because the given name is the default field and this bridge does not wish to use that field
				// name as is) or incorrectly; The field will not be added to the mapping, causing an exception at
				// runtime if the bridge writes that field nevertheless
				elasticsearchType = null;
				break;
			case STRING:
			case UNKNOWN:
			default:
				elasticsearchType = DataType.STRING;
				break;
		}

		if ( elasticsearchType == null ) {
			throw new IncompleteDataException( "Field type could not be determined" );
		}

		propertyMapping.setType( elasticsearchType );

		if ( !formats.isEmpty() ) {
			propertyMapping.setFormat( formats );
		}

		return elasticsearchType;
	}

	private void addNullValue(PropertyMapping propertyMapping, EntityIndexBinding indexBinding, DataType dataType,
			DocumentFieldMetadata fieldMetadata) {
		String indexNullAs = fieldMetadata.indexNullAs();
		if ( indexNullAs != null ) {
			Object convertedValue = ElasticSearchIndexNullAsHelper.getNullValue(
					fieldMetadata.getName(), dataType, indexNullAs
					);
			Gson gson = gsonService.getGson();
			propertyMapping.setNullValue( gson.toJsonTree( convertedValue ).getAsJsonPrimitive() );
		}
	}

	private TypeMapping getOrCreateParentMapping(TypeMapping rootMapping, String fieldName) {
		if ( !FieldHelper.isEmbeddedField( fieldName ) ) {
			return rootMapping;
		}

		TypeMapping parentMapping = rootMapping;

		String[] parts = fieldName.split( "\\." );
		for ( int i = 0; i < parts.length - 1; i++ ) {
			String part = parts[i];
			Map<String, PropertyMapping> parentMappingProperties = parentMapping.getProperties();
			PropertyMapping mapping =
					parentMappingProperties == null ? null : parentMapping.getProperties().get( part );
			if ( mapping == null ) {
				mapping = new PropertyMapping();

				// TODO HSEARCH-2263 enable nested mapping as needed:
				// * only needed for embedded *-to-many with more than one field
				// * for these, the user should be able to opt out (nested would be the safe default mapping in this
				// case, but they could want to opt out when only ever querying on single fields of the embeddable)

//				mapping.setType( DataType.NESTED );

				parentMapping.addProperty( part, mapping );
			}
			parentMapping = mapping;
		}

		return parentMapping;
	}

	/**
	 * Recursively collects all the bridge-defined fields for the given type and its embeddables.
	 */
	private Set<BridgeDefinedField> getAllBridgeDefinedFields(EntityIndexBinding binding) {
		Set<BridgeDefinedField> bridgeDefinedFields = new HashSet<>();
		collectPropertyLevelBridgeDefinedFields( binding.getDocumentBuilder().getMetadata(), bridgeDefinedFields );
		return bridgeDefinedFields;
	}

	private void collectPropertyLevelBridgeDefinedFields(TypeMetadata type, Set<BridgeDefinedField> allBridgeDefinedFields) {
		allBridgeDefinedFields.addAll( type.getClassBridgeDefinedFields() );

		if ( type.getIdPropertyMetadata() != null ) {
			allBridgeDefinedFields.addAll( type.getIdPropertyMetadata().getBridgeDefinedFields().values() );
		}

		for ( PropertyMetadata property : type.getAllPropertyMetadata() ) {
			allBridgeDefinedFields.addAll( property.getBridgeDefinedFields().values() );
		}

		for ( TypeMetadata embeddedType : type.getEmbeddedTypeMetadata() ) {
			collectPropertyLevelBridgeDefinedFields( embeddedType, allBridgeDefinedFields );
		}
	}
}
