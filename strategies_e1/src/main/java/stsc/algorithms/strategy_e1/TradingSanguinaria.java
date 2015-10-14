package stsc.algorithms.strategy_e1;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import stsc.algorithms.fundamental.analysis.statistics.eod.LeftToRightMovingPearsonCorrelation;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;

/**
 * https://en.wikipedia.org/wiki/Sanguinaria is a flower that rise very early in
 * Spring, it like first flower. <br/>
 * This algorithm is first attempt to create full trading algorithm.<br/>
 * <br/>
 * <b>Algorithm Description.</b><br/>
 * <u>General description</u><br/>
 * Algorithm use {@link LeftToRightMovingPearsonCorrelation} algorithm search
 * for stocks that has high correlation coefficient with well known indexes
 * (spy).
 * 
 */
public final class TradingSanguinaria extends EodAlgorithm {

	public TradingSanguinaria(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.empty();
	}

	@Override
	public void process(final Date date, final HashMap<String, Day> datafeed) throws BadSignalException {
		// TODO Auto-generated method stub

	}

}
