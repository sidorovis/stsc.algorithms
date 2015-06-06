package stsc.algorithms.helpers.stock;

import java.util.List;
import java.util.Optional;

import stsc.algorithms.geometry.stock.FibonacciRetracementBearStdDev;
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

/**
 * This algorithm is helper for Visualizing some of algorithms results. For
 * example {@link FibonacciRetracementBearStdDev} algorithms can provide signal
 * {@link Double#MAX_VALUE} which is really much bigger then expected to
 * visualize. <br/>
 * So you can use this algorithm to set up maximum (up and bottom borders). <br/>
 * For example: you can use it with next parameters:<br/>
 * a1. UP = 100d, DOWN = -100d;<br/>
 * a2. ALL = 100d (will setup 'up to 100d' and 'down to -100d').<br/>
 * You can use UP, DOWN and ALL together but result will be unpredictable. By
 * default UP = {@link Double#MAX_VALUE}, DOWN = -{@link Double#MAX_VALUE}, ALL
 * = {@link Double#MAX_VALUE}.
 */
public class AlgorithmsLimiter extends StockAlgorithm {

	private final String subAlgoName;
	private final double up;
	private final double down;
	private final double all;

	public AlgorithmsLimiter(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		List<String> subExecutionNames = init.getSettings().getSubExecutions();
		if (subExecutionNames.size() < 1)
			throw new BadAlgorithmException(AlgorithmsLimiter.class.toString() + " require one sub parameter");
		subAlgoName = subExecutionNames.get(0);
		up = init.getSettings().getDoubleSetting("UP", Double.MAX_VALUE).getValue();
		down = init.getSettings().getDoubleSetting("DOWN", -Double.MAX_VALUE).getValue();
		all = init.getSettings().getDoubleSetting("ALL", Double.MAX_VALUE).getValue();
		if (up <= down)
			throw new BadAlgorithmException("up parameter should be > then down parameter");
		if (all <= 0.0)
			throw new BadAlgorithmException("all parameter should be bigger then 0.0");
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final SignalContainer<?> serieSignal = getSignal(subAlgoName, day.getDate());
		if (!serieSignal.isPresent()) {
			return;
		}
		final Optional<DoubleSignal> lastSignal = serieSignal.getSignal(DoubleSignal.class);
		if (lastSignal.isPresent()) {
			final double value = lastSignal.get().getValue();
			final double limitedValue = calculateNewValue(value);
			addSignal(day.getDate(), new DoubleSignal(limitedValue));
		}
	}

	private double calculateNewValue(double value) {
		if (value > up)
			return up;
		if (value < down)
			return down;
		if (value > all)
			return all;
		if (value < -all) {
			return -all;
		}
		return value;
	}
}
