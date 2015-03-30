package de.charite.compbio.jannovar.datasource;

import org.ini4j.Profile.Section;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.impl.parse.RefSeqParser;
import de.charite.compbio.jannovar.impl.parse.TranscriptParseException;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.TranscriptModel;

// TODO(holtgrem): Report longest transcript as primary one for RefSeq.

/**
 * Creation of {@link JannovarData} objects from a {@link RefSeqDataSource}.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
final class RefSeqJannovarDataFactory extends JannovarDataFactory {

	/**
	 * Construct the factory with the given {@link RefSeqDataSource}.
	 *
	 * @param options
	 *            configuration for proxy settings
	 * @param dataSource
	 *            the data source to use.
	 * @param iniSection
	 *            {@link Section} with configuration from INI file
	 */
	public RefSeqJannovarDataFactory(JannovarOptions options, RefSeqDataSource dataSource, Section iniSection) {
		super(options, dataSource, iniSection);
	}

	@Override
	protected ImmutableList<TranscriptModel> parseTranscripts(ReferenceDictionary refDict, String targetDir)
			throws TranscriptParseException {
		return new RefSeqParser(refDict, targetDir, iniSection, options.printProgressBars).run();
	}

}
