package stsc.algorithms.fundamental.analysis.statistics.eod;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.MapKeyPairToDoubleSignal;
import stsc.signals.MapKeyPairToDoubleSignal.Builder;
import stsc.signals.series.LimitSignalsSerie;

/**
 * {@link LeftToRightMovingPearsonCorrelation} algorithm calculate Moving
 * Pearson Correlation between elements from left list and right list. Elements
 * at left list and at right list could be same (user of algorithm should make
 * sure that elements are not repeatable).
 */
public class LeftToRightMovingPearsonCorrelation extends EodAlgorithm implements MovingPearsonCorrelation {

	private final int correlationLength;
	private final ArrayList<String> leftElements = new ArrayList<>();
	private final ArrayList<String> rightElements = new ArrayList<>();

	private final Map<String, OnStockData> onStock = new HashMap<>();
	private final boolean allFromRightSide;

	public LeftToRightMovingPearsonCorrelation(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.correlationLength = init.getSettings().getIntegerSetting("N", 22).getValue().intValue();
		final String leftElementsString = init.getSettings().getStringSetting("LE", "").getValue();
		final String rightElementsString = init.getSettings().getStringSetting("RE", "").getValue();
		this.allFromRightSide = init.getSettings().getStringSetting("ALLR", "false").getValue().equals("true");
		for (String e : leftElementsString.split("\\|")) {
			if (!e.trim().isEmpty())
				leftElements.add(e.trim());
		}
		for (String e : rightElementsString.split("\\|")) {
			if (!e.trim().isEmpty())
				rightElements.add(e.trim());
		}
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
		final Set<String> datafeedKeys = datafeed.keySet();
		final MapKeyPairToDoubleSignal.Builder builder = MapKeyPairToDoubleSignal.builder();
		for (String l : leftElements) {
			if (!datafeedKeys.contains(l)) {
				continue;
			}
			if (allFromRightSide) {
				for (String r : datafeedKeys) {
					calculateCorrelation(l, r, date, builder);
				}
			} else {
				for (String r : rightElements) {
					if (!datafeedKeys.contains(r)) {
						continue;
					}
					calculateCorrelation(l, r, date, builder);
				}
			}
		}
		addSignal(date, builder.build());
	}

	private void calculateCorrelation(String l, String r, Date date, Builder builder) {
		final OnStockData left = onStock.get(l);
		final OnStockData right = onStock.get(r);
		final double correlationValue = calculateCorrelationFor(date, left, right);
		builder.addValue(l, r, correlationValue);
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
