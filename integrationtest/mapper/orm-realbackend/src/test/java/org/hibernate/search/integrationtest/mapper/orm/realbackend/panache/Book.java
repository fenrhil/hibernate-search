/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheElasticsearchQuerySelectStep;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.impl.PanacheElasticsearchSupport;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

@Entity(name = "book")
@Indexed
public class Book {

	public static PanacheElasticsearchQuerySelectStep<SearchLoadingOptionsStep> search() {
		return PanacheElasticsearchSupport.search( Book.class );
	}

	@Id
	private Integer id;

	@FullTextField
	private String title;

	@KeywordField
	@Basic(optional = false)
	private Genre genre;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}
}
