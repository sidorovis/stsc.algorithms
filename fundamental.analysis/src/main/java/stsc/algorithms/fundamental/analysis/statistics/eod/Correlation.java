package stsc.algorithms.fundamental.analysis.statistics.eod;

import java.util.Date;
import java.util.HashMap;
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

public class Correlation extends EodAlgorithm {

	public Correlation(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		// TODO Auto-generated method stub

	}

}
