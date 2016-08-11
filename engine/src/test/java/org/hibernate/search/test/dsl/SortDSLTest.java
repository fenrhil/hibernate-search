/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.dsl;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Spatial;
import org.hibernate.search.annotations.SpatialMode;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.StringBridge;
import org.hibernate.search.bridge.builtin.NumericFieldBridge;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.engine.service.spi.ServiceReference;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.hibernate.search.query.dsl.sort.DistanceMethod;
import org.hibernate.search.query.dsl.sort.MultiValuedMode;
import org.hibernate.search.query.engine.impl.LuceneQueryTranslator;
import org.hibernate.search.query.engine.spi.EntityInfo;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.hibernate.search.query.engine.spi.QueryDescriptor;
import org.hibernate.search.query.engine.spi.impl.LuceneQueryDescriptor;
import org.hibernate.search.spatial.Coordinates;
import org.hibernate.search.testsupport.TestForIssue;
import org.hibernate.search.testsupport.junit.SearchFactoryHolder;
import org.hibernate.search.testsupport.setup.TransactionContextForTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Emmanuel Bernard
 * @author Yoann Rodiere
 */
@TestForIssue(jiraKey = "HSEARCH-1872")
public class SortDSLTest {
	@Rule
	public SearchFactoryHolder sfHolder = new SearchFactoryHolder( IndexedEntry.class );

	@Before
	public void prepareTestData() {
		storeData(
				new IndexedEntry(0)
				.setMainData(
						"frequent infrequent1 infrequent2 infrequent1 inMultipleDocsWithDifferentScore",
						1, 2d
				)
				/*
				 * Distances:
				 * - to (24,32) with arc method: 10.16km
				 * - to (24,32) with plane method: 11.12km (exact same as entry 1)
				 */
				.setLocation( 24.0d, 31.9d )
				.setMultiValued( 444, -1, 200 )
		);
		storeData(
				new IndexedEntry(1)
				.setMainData(
						"frequent inMultipleDocsWithDifferentScore inMultipleDocsWithDifferentScore inMultipleDocsWithDifferentScore",
						2, 1d
				)
				/*
				 * Distances:
				 * - to (24,32) with arc method: 11.12km
				 * - to (24,32) with plane method: 11.12km (exact same as entry 0)
				 */
				.setLocation( 23.9d, 32.0d )
				.setMultiValued( 4242, 23, 23 )
		);
		storeData(
				new IndexedEntry(2)
				.setMainData( null, 1, null)
		);
		storeData(
				new IndexedEntry(3)
				.setMainData(
						"frequent infrequent1 inMultipleDocsWithDifferentScore inMultipleDocsWithDifferentScore",
						1, 3d
				)
				/*
				 * Distances:
				 * - to (24,32) with arc method: 15.06km
				 * - to (24,32) with plane method: 15.73km
				 */
				.setLocation( 23.9d, 32.1d )
				.setMultiValued( 700, 799, 750 )
		);
	}

	private QueryBuilder builder() {
		return sfHolder.getSearchFactory().buildQueryBuilder().forEntity( IndexedEntry.class ).get();
	}

	@Test
	public void score() throws Exception {
		Query query = builder().keyword()
				.onField( "textField" )
				.matching( "infrequent1" )
				.createQuery();

		Sort sort = builder().sort()
				.byScore()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 3 )
		);

		sort = builder().sort()
				.byScore()
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0 )
		);

		sort = builder().sort()
				.byScore()
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 3 )
		);
	}

	@Test
	public void docID() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byIndexOrder()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 2, 3 )
		);

		sort = builder().sort()
				.byIndexOrder()
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 2, 3 )
		);

		sort = builder().sort()
				.byIndexOrder()
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 2, 1, 0 )
		);
	}

	@Test
	public void singleField() throws Exception {
		Query query = builder().all().createQuery();

		// Missing value is not provided; the missing values should be considered as 0

		Sort sort = builder().sort()
				.byField( "uniqueNumericField" )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 1, 0, 3 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 1, 0, 3 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 1, 2 )
		);
	}

	@Test
	public void singleField_missingValue_use() throws Exception {
		// TODO YR add the relevant API methods and code this test OR remove this test
		Assert.fail( "Missing API for defining missing value replacement without ignoring field bridge." );
	}

	@Test
	public void singleField_missingValue_useIgnoreFieldBridge() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "uniqueNumericField" )
						.onMissingValue().use( String.valueOf( 1.5d) ).ignoreFieldBridge()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 2, 0, 3 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.asc()
						.onMissingValue().use( String.valueOf( Calendar.APRIL) ).ignoreFieldBridge()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 2, 0, 3 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.desc()
						.onMissingValue().use( String.valueOf( Calendar.APRIL) ).ignoreFieldBridge()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 2, 1 )
		);
	}

	@Test
	public void singleField_missingValue_sortFirst() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "uniqueNumericField" )
						.onMissingValue().sortFirst()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 1, 0, 3 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.asc()
						.onMissingValue().sortFirst()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 1, 0, 3 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.desc()
						.onMissingValue().sortFirst()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 3, 0, 1 )
		);
	}

	@Test
	public void singleField_missingValue_sortLast() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "uniqueNumericField" )
						.onMissingValue().sortLast()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 0, 3, 2 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.asc()
						.onMissingValue().sortLast()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 0, 3, 2 )
		);

		sort = builder().sort()
				.byField( "uniqueNumericField" )
						.desc()
						.onMissingValue().sortLast()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 1, 2 )
		);
	}

	@Test
	public void multipleFields() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "nonUniqueNumericField" )
				.andByField( "uniqueNumericField" )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "nonUniqueNumericField" )
				.andByField( "uniqueNumericField" )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "nonUniqueNumericField" )
				.andByField( "uniqueNumericField" )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 2, 1 )
		);
	}

	@Test
	public void multiValuedField_min() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MIN )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 2, 1, 3 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MIN )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 2, 1, 3 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MIN )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 1, 2, 0 )
		);
	}

	@Test
	public void multiValuedField_max() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MAX )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MAX )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MAX )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 3, 0, 2 )
		);
	}

	@Test
	public void multiValuedField_avg() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.AVG )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.AVG )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.AVG )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 3, 0, 2 )
		);
	}

	@Test
	public void multiValuedField_median() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MEDIAN )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 1, 0, 3 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MEDIAN )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 1, 0, 3 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.MEDIAN )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 1, 2 )
		);
	}

	@Test
	public void multiValuedField_sum() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.SUM )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.SUM )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 0, 3, 1 )
		);

		sort = builder().sort()
				.byField( "multiValuedField" )
						.treatMultiValuedAs( MultiValuedMode.SUM )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 3, 0, 2 )
		);
	}

	@Test
	public void distance() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 3, 2 )
		);

		sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 3, 2 )
		);

		sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 3, 1, 0 )
		);
	}

	@Test
	public void distanceFromSpatialQuery() throws Exception {
		// TODO YR Make sure it's really clear in the docs that the "within" call serves no purpose in this case

		Query query = builder().spatial()
				.onField("location_hash")
						.within(51, Unit.KM).ofLatitude( 24 ).andLongitude( 32 )
				.createQuery();

		Sort sort = builder().sort()
				.byDistanceFromSpatialQuery( query )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 3 )
		);

		sort = builder().sort()
				.byDistanceFromSpatialQuery( query )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 3 )
		);

		sort = builder().sort()
				.byDistanceFromSpatialQuery( query )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 1, 0 )
		);
	}

	@Test
	public void distanceWithReturnedDistance() throws Exception {
		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.in( Unit.KM )
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 3, 2 )
		);

		sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 0, 1, 3, 2 )
		);

		sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 3, 1, 0 )
		);
	}

	@Test
	public void distanceWithPlaneComputeMethod() throws Exception {

		Query query = builder().all().createQuery();

		Sort sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.withComputeMethod( DistanceMethod.PLANE )
				.andByField( "idSort" )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 0, 3, 2 )
		);

		sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.withComputeMethod( DistanceMethod.PLANE )
						.asc()
				.andByField( "idSort" )
						.desc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 0, 3, 2 )
		);

		sort = builder().sort()
				.byField( "location_hash" )
						.fromLatitude( 24 ).andLongitude( 32 )
						.withComputeMethod( DistanceMethod.PLANE )
						.desc()
				.andByField( "idSort" )
						.asc()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 2, 3, 0, 1 )
		);
	}

	@Test
	public void nativeLucene() throws Exception {
		// TODO YR code this test OR remove this test and remove "byNative" methods from the API.
		Assert.fail( "API to be removed? native lucene sort" );
	}

	@Test
	public void fieldThenScore() throws Exception {
		Query query = builder().keyword()
				.onField( "textField" )
				.matching( "inMultipleDocsWithDifferentScore" )
				.createQuery();

		Sort sort = builder().sort()
				.byField( "nonUniqueNumericField" )
				.andByScore()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 1 )
		);

		sort = builder().sort()
				.byField( "nonUniqueNumericField" )
						.asc()
				.andByScore()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 3, 0, 1 )
		);

		sort = builder().sort()
				.byField( "nonUniqueNumericField" )
						.desc()
				.andByScore()
				.createSort();
		assertThat(
				query( query, sort ),
				returnsIDsInOrder( 1, 3, 0, 2 )
		);
	}

	@Test
	public void scoreThenField() throws Exception {
		// TODO YR add the relevant API methods and code this test OR remove this test
		Assert.fail( "Missing API for chaining chain a field sort after a score sort." );
	}

	@Test
	public void withComparator() throws Exception {
		// TODO YR code this test OR remove this test and remove "withComparator" methods from the API.
		Assert.fail( "API to be removed? withComparator()" );
		builder().sort().byField( "foo" ).withComparator( null ).createSort();
	}


	private Matcher<List<EntityInfo>> returnsIDsInOrder(Integer ... idsInOrder) {
		final List<Integer> idsInOrderList = Arrays.asList( idsInOrder );
		return new TypeSafeMatcher<List<EntityInfo>>() {
			@Override
			public void describeTo(Description description) {
				description.appendText( "a list containing exactly (and in the same order) " )
						.appendValue( idsInOrderList );
			}
			@Override
			protected void describeMismatchSafely(List<EntityInfo> item, Description mismatchDescription) {
				mismatchDescription.appendText( "was " ).appendValue( toIds( item ) );
			}

			private List<Object> toIds(List<EntityInfo> entityInfos) {
				List<Object> result = new ArrayList<>();
				for ( EntityInfo entityInfo : entityInfos ) {
					result.add( entityInfo.getProjection()[0] );
				}
				return result;
			}

			@Override
			protected boolean matchesSafely(List<EntityInfo> actual) {
				return idsInOrderList.equals( toIds( actual ) );
			}
		};
	}

	private List<EntityInfo> query(Query luceneQuery, Sort sort) {
		HSQuery hsQuery = getHSQuery( luceneQuery );
		return hsQuery
				.targetedEntities( Arrays.asList( new Class<?>[] { IndexedEntry.class } ) )
				.projection( "id" )
				.sort( sort )
				.queryEntityInfos();
	}

	private HSQuery getHSQuery(Query luceneQuery) {
		ExtendedSearchIntegrator sf = sfHolder.getSearchFactory();
		QueryDescriptor descriptor = null;
		try {
			ServiceReference<LuceneQueryTranslator> translator =
					sf.getServiceManager().requestReference( LuceneQueryTranslator.class );
			if ( translator.get().conversionRequired( IndexedEntry.class ) ) {
				descriptor = translator.get().convertLuceneQuery( luceneQuery );
			}
		}
		catch (SearchException e) {
			// Ignore: no translator found
		}

		if ( descriptor == null ) {
			descriptor = new LuceneQueryDescriptor( luceneQuery );
		}

		return descriptor.createHSQuery( sf );
	}

	private void storeData(IndexedEntry entry) {
		Work work = new Work( entry, entry.id, WorkType.ADD, false );
		TransactionContextForTest tc = new TransactionContextForTest();
		sfHolder.getSearchFactory().getWorker().performWork( work, tc );
		tc.end();
	}

	public static class CollectionOfIntegersFieldBridge
			implements org.hibernate.search.bridge.FieldBridge, StringBridge {
		private final NumericFieldBridge delegate = NumericFieldBridge.INT_FIELD_BRIDGE;

		@Override
		public String objectToString(Object object) {
			return delegate.objectToString( object );
		}
		@Override
		public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
			if ( value == null ) {
				return;
			}
			if ( !( value instanceof Collection ) ) {
				throw new IllegalArgumentException( "This FieldBridge only supports collections." );
			}
			Collection<?> objects = (Collection<?>) value;

			for ( Object object : objects ) {
				delegate.set( name, object, document, luceneOptions );
			}
		}
	}

	@Indexed
	@Spatial(name = "location_hash", spatialMode = SpatialMode.HASH)
	public static class IndexedEntry implements Coordinates {

		@DocumentId
		@Field(name = "idSort")
		@SortableField(forField = "idSort")
		int id;

		@Field
		String textField;

		@Field
		@SortableField
		Integer nonUniqueNumericField;

		@Field
		@SortableField
		Double uniqueNumericField;

		@Field(bridge = @FieldBridge(impl = CollectionOfIntegersFieldBridge.class))
		@SortableField
		Collection<Integer> multiValuedField;

		Double latitude;

		Double longitude;

		public IndexedEntry() {
		}

		public IndexedEntry(int id) {
			super();
			this.id = id;
		}

		@Override
		public Double getLatitude() {
			return latitude;
		}

		@Override
		public Double getLongitude() {
			return longitude;
		}

		public IndexedEntry setMainData(String textField, Integer nonUniqueNumericField, Double uniqueNumericField) {
			this.textField = textField;
			this.nonUniqueNumericField = nonUniqueNumericField;
			this.uniqueNumericField = uniqueNumericField;
			return this;
		}

		public IndexedEntry setLocation(Double latitude, Double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
			return this;
		}

		public IndexedEntry setMultiValued(Integer ... integers) {
			this.multiValuedField = Arrays.asList( integers );
			return this;
		}
	}
}
