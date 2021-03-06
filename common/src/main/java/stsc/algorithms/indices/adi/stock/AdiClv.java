package stsc.algorithms.indices.adi.stock;

import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.common.stocks.Prices;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * AdiClv - on stock algortithm https://en.wikipedia.org/wiki/Accumulation/distribution_index
 * 
 * @input_parameters size - integer (2) size of output serie <br/>
 * @output_type {@link DoubleSignal} class.
 * @output_description if high == low then returns 0.0 else: <br/>
 *                     clv = ( close - low - ( high - close ) ) / ( high - low ).
 */
public final class AdiClv extends StockAlgorithm {

	public AdiClv(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		final Prices p = day.getPrices();
		final double denominator = p.getHigh() - p.getLow();
		if (Double.compare(denominator, 0.0) == 0) {
			addSignal(day.getDate(), new DoubleSignal(0.0));
		} else {
			final double clv = ((p.getClose() - p.getLow() - (p.getHigh() - p.getClose()))) / (denominator);
			addSignal(day.getDate(), new DoubleSignal(clv));
		}
	}
}
