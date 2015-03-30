package de.charite.compbio.jannovar.annotation.builders;

import java.util.ArrayList;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationMessage;
import de.charite.compbio.jannovar.annotation.InvalidGenomeChange;
import de.charite.compbio.jannovar.annotation.VariantType;
import de.charite.compbio.jannovar.impl.util.StringUtil;
import de.charite.compbio.jannovar.impl.util.Translator;
import de.charite.compbio.jannovar.reference.CDSPosition;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.ProjectionException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptPosition;
import de.charite.compbio.jannovar.reference.TranscriptSequenceDecorator;

// TODO(holtgrew): Mutations near splice sites should be annotated as "p.?" as Mutalyzer does.

/**
 * Builds {@link Annotation} objects for the SNV {@link GenomeChange}s in the given {@link TranscriptInfo}.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public final class SNVAnnotationBuilder extends AnnotationBuilder {

	/**
	 * Override substitution annotation string in the case of coding change.
	 *
	 * For changes in coding regions, this is necessary since the transcript might not be the same as the reference
	 * (that the VCF file is generated from).
	 */
	private String hgvsSNVOverride = null;

	/**
	 * @param transcript
	 *            {@link TranscriptInfo} to build the annotation for
	 * @param change
	 *            {@link GenomeChange} to build the annotation with
	 * @throws InvalidGenomeChange
	 *             if <code>change</code> did not describe a deletion
	 */
	SNVAnnotationBuilder(TranscriptModel transcript, GenomeChange change) throws InvalidGenomeChange {
		super(transcript, change);

		// guard against invalid genome change
		if (change.ref.length() != 1 || change.alt.length() != 1)
			throw new InvalidGenomeChange("GenomeChange " + change + " does not describe a SNV.");
	}

	@Override
	public Annotation build() {
		// Go through top-level cases (clustered by how they are handled here) and build annotations for each of them
		// where applicable.

		if (!transcript.isCoding())
			return buildNonCodingAnnotation();

		final GenomeInterval changeInterval = change.getGenomeInterval();
		if (so.liesInCDSExon(changeInterval) && transcript.cdsRegion.contains(changeInterval))
			return buildCDSExonicAnnotation(); // lies in coding part of exon
		else if (so.overlapsWithCDSIntron(changeInterval) && so.overlapsWithCDS(changeInterval))
			return buildIntronicAnnotation(); // intron but no exon => intronic variant
		else if (so.overlapsWithFivePrimeUTR(changeInterval) || so.overlapsWithThreePrimeUTR(changeInterval))
			return buildUTRAnnotation();
		else if (so.overlapsWithUpstreamRegion(changeInterval) || so.overlapsWithDownstreamRegion(changeInterval))
			return buildUpOrDownstreamAnnotation();
		else
			return buildIntergenicAnnotation();
	}

	private Annotation buildCDSExonicAnnotation() {
		// Get 0-based transcript and CDS positions.
		TranscriptPosition txPos;
		CDSPosition cdsPos;
		try {
			txPos = projector.genomeToTranscriptPos(change.pos);
			cdsPos = projector.genomeToCDSPos(change.pos);
		} catch (ProjectionException e) {
			throw new Error("Bug: CDS exon position must be translatable to transcript position");
		}

		// Check that the WT nucleotide from the transcript is consistent with change.ref and generate a warning message
		// if this is not the case.
		messages.add(AnnotationMessage.WARNING_REF_DOES_NOT_MATCH_GENOME);

		// Compute the frame shift and codon start position.
		int frameShift = cdsPos.pos % 3;
		// Get the transcript codon. From this, we generate the WT and the variant codon. This is important in the case
		// where the transcript differs from the reference. This inconsistency of the reference and the transcript is
		// not necessarily an error in the data base but can also occur in the case of post-transcriptional changes of
		// the transcript.
		String transcriptCodon = seqDecorator.getCodonAt(txPos, cdsPos);
		String wtCodon = TranscriptSequenceDecorator.codonWithUpdatedBase(transcriptCodon, frameShift,
				change.ref.charAt(0));
		String varCodon = TranscriptSequenceDecorator.codonWithUpdatedBase(transcriptCodon, frameShift,
				change.alt.charAt(0));

		// Construct the HGSV annotation parts for the transcript location and nucleotides (note that HGSV uses 1-based
		// positions).
		char wtNT = wtCodon.charAt(frameShift); // wild type nucleotide
		char varNT = varCodon.charAt(frameShift); // wild type amino acid
		hgvsSNVOverride = StringUtil.concatenate(wtNT, ">", varNT);

		// Construct annotation part for the protein.
		String wtAA = Translator.getTranslator().translateDNA3(wtCodon);
		String varAA = Translator.getTranslator().translateDNA3(varCodon);
		String protAnno = StringUtil.concatenate("p.", wtAA, cdsPos.pos / 3 + 1, varAA);
		if (wtAA.equals(varAA)) // simplify in the case of synonymous SNV
			protAnno = StringUtil.concatenate("p.=");

		// Compute variant type.
		ArrayList<VariantType> varTypes = computeVariantTypes(wtAA, varAA);
		GenomeInterval changeInterval = change.getGenomeInterval();
		if (so.overlapsWithTranslationalStartSite(changeInterval)) {
			varTypes.add(VariantType.START_LOSS);
			protAnno = "p.0?";
		} else if (so.overlapsWithTranslationalStopSite(changeInterval)) {
			if (wtAA.equals(varAA)) { // change in stop codon, but no AA change
				varTypes.add(VariantType.STOP_RETAINED);
			} else { // change in stop codon, AA change
				varTypes.add(VariantType.STOPLOSS);
				String varNTString = seqChangeHelper.getCDSWithChange(change);
				String varAAString = Translator.getTranslator().translateDNA(varNTString);
				int stopCodonPos = varAAString.indexOf('*', cdsPos.pos / 3);
				protAnno = StringUtil.concatenate(protAnno, "ext*", stopCodonPos - cdsPos.pos / 3);
			}
		}
		// Check for being a splice site variant. The splice donor, acceptor, and region intervals are disjoint.
		if (so.overlapsWithSpliceDonorSite(changeInterval))
			varTypes.add(VariantType.SPLICE_DONOR);
		else if (so.overlapsWithSpliceAcceptorSite(changeInterval))
			varTypes.add(VariantType.SPLICE_ACCEPTOR);
		else if (so.overlapsWithSpliceRegion(changeInterval))
			varTypes.add(VariantType.SPLICE_REGION);

		// Build the resulting Annotation.
		return new Annotation(transcript, change, varTypes, locAnno, ncHGVS(), protAnno);
	}

	@Override
	protected String ncHGVS() {
		if (hgvsSNVOverride == null)
			return StringUtil.concatenate(dnaAnno, change.ref, ">", change.alt);
		else
			return StringUtil.concatenate(dnaAnno, hgvsSNVOverride);
	}

	/**
	 * @param wtAA
	 *            wild type amino acid
	 * @param varAA
	 *            variant amino acid
	 * @return variant types described by single nucleotide change
	 */
	private ArrayList<VariantType> computeVariantTypes(String wtAA, String varAA) {
		ArrayList<VariantType> result = new ArrayList<VariantType>();
		if (wtAA.equals(varAA))
			result.add(VariantType.SYNONYMOUS);
		else if (wtAA.equals("*"))
			result.add(VariantType.STOPLOSS);
		else if (varAA.equals("*"))
			result.add(VariantType.STOPGAIN);
		else
			result.add(VariantType.MISSENSE);
		return result;
	}

}
