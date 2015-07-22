package stsc.algorithms;

import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public final class EodToStockAdapter<S extends SerieSignal> extends StockAlgorithm {

	public interface SignalGenerator<S extends SerieSignal> {
		public S generateSignal(Day day);
	}

	private final SignalGenerator<S> generator;

	public EodToStockAdapter(final StockAlgorithmInit init, final SignalGenerator<S> generator) throws BadAlgorithmException {
		super(init);
		this.generator = generator;
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		if (generator != null) {
			final S signal = generator.generateSignal(day);
			addSignal(day.getDate(), signal);
		}
	}

}
