package stsc.algorithms.indices.stock;

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

/**
 * See http://www.metastock.com/Customer/Resources/TAAZ/Default.aspx?p=115 <br/>
 * This algorithm returns {@link DoubleSignal} limit serie of signals with pre-selected size. <br/>
 * TypicalPrice = ( High + Low + Close ) / 3. <br/>
 * Parameters: <br/>
 * a. size = integer (2 by default) - configure size of output limit serie.
 */
public final class TypicalPrice extends StockAlgorithm {

	public TypicalPrice(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final double typicalPrice = (day.getPrices().getHigh() + day.getPrices().getLow() + day.getPrices().getClose()) / 3;
		addSignal(day.getDate(), new DoubleSignal(typicalPrice));
	}

}
