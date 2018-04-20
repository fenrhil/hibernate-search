/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.types.formatter.impl;

import java.util.Objects;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.hibernate.search.v6poc.backend.document.model.Sortable;
import org.hibernate.search.v6poc.backend.lucene.document.impl.LuceneDocumentBuilder;
import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneFieldFormatter;
import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneIndexSchemaObjectNode;
import org.hibernate.search.v6poc.backend.lucene.util.impl.AnalyzerUtils;

public final class StringFieldFormatter implements LuceneFieldFormatter<String> {

	private final Sortable sortable;

	private final FieldType fieldType;

	private final Analyzer normalizer;

	public StringFieldFormatter(Sortable sortable, FieldType fieldType, Analyzer normalizer) {
		this.sortable = sortable;
		this.fieldType = fieldType;
		this.normalizer = normalizer;
	}

	@Override
	public void addFields(LuceneDocumentBuilder documentBuilder, LuceneIndexSchemaObjectNode parentNode, String fieldName, String value) {
		if ( value == null ) {
			return;
		}

		documentBuilder.addField( parentNode, new Field( fieldName, value, fieldType ) );

		if ( Sortable.YES.equals( sortable ) ) {
			documentBuilder.addField( parentNode, new SortedDocValuesField(
					fieldName,
					new BytesRef( normalizer != null ? AnalyzerUtils.analyzeSortableValue( normalizer, fieldName, value ) : value )
			) );
		}
	}

	@Override
	public Object format(Object value) {
		return value;
	}

	@Override
	public String parse(Document document, String fieldName) {
		return document.get( fieldName );
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( StringFieldFormatter.class != obj.getClass() ) {
			return false;
		}

		StringFieldFormatter other = (StringFieldFormatter) obj;

		return Objects.equals( sortable, other.sortable ) &&
				Objects.equals( fieldType, other.fieldType ) &&
				Objects.equals( normalizer, other.normalizer );
	}

	@Override
	public int hashCode() {
		return Objects.hash( sortable, fieldType, normalizer );
	}
}
