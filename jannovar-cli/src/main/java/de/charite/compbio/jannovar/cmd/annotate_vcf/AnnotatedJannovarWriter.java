package de.charite.compbio.jannovar.cmd.annotate_vcf;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.impl.util.PathUtil;
import de.charite.compbio.jannovar.io.Chromosome;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeChange;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.PositionType;

/**
 * Annotate variant in {@link VariantContext} and write out in Jannovar format.
 */
public class AnnotatedJannovarWriter extends AnnotatedVariantWriter {

	/** {@link ReferenceDictionary} object to use for information about the genome. */
	private final ReferenceDictionary refDict;

	/** the VCF file to process */
	private String vcfPath;

	/** options object */
	private JannovarOptions options;

	/** the VariantAnnotator to use. */
	private VariantAnnotator annotator;

	/** BufferedWriter to use for writing */
	BufferedWriter out = null;

	/** current line */
	int currentLine = 0;

	public AnnotatedJannovarWriter(ReferenceDictionary refDict, ImmutableMap<Integer, Chromosome> chromosomeMap,
			String vcfPath, JannovarOptions options) throws IOException {
		this.refDict = refDict;
		this.annotator = new VariantAnnotator(refDict, chromosomeMap);
		this.vcfPath = vcfPath;
		this.options = options;
		this.openBufferedWriter();
	}

	@Override
	public String getOutFileName() {
		// build file name for output file
		File f = new File(vcfPath);
		String outname = f.getName();
		if (options.outVCFFolder != null)
			outname = PathUtil.join(options.outVCFFolder, outname);
		int i = outname.lastIndexOf("vcf");
		if (i < 0)
			i = outname.lastIndexOf("VCF");
		if (i < 0)
			return f.getParent() + File.separator + outname + ".jv";
		else
			return f.getParent() + File.separator + outname.substring(0, i) + "jv";
	}

	/**
	 * Open the output file stream.
	 *
	 * @throws IOException
	 *             when opening the output file failed.
	 */
	private void openBufferedWriter() throws IOException {
		// try to open file
		try {
			FileWriter fstream = new FileWriter(getOutFileName());
			out = new BufferedWriter(fstream);
		} catch (IOException e) {
			close(); // swallows any exception thrown by this.out.close
			throw e; // rethrow e
		}
	}

	/** Close writer, free resources */
	@Override
	public void close() {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			// swallow, nothing we can do about it
		}
	}

	/**
	 * Write out record for VariantContext.
	 *
	 * @throws AnnotationException
	 *             when a problem with annotation occurs
	 * @throws IOException
	 *             when problem with I/O occurs
	 */
	@Override
	public void put(VariantContext vc) throws AnnotationException, IOException {
		currentLine++;

		String chrStr = vc.getChr();
		// Catch the case that vc.getChr() is not in ChromosomeMap.identifier2chromosom. This is the case
		// for the "random" contigs etc. In this case, we simply ignore the record.
		Integer boxedInt = refDict.contigID.get(vc.getChr());
		if (boxedInt == null)
			return;
		int chr = boxedInt.intValue();

		// FIXME(mjaeger): We should care about more than just the first alternative allele.
		// Get shortcuts to ref, alt, and position. Note that this is "uncorrected" data, common prefixes etc. are
		// stripped when constructing the GenomeChange.
		final String ref = vc.getReference().getBaseString();
		final String alt = vc.getAlternateAllele(0).getBaseString();
		final int pos = vc.getStart();
		// Construct GenomeChange from this and strip common prefixes.
		final GenomeChange change = new GenomeChange(
				new GenomePosition(refDict, '+', chr, pos, PositionType.ONE_BASED), ref, alt);

		String gtype = stringForGenotype(vc, 0);
		float qual = (float) vc.getPhredScaledQual();
		AnnotationList anno = annotator.buildAnnotationList(change);
		if (anno == null) {
			String e = String.format("No annotations found for variant %s", vc.toString());
			throw new AnnotationException(e);
		}

		for (Annotation a : anno.entries) {
			String effect = Joiner.on("+").join(a.effects);
			String annt = Joiner.on(":").skipNulls().join(a.ntHGVSDescription, a.aaHGVSDescription);
			String sym = a.transcript.geneSymbol;
			String s = String.format("%d\t%s\t%s\t%s\t%s\t%d\t%s\t%s\t%s\t%.1f\n", currentLine, effect, sym, annt,
					chrStr, change.pos, change.ref, change.alt, gtype, qual);
			out.write(s);
		}
	}

	/**
	 * Return genotype string as in VCF for the i-th individual at the position in variantContext.
	 *
	 * @param variantContext
	 *            The VariantContext to query.
	 * @param i
	 *            Index of individual.
	 * @return String with the genotype call string, e.g. "0/1" or "1|1".
	 */
	private String stringForGenotype(VariantContext variantContext, int i) {
		Genotype gt = variantContext.getGenotype(i);
		StringBuilder builder = new StringBuilder();
		for (Allele allele : gt.getAlleles()) {
			if (builder.length() > 0)
				builder.append(gt.isPhased() ? '|' : '/');
			builder.append(variantContext.getAlleleIndex(allele));
		}
		return builder.toString();
	}
}
