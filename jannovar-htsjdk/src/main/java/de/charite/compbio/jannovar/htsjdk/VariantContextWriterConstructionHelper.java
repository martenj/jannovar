package de.charite.compbio.jannovar.htsjdk;

import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.io.File;

import de.charite.compbio.jannovar.annotation.Annotation;

/**
 * Helper for creating a {@link VariantContextWriter} from a {@link VariantContextReader}.
 *
 * Part of the Jannovar-HTSJDK bridge.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public final class VariantContextWriterConstructionHelper {

	/**
	 * Return a new {@link ContextWriter} that uses the header from <code>reader</code> but has the header extended
	 * header through {@link extendHeaderFields}.
	 *
	 * @param reader
	 *            the reader to use for the construction
	 * @param fileName
	 *            path to output file
	 * @param fields
	 *            selection of header fields to write out
	 */
	public static VariantContextWriter openVariantContextWriter(VCFFileReader reader, String fileName, InfoFields fields) {
		// construct factory object for VariantContextWriter
		VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
		builder.setReferenceDictionary(reader.getFileHeader().getSequenceDictionary());
		builder.setOutputFile(new File(fileName));
		// Be more lenient in missing header fields.
		builder.setOption(Options.ALLOW_MISSING_FIELDS_IN_HEADER);
		// Disable on-the-fly generation of Tribble index if the input file does not have a sequence dictionary.
		if (reader.getFileHeader().getSequenceDictionary() == null)
			builder.unsetOption(Options.INDEX_ON_THE_FLY);

		// construct VariantContextWriter and write out header
		VariantContextWriter out = builder.build();
		out.writeHeader(extendHeaderFields(reader.getFileHeader(), fields));
		return out;
	}

	/**
	 * Extend a {@link VCFHeader} with the given <code>fields</code>.
	 *
	 * @param header
	 *            the {@link VCFHeader} to extend
	 * @param fields
	 *            the {@link InfoFields} to get the field selection from
	 * @return extended VCFHeader
	 */
	public static VCFHeader extendHeaderFields(VCFHeader header, InfoFields fields) {
		if (fields == InfoFields.EFFECT_HGVS || fields == InfoFields.BOTH) {
			// add INFO line for EFFECT field
			VCFInfoHeaderLine effectLine = new VCFInfoHeaderLine("EFFECT", 1, VCFHeaderLineType.String,
					Annotation.INFO_EFFECT);
			header.addMetaDataLine(effectLine);
			// add INFO line for HGVS field
			VCFInfoHeaderLine hgvsLine = new VCFInfoHeaderLine("HGVS", 1, VCFHeaderLineType.String,
					Annotation.INFO_HGVS);
			header.addMetaDataLine(hgvsLine);
		}
		if (fields == InfoFields.VCF_ANN || fields == InfoFields.BOTH) {
			// add INFO line for standardized ANN field
			VCFInfoHeaderLine annLine = new VCFInfoHeaderLine("ANN", 1, VCFHeaderLineType.String,
					Annotation.VCF_ANN_DESCRIPTION_STRING);
			header.addMetaDataLine(annLine);
		}
		return header;
	}

}
