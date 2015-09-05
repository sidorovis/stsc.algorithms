package stsc.algorithms.fundamental.analysis.statistics.eod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import stsc.common.Day;

/**
 * {@link OnStockData} stores last
 * {@link AllToAllMovingPearsonCorrelation#correlationLength} elements for one
 * of the stocks. And provide interface to calculate average value for 'n' last
 * elements.
 */
final class OnStockData {

	private MovingPearsonCorrelation movingPearsonCorrelation;
	private LinkedList<Double> elements = new LinkedList<>();

	public OnStockData(final MovingPearsonCorrelation movingPearsonCorrelation) {
		this.movingPearsonCorrelation = movingPearsonCorrelation;
	}

	public void processNewEntry(Entry<String, Day> e) {
		final double v = movingPearsonCorrelation.getValue(e.getValue());
		elements.add(v);
		if (elements.size() > movingPearsonCorrelation.getCorrelationLength()) {
			elements.poll().doubleValue();
		}
	}

	public double getAverageForLast(int n) {
		final Iterator<Double> eIter = elements.iterator();
		if (n > elements.size()) {
			return 0.0;
		}
		for (int i = 0; i < elements.size() - n; ++i) {
			eIter.next();
		}
		double sum = 0.0;
		while (eIter.hasNext()) {
			sum += eIter.next();
		}
		return sum / n;
	}

	public int size() {
		return elements.size();
	}

	public Iterator<Double> iterator() {
		return elements.iterator();
	}

}