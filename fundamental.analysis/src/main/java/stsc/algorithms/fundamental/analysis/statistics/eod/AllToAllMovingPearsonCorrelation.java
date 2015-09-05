package stsc.algorithms.fundamental.analysis.statistics.eod;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import stsc.signals.MapKeyPairToDoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * {@link AllToAllMovingPearsonCorrelation} end of day algorithm that calculate
 * moving correlation coefficient for all possible pairs using Pearson model.<br/>
 */
public final class AllToAllMovingPearsonCorrelation extends EodAlgorithm implements MovingPearsonCorrelation {

	private final int correlationLength;

	private final Map<String, OnStockData> onStock = new HashMap<>();

	public AllToAllMovingPearsonCorrelation(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.correlationLength = init.getSettings().getIntegerSetting("N", 22).getValue().intValue();
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final EodAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(MapKeyPairToDoubleSignal.class, size));
	}

	@Override
	public void process(final Date date, final HashMap<String, Day> datafeed) throws BadSignalException {
		for (Entry<String, Day> e : datafeed.entrySet()) {
			addEntry(e);
		}
		final ArrayList<String> stockNames = new ArrayList<>(datafeed.keySet());
		final MapKeyPairToDoubleSignal.Builder builder = MapKeyPairToDoubleSignal.builder();
		for (int i = 0; i < stockNames.size(); ++i) {
			for (int u = i + 1; u < stockNames.size(); ++u) {
				final OnStockData left = onStock.get(stockNames.get(i));
				final OnStockData right = onStock.get(stockNames.get(u));
				final double correlationValue = calculateCorrelationFor(date, left, right);
				builder.addValue(stockNames.get(i), stockNames.get(u), correlationValue);
			}
		}
		addSignal(date, builder.build());
	}

	private double calculateCorrelationFor(final Date date, OnStockData left, OnStockData right) {
		if (left.size() > right.size()) {
			OnStockData swap = left;
			left = right;
			right = swap;
		}
		if (correlationLength / 2.0 > left.size() || correlationLength / 2.0 > right.size()) {
			return 0.0;
		}

		final Iterator<Double> leftIter = left.iterator();
		final Iterator<Double> rightIter = right.iterator();
		for (int i = 0; i < right.size() - left.size(); ++i) {
			rightIter.next();
		}
		double covxy = 0.0;
		double sumSigmaLeft = 0.0;
		double sumSigmaRight = 0.0;
		final double leftAverage = left.getAverageForLast(right.size() - left.size() + 1);
		final double rightAverage = right.getAverageForLast(right.size() - left.size() + 1);
		while (rightIter.hasNext()) {
			final double lvDiff = leftIter.next() - leftAverage;
			final double rvDiff = rightIter.next() - rightAverage;
			covxy += lvDiff * rvDiff;
			sumSigmaLeft += Math.pow(lvDiff, 2.0);
			sumSigmaRight += Math.pow(rvDiff, 2.0);
		}
		return covxy / Math.sqrt(sumSigmaLeft * sumSigmaRight);

	}

	private void addEntry(final Entry<String, Day> e) {
		OnStockData osd = onStock.get(e.getKey());
		if (osd == null) {
			osd = new OnStockData(this);
			onStock.put(e.getKey(), osd);
		}
		osd.processNewEntry(e);
	}

	// used by OnStockData
	@Override
	public double getValue(final Day day) {
		return day.getPrices().getOpen();
	}

	// used by OnStockData
	@Override
	public int getCorrelationLength() {
		return correlationLength;
	}
}
