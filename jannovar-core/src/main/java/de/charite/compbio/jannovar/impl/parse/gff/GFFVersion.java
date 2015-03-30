package de.charite.compbio.jannovar.impl.parse.gff;

import de.charite.compbio.jannovar.Immutable;

/**
 * Wraps information about the version of a GFF/GTF file and allows to query format specific values.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@Immutable
public final class GFFVersion {

	/** the version */
	public final int version;
	/** the string to use for separating attributes */
	public final String valueSeparator;

	public GFFVersion(int version) {
		this.version = version;
		this.valueSeparator = (version == 3) ? "=" : " ";
	}

	@Override
	public String toString() {
		return Integer.toString(version);
	}

}