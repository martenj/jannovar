package de.charite.compbio.jannovar.datasource;

import org.ini4j.Profile.Section;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.JannovarOptions;

/**
 * {@link DataSource} implementation for data from Ensembl.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
final class EnsemblDataSource extends DataSource {

	/** expected keys in data source configuration file */
	private final ImmutableList<String> urlKeys = ImmutableList.of("cdna", "gtf", "chromInfo", "chrToAccessions");

	EnsemblDataSource(JannovarOptions options, Section iniSection)
			throws InvalidDataSourceException {
		super(options, iniSection);

		checkURLs();
	}

	@Override
	public JannovarDataFactory getDataFactory() {
		return new EnsemblJannovarDataFactory(options, this, iniSection, options.printProgressBars);
	}

	@Override
	protected ImmutableList<String> getURLKeys() {
		return urlKeys;
	}

}
