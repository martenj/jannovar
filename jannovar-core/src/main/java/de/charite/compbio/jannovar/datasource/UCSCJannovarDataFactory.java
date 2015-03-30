package de.charite.compbio.jannovar.datasource;

import org.ini4j.Profile.Section;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.impl.parse.TranscriptParseException;
import de.charite.compbio.jannovar.impl.parse.UCSCParser;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Creation of {@link JannovarData} objects from a {@link UCSCDataSource}.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
final class UCSCJannovarDataFactory extends JannovarDataFactory {

	/**
	 * Construct the factory with the given {@link UCSCDataSource}.
	 *
	 * @param options
	 *            configuration for proxy settings
	 * @param dataSource
	 *            the data source to use.
	 * @param iniSection
	 *            {@link Section} with configuration from INI file
	 */
	public UCSCJannovarDataFactory(JannovarOptions options, UCSCDataSource dataSource, Section iniSection) {
		super(options, dataSource, iniSection);
	}

	@Override
	protected ImmutableList<TranscriptModel> parseTranscripts(ReferenceDictionary refDict, String targetDir)
			throws TranscriptParseException {
		return new UCSCParser(refDict, targetDir, iniSection).run();
	}

}
