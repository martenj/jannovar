package de.charite.compbio.jannovar.reference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.CDSInterval;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;

public class CDSIntervalTest {

	/** this test uses this static hg19 reference dictionary */
	static final ReferenceDictionary refDict = HG19RefDictBuilder.build();

	/** transcript builder for the forward strand */
	TranscriptModelBuilder builderForward;
	/** transcript builder for the reverse strand */
	TranscriptModelBuilder builderReverse;
	/** transcript info for the forward strand */
	TranscriptModel infoForward;
	/** transcript info for the reverse strand */
	TranscriptModel infoReverse;

	@Before
	public void setUp() {
		this.builderForward = TranscriptModelFactory.parseKnownGenesLine(refDict,
				"uc009vmz.1\tchr1\t+\t11539294\t11541938\t11539294\t11539294\t2\t"
						+ "11539294,11541314,\t11539429,11541938,\tuc009vmz.1");
		this.infoForward = builderForward.build();
		this.builderReverse = TranscriptModelFactory.parseKnownGenesLine(refDict,
				"uc009vjr.2\tchr1\t-\t893648\t894679\t894010\t894620\t2\t"
						+ "893648,894594,\t894461,894679,\tuc009vjr.2");
		this.infoReverse = builderForward.build();
	}

	@Test
	public void testConstructorDefaultPositionType() {
		CDSInterval interval = new CDSInterval(this.infoForward, 23, 45);
		Assert.assertEquals(interval.transcript, this.infoForward);
		Assert.assertEquals(interval.beginPos, 23);
		Assert.assertEquals(interval.endPos, 45);
		Assert.assertEquals(interval.positionType, PositionType.ONE_BASED);
		Assert.assertEquals(interval.length(), 23);
	}

	@Test
	public void testConstructorExplicitPositionType() {
		CDSInterval interval = new CDSInterval(this.infoForward, 23, 45, PositionType.ZERO_BASED);
		Assert.assertEquals(interval.transcript, this.infoForward);
		Assert.assertEquals(interval.beginPos, 23);
		Assert.assertEquals(interval.endPos, 45);
		Assert.assertEquals(interval.positionType, PositionType.ZERO_BASED);
		Assert.assertEquals(interval.length(), 22);
	}

	@Test
	public void testConstructorOneToZeroPositionType() {
		CDSInterval oneInterval = new CDSInterval(this.infoForward, 23, 45, PositionType.ONE_BASED);
		CDSInterval zeroInterval = new CDSInterval(oneInterval, PositionType.ZERO_BASED);

		Assert.assertEquals(zeroInterval.transcript, this.infoForward);
		Assert.assertEquals(zeroInterval.beginPos, 22);
		Assert.assertEquals(zeroInterval.endPos, 45);
		Assert.assertEquals(zeroInterval.positionType, PositionType.ZERO_BASED);
		Assert.assertEquals(zeroInterval.length(), 23);
	}

	@Test
	public void testConstructorZeroToOnePositionType() {
		CDSInterval zeroInterval = new CDSInterval(this.infoForward, 23, 45, PositionType.ZERO_BASED);
		CDSInterval oneInterval = new CDSInterval(zeroInterval, PositionType.ONE_BASED);

		Assert.assertEquals(oneInterval.transcript, this.infoForward);
		Assert.assertEquals(oneInterval.beginPos, 24);
		Assert.assertEquals(oneInterval.endPos, 45);
		Assert.assertEquals(oneInterval.positionType, PositionType.ONE_BASED);
		Assert.assertEquals(oneInterval.length(), 22);
	}
}
