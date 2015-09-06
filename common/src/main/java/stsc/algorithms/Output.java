package stsc.algorithms;

import java.util.List;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SignalContainer;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.IntegerSignal;
import stsc.signals.SideSignal;
import stsc.signals.series.CommonSignalsSerie;

/**
 * Output algorithm is an adaptor algorithm that could be used for graphical
 * representation of another algorithms result. <br/>
 * Could adapt: <br/>
 * 1. {@link DoubleSignal} (to {@link DoubleSignal}); <br/>
 * 2. {@link SideSignal} (to {@link DoubleSignal}
 */
public final class Output extends StockAlgorithm {

	private final String fromExecution;

	public Output(final StockAlgorithmInit initialize) throws BadAlgorithmException {
		super(initialize);
		final List<String> subExecutions = initialize.getSettings().getSubExecutions();
		if (subExecutions.size() < 1)
			throw new BadAlgorithmException(Output.class.getSimpleName() + " algorithm should have at least one sub-execution");
		this.fromExecution = subExecutions.get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final StockAlgorithmInit init) throws BadAlgorithmException {
		return Optional.of(new CommonSignalsSerie<SerieSignal>(DoubleSignal.class));
	}

	@Override
	public void process(final Day day) throws BadSignalException {
		final SignalContainer<? extends SerieSignal> signalHandler = getSignal(fromExecution, day.getDate());
		if (signalHandler == null)
			return;
		final Optional<? extends SerieSignal> signal = signalHandler.getValue();
		if (!signal.isPresent())
			return;
		if (signal.get() instanceof DoubleSignal) {
			addSignal(day.getDate(), signal.get());
		} else if (signal.get() instanceof SideSignal) {
			final SideSignal sideSignal = (SideSignal) signal.get();
			final DoubleSignal result = new DoubleSignal(sideSignal.getValue() * sideSignal.getSide().value());
			addSignal(day.getDate(), result);
		} else if (signal.get() instanceof IntegerSignal) {
			addSignal(day.getDate(), new DoubleSignal(((IntegerSignal) signal.get()).getValue()));
		}
	}
}
