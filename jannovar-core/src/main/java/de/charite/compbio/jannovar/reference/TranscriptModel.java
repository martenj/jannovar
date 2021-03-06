package de.charite.compbio.jannovar.reference;

import java.io.Serializable;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.Immutable;

/**
 * The information representing a transcript model.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@Immutable
public final class TranscriptModel implements Serializable, Comparable<TranscriptModel> {

	/**
	 * Accession number of the transcript (e.g., the UCSC knownGene id - uc011nca.2). The version number may be
	 * included.
	 */
	public final String accession;

	/**
	 * Gene symbol of the known Gene. Can be null for some genes. Note that in annovar, $name2 corresponds to the
	 * geneSymbol if available, otherwise the kgID is used.
	 */
	public final String geneSymbol;

	/** Genomic interval with transcript begin/end. */
	public final GenomeInterval txRegion;

	/**
	 * Genomic interval with CDS begin/end.
	 *
	 * @note Note that in Jannovar, the CDS region includes the start and stop codon.
	 */
	public final GenomeInterval cdsRegion;

	/** Genomic intervals with the exons, order is dictated by strand of transcript. */
	public final ImmutableList<GenomeInterval> exonRegions;

	/** cDNA sequence of the spliced RNA of this known gene transcript. */
	public final String sequence;

	/**
	 * The gene ID, from Ensembl (<code>"ENS[MUS]*G0+([0-9]+)"</code>), Entrez ("<code>ENTREZ([0-9]+)</code>
	 * "), RefSeq ("<code>gene([0-9]+)</code>").
	 *
	 * <code>null</code> for no available gene ID.
	 */
	public final String geneID;

	/**
	 * The transcript support level of the this transcript (the lower the better).
	 *
	 * @see TranscriptSupportLevels
	 * @see {@link http://www.ensembl.org/Help/Glossary?id=492}
	 */
	public final int transcriptSupportLevel;

	/** Class version (for serialization). */
	public static final long serialVersionUID = 3L;

	/**
	 * Initialize the TranscriptInfo object from the given parameters.
	 */
	public TranscriptModel(String accession, String geneSymbol, GenomeInterval txRegion, GenomeInterval cdsRegion,
			ImmutableList<GenomeInterval> exonRegions, String sequence, String geneID, int transcriptSupportLevel) {
		this.accession = accession;
		this.geneSymbol = geneSymbol;
		this.txRegion = txRegion;
		this.cdsRegion = cdsRegion;
		this.exonRegions = exonRegions;
		this.sequence = sequence;
		this.geneID = geneID;
		this.transcriptSupportLevel = transcriptSupportLevel;
		checkForConsistency();
	}

	/** @return the strand of the transcript */
	public char getStrand() {
		return txRegion.strand;
	}

	/** @return the chromosome of the transcript */
	public int getChr() {
		return txRegion.chr;
	}

	/**
	 * @return <tt>true</tt> if this is a gene-coding transcript, marked by <tt>cdsRegion</tt> being empty.
	 */
	public boolean isCoding() {
		return (this.cdsRegion.beginPos < this.cdsRegion.endPos);
	}

	/**
	 * @return the length of the coding exon sequence
	 */
	public int cdsTranscriptLength() {
		int result = 0;
		for (GenomeInterval region : exonRegions)
			result += region.intersection(cdsRegion).length();
		return result;
	}

	/**
	 * @return the sum of the exon sequence lengths
	 */
	public int transcriptLength() {
		int result = 0;
		for (GenomeInterval region : exonRegions)
			result += region.length();
		return result;
	}

	/**
	 * @param i
	 *            0-based index of the intron's region to return
	 * @return {@link GenomeInterval} with the intron's region
	 */
	public GenomeInterval intronRegion(int i) {
		// TODO(holtgrem): test me!
		GenomeInterval exonRegionL = exonRegions.get(i);
		GenomeInterval exonRegionR = exonRegions.get(i + 1);
		return new GenomeInterval(exonRegionL.refDict, exonRegionL.strand, exonRegionL.chr, exonRegionL.endPos,
				exonRegionR.beginPos, PositionType.ZERO_BASED);
	}

	/**
	 * Ensures that the strands are consistent.
	 */
	private void checkForConsistency() {
		char strand = txRegion.strand;
		assert (txRegion.strand == strand);
		assert (cdsRegion.strand == strand);
		for (GenomeInterval region : exonRegions)
			assert (region.strand == strand);
	}

	@Override
	public String toString() {
		return accession + "(" + txRegion + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accession == null) ? 0 : accession.hashCode());
		result = prime * result + ((cdsRegion == null) ? 0 : cdsRegion.hashCode());
		result = prime * result + ((exonRegions == null) ? 0 : exonRegions.hashCode());
		result = prime * result + ((geneID == null) ? 0 : geneID.hashCode());
		result = prime * result + ((geneSymbol == null) ? 0 : geneSymbol.hashCode());
		result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
		result = prime * result + transcriptSupportLevel;
		result = prime * result + ((txRegion == null) ? 0 : txRegion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TranscriptModel other = (TranscriptModel) obj;
		if (accession == null) {
			if (other.accession != null)
				return false;
		} else if (!accession.equals(other.accession))
			return false;
		if (cdsRegion == null) {
			if (other.cdsRegion != null)
				return false;
		} else if (!cdsRegion.equals(other.cdsRegion))
			return false;
		if (exonRegions == null) {
			if (other.exonRegions != null)
				return false;
		} else if (!exonRegions.equals(other.exonRegions))
			return false;
		if (geneID == null) {
			if (other.geneID != null)
				return false;
		} else if (!geneID.equals(other.geneID))
			return false;
		if (geneSymbol == null) {
			if (other.geneSymbol != null)
				return false;
		} else if (!geneSymbol.equals(other.geneSymbol))
			return false;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		if (transcriptSupportLevel != other.transcriptSupportLevel)
			return false;
		if (txRegion == null) {
			if (other.txRegion != null)
				return false;
		} else if (!txRegion.equals(other.txRegion))
			return false;
		return true;
	}

	@Override
	public int compareTo(TranscriptModel o) {
		int result = -1;
		if (geneID != null && o.geneID != null) {
			result = geneID.compareTo(o.geneID);
			if (result != 0)
				return result;
		}

		result = geneSymbol.compareTo(o.geneSymbol);
		if (result != 0)
			return result;

		return accession.compareTo(o.accession);
	}

}
