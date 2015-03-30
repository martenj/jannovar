package de.charite.compbio.jannovar.filter;

import htsjdk.variant.variantcontext.writer.VariantContextWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.charite.compbio.jannovar.JannovarFilterCommandLineParser;

/**
 * Write results to a {@link VariantContextWriter}
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class WriterFilter implements VariantContextFilter {

	/** the logger object to use */
	private static final Logger LOGGER = LoggerFactory.getLogger(JannovarFilterCommandLineParser.class);

	/** the {@link VariantContextWriter} to use for writing out */
	private final VariantContextWriter writer;

	/** Initialize with the given {@link VariantContextWriter}. */
	public WriterFilter(VariantContextWriter writer) {
		this.writer = writer;
	}

	@Override
	public void put(FlaggedVariant fv) throws FilterException {
		LOGGER.trace("Variant added to writer {} => included? {}", new Object[] { fv.vc, fv.isIncluded() });
		if (fv.isIncluded())
			writer.add(fv.vc);
	}

	@Override
	public void finish() {
		/* no-op */
	}

}
