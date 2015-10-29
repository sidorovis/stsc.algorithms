package stsc.algorithms.indices.stock;

import java.util.LinkedList;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class Momentum extends StockAlgorithm {

	private int index = 0;

	private final int N;
	private LinkedList<Double> period = new LinkedList<>();

	private final String subAlgoName;

	public Momentum(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.N = init.getSettings().getIntegerSetting("N", 5);
		if (init.getSettings().getSubExecutions().size() < 1) {
			throw new BadAlgorithmException(Momentum.class + " on stock algorithm should have at least one sub execution");
		}
		this.subAlgoName = init.getSettings().getSubExecutions().get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final double pt = getSignal(subAlgoName, day.getDate()).getContent(DoubleSignal.class).getValue();
		period.addLast(pt);
		if (index == 0) {
			addSignal(day.getDate(), new DoubleSignal(0.0));
		} else if (index < N) {
			final double ptn = period.getFirst();
			addSignal(day.getDate(), new DoubleSignal(pt - ptn));
		} else {
			final double ptn = period.pollFirst();
			addSignal(day.getDate(), new DoubleSignal(pt - ptn));
		}
		index += 1;
	}

}
