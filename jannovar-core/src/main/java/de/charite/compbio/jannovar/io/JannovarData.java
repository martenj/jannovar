package de.charite.compbio.jannovar.io;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.charite.compbio.jannovar.Immutable;
import de.charite.compbio.jannovar.impl.intervals.IntervalArray;
import de.charite.compbio.jannovar.reference.TranscriptIntervalEndExtractor;
import de.charite.compbio.jannovar.reference.TranscriptModel;

// NOTE(holtgrem): Part of the public interface of the Jannovar library.
// TODO(holtgrem): Add the interval trees here.
// TODO(holtgrem): Rename package "de.charite.compbio.jannovar.io" to "de.charite.compbio.jannovar.data"?

/**
 * This data type is used for serialization after downloading.
 *
 * Making this class immutable makes it a convenient serializeable read-only database.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@Immutable
public final class JannovarData implements Serializable {

	/** Serial version ID. */
	private static final long serialVersionUID = 2L;

	/** map from chromosome ID to {@link Chromosome} */
	public final ImmutableMap<Integer, Chromosome> chromosomes;

	/** information about reference lengths and identities */
	public final ReferenceDictionary refDict;

	/**
	 * Initialize the object with the given values.
	 *
	 * @param refDict
	 *            the {@link ReferenceDictionary} to use in this object
	 * @param transcriptInfos
	 *            the list of {@link TranscriptInfo} objects to use in this object
	 */
	public JannovarData(ReferenceDictionary refDict, ImmutableList<TranscriptModel> transcriptInfos) {
		this.refDict = refDict;
		this.chromosomes = makeChromsomes(refDict, transcriptInfos);
	}

	/**
	 * This function constructs a HashMap<Byte,Chromosome> map of Chromosome objects in which the {@link TranscriptInfo}
	 * objects are entered into an {@link IntervalArray} for the appropriate Chromosome.
	 *
	 * @param refDict
	 *            the {@link ReferenceDictionary} to use for the construction
	 * @param transcriptInfos
	 *            list of {@link TranscriptInfo} objects with the transcripts of all chromosomes
	 * @return a mapping from numeric chromsome ID to {@link Chromosome} object
	 */
	private static ImmutableMap<Integer, Chromosome> makeChromsomes(
			ReferenceDictionary refDict, ImmutableList<TranscriptModel> transcriptInfos) {
		ImmutableMap.Builder<Integer, Chromosome> builder = new ImmutableMap.Builder<Integer, Chromosome>();

		// First, factorize the TranscriptInfo objects by chromosome ID.

		// create hash map for this
		HashMap<Integer, ArrayList<TranscriptModel>> transcripts = new HashMap<Integer, ArrayList<TranscriptModel>>();
		for (Integer chrID : refDict.contigName.keySet())
			transcripts.put(chrID, new ArrayList<TranscriptModel>());
		// distribute TranscriptInfo lists
		for (TranscriptModel transcript : transcriptInfos)
			transcripts.get(transcript.getChr()).add(transcript);

		// Then, construct an interval tree for each chromosome and add the lists of intervals.
		for (Integer chrID : transcripts.keySet()) {
			IntervalArray<TranscriptModel> iTree = new IntervalArray<TranscriptModel>(transcripts.get(chrID),
					new TranscriptIntervalEndExtractor());
			builder.put(chrID, new Chromosome(refDict, chrID, iTree));
		}

		return builder.build();
	}

}
