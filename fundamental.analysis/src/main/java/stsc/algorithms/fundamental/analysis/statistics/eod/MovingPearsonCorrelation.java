package stsc.algorithms.fundamental.analysis.statistics.eod;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * {@link MovingPearsonCorrelation} end of day algorithm that calculate moving
 * correlation coefficient for all possible pairs using Pearson model.</br> TODO
 * create map for exit
 */
public final class MovingPearsonCorrelation extends EodAlgorithm {

	private final class OnStockData {
		public LinkedList<Double> elements = new LinkedList<>();

		public OnStockData() {
		}

		public void processNewEntry(Entry<String, Day> e) {
			final double v = getValue(e.getValue());
			elements.add(v);
			if (elements.size() > correlationLength) {
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

	}

	private final int correlationLength;

	private final Map<String, OnStockData> onStock = new HashMap<>();

	public MovingPearsonCorrelation(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.correlationLength = init.getSettings().getIntegerSetting("N", 22).getValue().intValue();

	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		for (Entry<String, Day> e : datafeed.entrySet()) {
			addEntry(e);
		}
		final ArrayList<String> stockNames = new ArrayList<>(datafeed.keySet());
		for (int i = 0; i < stockNames.size(); ++i) {
			for (int u = i + 1; u < stockNames.size(); ++u) {
				final OnStockData left = onStock.get(stockNames.get(i));
				final OnStockData right = onStock.get(stockNames.get(u));
				final double correlationValue = calculateCorrelationFor(date, left, right);
				addSignal(date, new DoubleSignal(correlationValue));
				return;
			}
		}
	}

	private double calculateCorrelationFor(final Date date, OnStockData left, OnStockData right) {
		if (left.elements.size() > right.elements.size()) {
			OnStockData swap = left;
			left = right;
			right = swap;
		}
		if (correlationLength / 2.0 > left.elements.size() || correlationLength / 2.0 > right.elements.size()) {
			return 0.0;
		}

		final Iterator<Double> leftIter = left.elements.iterator();
		final Iterator<Double> rightIter = right.elements.iterator();
		for (int i = 0; i < right.elements.size() - left.elements.size(); ++i) {
			rightIter.next();
		}
		double covxy = 0.0;
		double sumSigmaLeft = 0.0;
		double sumSigmaRight = 0.0;
		final double leftAverage = left.getAverageForLast(right.elements.size() - left.elements.size() + 1);
		final double rightAverage = right.getAverageForLast(right.elements.size() - left.elements.size() + 1);
		while (rightIter.hasNext()) {
			final double lvDiff = leftIter.next() - leftAverage;
			final double rvDiff = rightIter.next() - rightAverage;
			covxy += lvDiff * rvDiff;
			sumSigmaLeft += Math.pow(lvDiff, 2.0);
			sumSigmaRight += Math.pow(rvDiff, 2.0);
		}
		return covxy / Math.sqrt(sumSigmaLeft * sumSigmaRight);

	}

	private void addEntry(Entry<String, Day> e) {
		OnStockData osd = onStock.get(e.getKey());
		if (osd == null) {
			osd = new OnStockData();
			onStock.put(e.getKey(), osd);
		}
		osd.processNewEntry(e);
	}

	private double getValue(final Day day) {
		return day.getPrices().getOpen();
	}

}
