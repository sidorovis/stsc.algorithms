package stsc.algorithms.indices.primitive.eod;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * Simple Moving Average algorithm. Works on serie of Double and output serie of Double. This is end of day algorithm to process EOD output sub-executions. TODO
 * think how to unite with {@link stsc.algorithms.indices.primitive.stock.Sma}.
 */
public final class Sma extends EodAlgorithm {

	private final String subAlgoName;
	private final Integer N;

	private final LinkedList<Double> elements = new LinkedList<>();
	private Double sum = Double.valueOf(0.0);

	public Sma(EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		N = init.getSettings().getIntegerSetting("N", 5);
		List<String> subExecutionNames = init.getSettings().getSubExecutions();
		if (subExecutionNames.size() < 1)
			throw new BadAlgorithmException(Sma.class.toString() + " algorithm should receive at least one sub algorithm");
		subAlgoName = subExecutionNames.get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		final double price = getSignal(subAlgoName, date).getContent(DoubleSignal.class).getValue();
		elements.push(price);
		sum += price;
		if (elements.size() <= N) {
			addSignal(date, new DoubleSignal(sum / elements.size()));
		} else {
			Double lastElement = elements.pollLast();
			sum -= lastElement;
			addSignal(date, new DoubleSignal(sum / N));
		}
	}

}
