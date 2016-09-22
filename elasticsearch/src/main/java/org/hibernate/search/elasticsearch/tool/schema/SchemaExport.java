/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.tool.schema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.search.cfg.SearchMapping;
import org.hibernate.search.cfg.impl.SearchConfigurationFromHibernateCore;
import org.hibernate.search.cfg.spi.SearchConfigurationBase;
import org.hibernate.search.elasticsearch.logging.impl.Log;
import org.hibernate.search.elasticsearch.schema.impl.ElasticsearchSchemaTranslator;
import org.hibernate.search.elasticsearch.tool.schema.impl.SchemaExportSearchConfiguration;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.engine.service.classloading.spi.ClassLoaderService;
import org.hibernate.search.engine.service.impl.StandardServiceManager;
import org.hibernate.search.engine.service.spi.Service;
import org.hibernate.search.engine.service.spi.ServiceManager;
import org.hibernate.search.engine.service.spi.ServiceReference;
import org.hibernate.search.exception.AssertionFailure;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.spi.SearchIntegratorBuilder;
import org.hibernate.search.util.logging.impl.LoggerFactory;
import org.hibernate.service.ServiceRegistry;

/**
 * Command-line tool for exporting an Elasticsearch schema (mappings) to JSON files.
 *
 * <p>The tool will take a directory as parameter, and will generate one directory for each
 * index, and one file in each directory for each mapping in this index.
 *
 * @author Yoann Rodiere
 */
public class SchemaExport {

	private static final Log LOG = LoggerFactory.make( Log.class );

	public static final class Builder {
		private Path outputDirectory = null;
		private boolean prettyPrint = false;
//		private Metadata metadata;
		private ExtendedSearchIntegrator searchIntegrator;

		private boolean frozen = false;

//		/**
//		 * Sets the metadata that should be used for discovering entities.
//		 * @param metadata
//		 * @return this
//		 */
//		public Builder metadata(Metadata metadata) {
//			checkNotFrozen();
//			this.metadata = metadata;
//			return this;
//		}

		public Builder searchIntegrator(SearchIntegrator searchIntegrator) {
			checkNotFrozen();
			this.searchIntegrator = (ExtendedSearchIntegrator) searchIntegrator;
			return this;
		}

		/**
		 * Sets the directory to write the JSON mapping files to.
		 * @param outputDirectory
		 * @return this
		 */
		public Builder outputDirectory(Path outputDirectory) {
			checkNotFrozen();
			this.outputDirectory = outputDirectory;
			return this;
		}

		/**
		 * Sets whether JSON files should be pretty-printed (formatted for humans).
		 * @param prettyPrint
		 * @return this
		 */
		public Builder prettyPrint(boolean prettyPrint) {
			checkNotFrozen();
			this.prettyPrint = prettyPrint;
			return this;
		}

		private void checkNotFrozen() {
			if ( frozen ) {
				throw new AssertionFailure( "Cannot use a builder after having called 'build()'" );
			}
		}

		public SchemaExport build() {
			frozen = true;
			return new SchemaExport(this);
		}
	}

	private final Builder builder;

	private SchemaExport(Builder builder) {
		super();
		this.builder = builder;
	}

	public void execute() {
		LOG.schemaExportStarting();

//		ServiceRegistry serviceRegistry = ( (MetadataImplementor) builder.metadata ).getMetadataBuildingOptions().getServiceRegistry();
//
//		Map config = new HashMap();
//		config.putAll( serviceRegistry.getService( ConfigurationService.class ).getSettings() );

		// TODO start minimal services (no DB or ES connection)
		ServiceManager manager = builder.searchIntegrator.getServiceManager();
		try ( ServiceReference<ElasticsearchSchemaTranslator> translatorRef = manager.requestReference( ElasticsearchSchemaTranslator.class ) ) {
			ElasticsearchSchemaTranslator translator = translatorRef.get();
			for (indexBinding : builder.searchIntegrator.getDocu) {

			}
		}

		// TODO retrieve IndexMetadatas
		serviceRegistry.getService( serviceRole )

		// TODO export IndexMetadatas
	}

	public static void main(String[] args) {
		try {
			final CommandLineArgs commandLineArgs = CommandLineArgs.parse( args );
//			StandardServiceRegistry serviceRegistry = buildStandardServiceRegistry( commandLineArgs );
			try ( SearchIntegrator searchIntegrator = buildSearchIntegrator( commandLineArgs ) ){
				fromCommandLineArgs( commandLineArgs, searchIntegrator ).execute();
			}
		}
		catch (RuntimeException | IOException e) {
			LOG.schemaExportFailed( e );
			System.exit( 1 );
		}
	}

	private static SchemaExport fromCommandLineArgs(CommandLineArgs commandLineArgs, SearchIntegrator searchIntegrator) {
		return new Builder()
				.setOutputDirectory( commandLineArgs.outputDirectory )
				.setPrettyPrint( commandLineArgs.prettyPrint )
				.setSearchIntegrator( searchIntegrator )
				.build();
	}

//	private static StandardServiceRegistry buildStandardServiceRegistry(CommandLineArgs commandLineArgs) throws IOException {
//		final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
//		final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );
//
//		if ( commandLineArgs.cfgXmlFile != null ) {
//			ssrBuilder.configure( commandLineArgs.cfgXmlFile.toFile() );
//		}
//
//		Properties properties = new Properties();
//		if ( commandLineArgs.propertiesFile != null ) {
//			properties.load( Files.newInputStream( commandLineArgs.propertiesFile ) );
//		}
//		ssrBuilder.applySettings( properties );
//
//		return ssrBuilder.build();
//	}
//
//	private static MetadataImplementor buildMetadata(CommandLineArgs parsedArgs, StandardServiceRegistry serviceRegistry) {
//		final MetadataSources metadataSources = new MetadataSources( serviceRegistry );
//
//		for ( Path path : parsedArgs.hbmXmlFiles ) {
//			metadataSources.addFile( path.toFile() );
//		}
//
//		for ( Path path : parsedArgs.jarFiles ) {
//			metadataSources.addJar( path.toFile() );
//		}
//
//		final MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();
//
//		return (MetadataImplementor) metadataBuilder.build();
//	}

	private static SearchIntegrator buildSearchIntegrator(CommandLineArgs commandLineArgs)
			throws IOException {
		SearchIntegratorBuilder builder = new SearchIntegratorBuilder()
				.configuration( new SchemaExportSearchConfiguration() );
		// TODO allow adding classes explicitly?
//		for (Class<?> clazz : classes) {
//			builder = builder.addClass( clazz );
//		}
		return builder.buildSearchIntegrator();
	}

	private static class CommandLineArgs {
		private static final Pattern LONG_OPTION_PATTERN = Pattern.compile( "--(\\p{Alnum}+)(=(.*))?" );
		private static final int LONG_OPTION_PATTERN_OPTION_GROUP = 1;
		private static final int LONG_OPTION_PATTERN_VALUE_GROUP = 3;

		private Path outputDirectory = null;
		private boolean prettyPrint = false;

		private Path propertiesFile = null;
		private Path cfgXmlFile = null;

		private List<Path> hbmXmlFiles = new ArrayList<Path>();
		private List<Path> jarFiles = new ArrayList<Path>();

		public static CommandLineArgs parse(String[] args) {
			CommandLineArgs parsedArgs = new CommandLineArgs();

			for ( String arg : args ) {
				Matcher longOptionMatcher = LONG_OPTION_PATTERN.matcher( arg );
				if ( longOptionMatcher.matches() ) {
					String option = longOptionMatcher.group( LONG_OPTION_PATTERN_OPTION_GROUP );
					String value = longOptionMatcher.group( LONG_OPTION_PATTERN_VALUE_GROUP );
					switch ( option ) {
						case "output":
							checkNotBlank( option, value );
							parsedArgs.outputDirectory = Paths.get( value );
							break;
						case "properties":
							checkNotBlank( option, value );
							parsedArgs.propertiesFile = Paths.get( value );
							break;
						case "config":
							checkNotBlank( option, value );
							parsedArgs.propertiesFile = Paths.get( value );
							break;
						case "format":
						case "pretty":
							checkBlank( option, value );
							parsedArgs.prettyPrint = true;
							break;
					}
				}
				else {
					if ( arg.endsWith( ".jar" ) ) {
						parsedArgs.jarFiles.add( Paths.get( arg ) );
					}
					else {
						parsedArgs.hbmXmlFiles.add( Paths.get( arg ) );
					}
				}
			}

			return parsedArgs;
		}

		private static void checkBlank(String option, String value) {
			if ( value != null && !value.isEmpty() ) {
				throw new AssertionFailure( "No value expected for option '--" + option + "'" );
			}
		}

		private static void checkNotBlank(String option, String value) {
			if ( value == null || value.isEmpty() ) {
				throw new AssertionFailure( "Missing value for option '--" + option + "'" );
			}
		}
	}

}
