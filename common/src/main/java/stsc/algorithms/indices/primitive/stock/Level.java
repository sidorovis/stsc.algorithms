package stsc.algorithms.indices.primitive.stock;

import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalContainer;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public final class Level extends StockAlgorithm {

	final double level;
	final String factorName;

	public Level(StockAlgorithmInit initialize) throws BadAlgorithmException {
		super(initialize);
		level = Math.abs(initialize.getSettings().getDoubleSetting("f", 0.0));
		factorName = initialize.getSettings().getSubExecutions().get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		return Optional.of(new LimitSignalsSerie<SerieSignal>(DoubleSignal.class));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final SignalContainer<? extends SerieSignal> signal = getSignal(factorName, day.getDate());
		if (!signal.isPresent()) {
			return;
		}
		final DoubleSignal s = signal.getContent(DoubleSignal.class);
		if (s.getValue() > level || s.getValue() < -level) {
			addSignal(day.getDate(), new DoubleSignal(s.getValue()));
		}
	}
}
