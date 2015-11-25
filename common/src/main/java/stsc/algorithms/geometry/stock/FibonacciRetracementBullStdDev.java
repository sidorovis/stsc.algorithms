package stsc.algorithms.geometry.stock;

import java.util.List;
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
 * Coefficient that describes correlation between input sub-execution serie and fibonacci bull (up-side) retracement. <br/>
 * https://en.wikipedia.org/wiki/Fibonacci_retracement
 * 
 * @input_parameters N - integer (4 by default) <br/>
 *                   size - integer (6) size of output serie <br/>
 *                   sub-execution algorithm;
 * @output_type {@link DoubleSignal} class.
 * @output_description standard deviation for sub-execution serie from expected fibonacci retracement (bull direction).
 */
public final class FibonacciRetracementBullStdDev extends StockAlgorithm {

	public final static double ratios[] = { 0.0, 0.236068, 0.381966, 0.618034 };

	private final int N;
	private final String subAlgoName;

	public FibonacciRetracementBullStdDev(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		List<String> subExecutionNames = init.getSettings().getSubExecutions();
		N = init.getSettings().getIntegerSetting("N", 4);
		if (subExecutionNames.size() < 1)
			throw new BadAlgorithmException(FibonacciRetracementBullStdDev.class.toString() + " require one sub parameter");
		subAlgoName = subExecutionNames.get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 5);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final Optional<DoubleSignal> lastSignal = getSignal(subAlgoName, day.getDate()).getSignal(DoubleSignal.class);
		if (!lastSignal.isPresent()) {
			return;
		}
		final int currentIndex = getIndexForCurrentStock();
		if (currentIndex < N) {
			addSignal(day.getDate(), new DoubleSignal(Double.MAX_VALUE));
			return;
		}
		final Optional<DoubleSignal> firstSignal = getSignal(subAlgoName, currentIndex - N).getSignal(DoubleSignal.class);
		final double lastValue = lastSignal.get().getValue();
		final double firstValue = firstSignal.get().getValue();
		if (lastValue < firstValue) {
			addSignal(day.getDate(), new DoubleSignal(Double.MAX_VALUE));
			return;
		}
		final double difference = lastValue - firstValue;
		double stdDev = 0.0;
		for (int i = 1; i < ratios.length; ++i) {
			final int mappedIndex = i * (N / ratios.length);
			final int index = currentIndex - N + mappedIndex;
			final double expectedValue = firstValue + ratios[i] * difference;
			final double actualValue = getSignal(subAlgoName, index).getSignal(DoubleSignal.class).get().getValue();
			stdDev += Math.pow(actualValue - expectedValue, 2.0);
		}
		addSignal(day.getDate(), new DoubleSignal(stdDev));
	}
}
