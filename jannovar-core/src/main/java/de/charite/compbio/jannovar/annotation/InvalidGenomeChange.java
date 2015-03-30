package de.charite.compbio.jannovar.annotation;

import de.charite.compbio.jannovar.reference.GenomeChange;

/**
 * Thrown when the the given {@link GenomeChange} does not fit the used annotation builder class.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class InvalidGenomeChange extends AnnotationException {

	private static final long serialVersionUID = -6983204936815945929L;

	public InvalidGenomeChange() {
	}

	public InvalidGenomeChange(String msg) {
		super(msg);
	}

}
