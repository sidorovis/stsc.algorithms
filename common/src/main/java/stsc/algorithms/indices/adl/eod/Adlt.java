package stsc.algorithms.indices.adl.eod;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class Adlt extends EodAlgorithm {

	private final Map<String, Double> lastPrices = new HashMap<>();

	public Adlt(EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		double at = 0;
		double dt = 0;
		double ut = 0;
		for (Entry<String, Day> e : datafeed.entrySet()) {
			final Day d = e.getValue();
			final Double close = d.getPrices().getClose();
			Double v = lastPrices.get(e.getKey());
			if (v == null) {
				v = close;
			}
			lastPrices.put(e.getKey(), close);
			if (close > v) {
				at += 1;
			} else if (close < v) {
				dt += 1;
			} else {
				ut += 1;
			}
		}
		final int index = getCurrentIndex();
		if (index == 0) {
			addSignal(date, new DoubleSignal(0.0));
		} else {
			if (Double.compare(at + dt + ut, 0.0) == 0) {
				addSignal(date, new DoubleSignal(getSignal(index - 1).getContent(DoubleSignal.class).getValue()));
			} else {
				final double newAdditional = (at - dt) / (at + dt + ut);
				addSignal(date, new DoubleSignal(newAdditional + getSignal(index - 1).getContent(DoubleSignal.class).getValue()));
			}
		}
	}

}
