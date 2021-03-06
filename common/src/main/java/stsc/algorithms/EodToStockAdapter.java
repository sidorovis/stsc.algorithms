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
 * {@link EodToStockAdapter} algorithm is an adapter that provide possibility to 'reduce back' data from end of day algorithms to stock algorithm.<br/>
 * For example you have end of day algorithm (A) that calculate serie of double's based on some internal ideas and you want to use stock based algorithm (B)
 * over that serie. <br/>
 * You can adapt output of (A) to use it as input to (B).<br/>
 * This algorithm could be created only as sub-algorithm and require {@link SignalGenerator} implementation.
 * 
 * @input_parameters 'size' - integer value: size of output serie;
 * @output_type {@link DoubleSignal} class;
 * @output_description get signal from signal generator and returns it to serie.
 */
public final class EodToStockAdapter<S extends SerieSignal> extends StockAlgorithm {

	/**
	 * Internal algorithm that should be adapted.
	 * 
	 * @param <S>
	 *            SerieSignal type
	 */
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
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
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
