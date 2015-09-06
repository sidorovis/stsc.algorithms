package stsc.algorithms.indices.primitive.stock;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.AlgorithmSetting;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public final class Sma extends StockAlgorithm {

	private final String subAlgoName;
	private final AlgorithmSetting<Integer> N;

	final LinkedList<Double> elements = new LinkedList<>();
	Double sum = Double.valueOf(0.0);

	public Sma(final StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		N = init.getSettings().getIntegerSetting("N", 5);
		List<String> subExecutionNames = init.getSettings().getSubExecutions();
		if (subExecutionNames.size() < 1)
			throw new BadAlgorithmException("Sma algorithm should receive at least one sub algorithm");
		subAlgoName = subExecutionNames.get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<SerieSignal>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final double price = getSignal(subAlgoName, day.getDate()).getContent(DoubleSignal.class).getValue();
		elements.push(price);
		sum += price;
		if (elements.size() <= N.getValue()) {
			addSignal(day.getDate(), new DoubleSignal(sum / elements.size()));
		} else if (elements.size() > N.getValue()) {
			Double lastElement = elements.pollLast();
			sum -= lastElement;
			addSignal(day.getDate(), new DoubleSignal(sum / N.getValue()));
		}
	}
}
