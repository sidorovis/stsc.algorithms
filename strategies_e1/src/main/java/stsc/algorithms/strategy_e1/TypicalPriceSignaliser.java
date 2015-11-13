package stsc.algorithms.strategy_e1;

import java.util.Optional;

import stsc.algorithms.indices.stock.TypicalPrice;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Side;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.SideSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * This algorithm search for the moment when difference between low and typical price bigger then <LPS> parameter and returns {@link Side#LONG}. And for
 * <HPS> high price and typical price difference for {@link Side#SHORT} signal. <br/>
 * Parameters: <br/>
 * 1. HPS - double value - shift between high and typical price; <br/>
 * 2. LPS - double value - shift between low and typical price; <br/>
 */
public final class TypicalPriceSignaliser extends StockAlgorithm {

	final double highPriceShift;
	final double lowPriceShift;

	final String typicalPriceName;
	final TypicalPrice typicalPrice;

	public TypicalPriceSignaliser(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.highPriceShift = init.getSettings().getDoubleSetting("HPS", 10.0);
		this.lowPriceShift = init.getSettings().getDoubleSetting("LPS", 10.0);
		this.typicalPriceName = init.getExecutionName() + "_TP";
		final StockAlgorithmInit typicalPriceInit = init.createInit(typicalPriceName);
		this.typicalPrice = new TypicalPrice(typicalPriceInit);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		return Optional.of(new LimitSignalsSerie<SerieSignal>(SideSignal.class));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		this.typicalPrice.process(day);
		final double tp = getSignal(typicalPriceName, day.getDate()).getContent(DoubleSignal.class).getValue();
		if (day.getPrices().getHigh() - tp >= highPriceShift) {
			final double signalValue = day.getPrices().getHigh() - tp;
			addSignal(day.getDate(), new SideSignal(Side.LONG, signalValue));
		} else if (tp - day.getPrices().getLow() >= lowPriceShift) {
			final double signalValue = day.getPrices().getLow() - tp;
			addSignal(day.getDate(), new SideSignal(Side.SHORT, -signalValue));
		}
	}
}
