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

/**
 * {@link EodToStockAdapter} algorithm is an adapter that provide possibility to
 * 'reduce' back data from end of day algorithms to stock algorithm.<br/>
 * For example you have end of day algorithm (A) that calculate serie of
 * double's based on some internal ideas and you want to use stock based
 * algorithm (B) over that serie. You can adapt output of (A) to use it as input
 * to (B).
 */
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
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final StockAlgorithmInit initialize) throws BadAlgorithmException {
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
