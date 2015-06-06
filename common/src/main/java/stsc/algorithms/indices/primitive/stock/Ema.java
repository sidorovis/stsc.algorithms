package stsc.algorithms.indices.primitive.stock;

import java.util.List;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.AlgorithmSetting;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SignalContainer;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

//@formatter:off
/**
 * Ema on-stock algorithm calculate EMA function over necessary input require:
 * 1) setting for P: EMA[x+1] = PRICE_VALUE * P + EMA[x] * (1 - P); (0.2 by default);
 * 2) size setting that limit result serie; (2 by default);
 * 3) sub on-stock algorithm name (for example In(e=open)).
 * 
 * Translating P to N (days): N = (2 / P) - 1
 * P = 2 / ( N + 1 )
 */
//@formatter:on
public class Ema extends StockAlgorithm {

	private final String subAlgoName;
	private final AlgorithmSetting<Double> P;

	public Ema(final StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		P = init.getSettings().getDoubleSetting("P", 0.2);
		List<String> subExecutionNames = init.getSettings().getSubExecutions();
		if (subExecutionNames.size() < 1)
			throw new BadAlgorithmException("sub executions parameters not enought");
		subAlgoName = subExecutionNames.get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<SerieSignal>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final int signalIndex = getIndexForCurrentStock();
		final double price = getSignal(subAlgoName, day.getDate()).getContent(DoubleSignal.class).getValue();
		if (signalIndex == 0) {
			addSignal(day.getDate(), new DoubleSignal(price));
		} else {
			final SignalContainer<? extends SerieSignal> previousEmaSignal = getSignal(signalIndex - 1);
			if (previousEmaSignal != null) {
				final double previousEmaValue = previousEmaSignal.getContent(DoubleSignal.class).getValue();
				final double value = P.getValue() * price + (1.0 - P.getValue()) * previousEmaValue;
				addSignal(day.getDate(), new DoubleSignal(value));
			}
		}
	}
}
