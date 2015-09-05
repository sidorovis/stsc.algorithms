package stsc.algorithms.fundamental.analysis.statistics.eod;

import stsc.common.Day;

/**
 * This interface could be to make sure that algorithm could be used by
 * {@link OnStockData} elements to get required data from it.
 */
interface MovingPearsonCorrelation {

	// used by OnStockData
	public abstract double getValue(Day day);

	// used by OnStockData
	public abstract int getCorrelationLength();

}