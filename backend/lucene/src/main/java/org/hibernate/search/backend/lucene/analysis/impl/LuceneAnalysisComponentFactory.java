/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.analysis.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.search.engine.environment.classpath.spi.JavaPath;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.Version;

/**
 * Instances of this class are used to create Lucene analyzers, normalizers, tokenizers, char filters and token filters.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class LuceneAnalysisComponentFactory {

	private static final String LUCENE_VERSION_PARAM = "luceneMatchVersion";

	private static final KeywordTokenizerFactory KEYWORD_TOKENIZER_FACTORY =
			new KeywordTokenizerFactory( Collections.emptyMap() );

	private final Version luceneMatchVersion;
	private final JavaPath javaPath;
	private final ResourceLoader resourceLoader;

	public LuceneAnalysisComponentFactory(Version luceneMatchVersion, JavaPath javaPath) {
		this.luceneMatchVersion = luceneMatchVersion;
		this.javaPath = javaPath;
		this.resourceLoader = new HibernateSearchResourceLoader( javaPath );
	}

	public Analyzer createAnalyzer(TokenizerFactory tokenizerFactory,
			CharFilterFactory[] charFilterFactories, TokenFilterFactory[] filterFactories) {
		return new TokenizerChain( charFilterFactories, tokenizerFactory, filterFactories );
	}

	public Analyzer createNormalizer(String name,
			CharFilterFactory[] charFilterFactories, TokenFilterFactory[] filterFactories) {
		Analyzer normalizer = new TokenizerChain( charFilterFactories, KEYWORD_TOKENIZER_FACTORY, filterFactories );
		return wrapNormalizer( name, normalizer );
	}

	public Analyzer wrapNormalizer(String name, Analyzer normalizer) {
		return new HibernateSearchNormalizerWrapper( name, normalizer );
	}

	public TokenizerFactory createTokenizerFactory(Class<? extends TokenizerFactory> factoryClass,
			Map<String, String> parameters) throws IOException {
		return createAnalysisComponent( TokenizerFactory.class, factoryClass, parameters );
	}

	public CharFilterFactory createCharFilterFactory(Class<? extends CharFilterFactory> factoryClass,
			Map<String, String> parameters) throws IOException {
		return createAnalysisComponent( CharFilterFactory.class, factoryClass, parameters );
	}

	public TokenFilterFactory createTokenFilterFactory(Class<? extends TokenFilterFactory> factoryClass,
			Map<String, String> parameters) throws IOException {
		return createAnalysisComponent( TokenFilterFactory.class, factoryClass, parameters );
	}

	private <T> T createAnalysisComponent(Class<T> expectedFactoryClass,
			Class<? extends T> factoryClass, Map<String, String> parameters) throws IOException {
		final Map<String, String> tokenMapsOfParameters = getMapOfParameters( parameters, luceneMatchVersion );
		T tokenizerFactory = javaPath.instanceFromClass(
				expectedFactoryClass,
				factoryClass,
				expectedFactoryClass.getName(),
				tokenMapsOfParameters
		);
		injectResourceLoader( tokenizerFactory );
		return tokenizerFactory;
	}

	private void injectResourceLoader(Object processor) throws IOException {
		if ( processor instanceof ResourceLoaderAware ) {
			( (ResourceLoaderAware) processor ).inform( resourceLoader );
		}
	}

	private static Map<String, String> getMapOfParameters(Map<String, String> params, Version luceneMatchVersion) {
		Map<String, String> mapOfParams = new LinkedHashMap<>( params );
		params.put( LUCENE_VERSION_PARAM, luceneMatchVersion.toString() );
		return mapOfParams;
	}
}
