package stsc.algorithms.primitive.eod;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Side;
import stsc.common.algorithms.AlgorithmSetting;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;

/**
 * {@link OneSideOpenAlgorithm} is {@link EodAlgorithm} that can be used to test
 * hadoop multi-pc calculations / user interface Grid / Genetic searchers and
 * etc.
 * <hr/>
 * Parameters: <br/>
 * 1. <b>side: string</b> (for example "long", if it is "long" than field side
 * will store {@link Side#LONG} otherwise it will store {@link Side#SHORT}.
 * <hr/>
 * Algorithm description:<br/>
 * Open (selected by input parameter side) position for all stocks into first
 * available (not empty) datafeed. All positions shares amount is 100.<br/>
 * After first open keep positions opened till end of testing period.
 * <hr/>
 */
public final class OneSideOpenAlgorithm extends EodAlgorithm {

	private final Side side;
	private boolean opened = false;

	public OneSideOpenAlgorithm(EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		final AlgorithmSetting<String> sideValue = init.getSettings().getStringSetting("side", "long");
		if (sideValue.getValue().compareTo("long") == 0) {
			this.side = Side.LONG;
		} else {
			this.side = Side.SHORT;
		}
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.empty();
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		if (opened)
			return;
		if (datafeed.isEmpty())
			return;
		for (Map.Entry<String, Day> i : datafeed.entrySet()) {
			broker().buy(i.getKey(), side, 100);
		}
		opened = true;
	}
}
