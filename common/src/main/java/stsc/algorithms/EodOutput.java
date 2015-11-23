package stsc.algorithms;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SignalContainer;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.series.CommonSignalsSerie;

/**
 * End of day output algorithm. Pretty the same with {@link Output} but for end of day algorithm. <br/>
 * TODO consider possibility to unite with {@link Output} at one compilation unit. <br/>
 * 
 * @input_parameters sub execution algorithm.
 * @output_type {@link DoubleSignal} class.
 * @output_description get signal and add new signal value from sub-execution serie.
 */
public final class EodOutput extends EodAlgorithm {

	private final String fromExecution;

	public EodOutput(EodAlgorithmInit initialize) throws BadAlgorithmException {
		super(initialize);
		final List<String> subExecutions = initialize.getSettings().getSubExecutions();
		if (subExecutions.size() < 1)
			throw new BadAlgorithmException("out algorithm should have at least one sub-execution");
		this.fromExecution = subExecutions.get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.of(new CommonSignalsSerie<>(DoubleSignal.class));
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		final SignalContainer<? extends SerieSignal> signalHandler = getSignal(fromExecution, date);
		if (signalHandler == null)
			return;
		final Optional<? extends SerieSignal> signal = signalHandler.getValue();
		if (!signal.isPresent())
			return;
		if (signal.get() instanceof DoubleSignal) {
			addSignal(date, signal.get());
		}
	}
}
