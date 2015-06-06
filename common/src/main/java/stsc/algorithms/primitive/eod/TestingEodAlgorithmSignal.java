package stsc.algorithms.primitive.eod;

import stsc.common.signals.SerieSignal;


public class TestingEodAlgorithmSignal extends SerieSignal {
	public final String dateRepresentation;

	public TestingEodAlgorithmSignal(String dateRepresentation) {
		this.dateRepresentation = dateRepresentation;
	}
}