package stsc.algorithms.strategy_e1;

import java.util.List;
import java.util.Optional;

import stsc.algorithms.indices.bb.stock.BollingerBands;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Side;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.common.stocks.Prices;
import stsc.signals.ListOfDoubleSignal;
import stsc.signals.SideSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * Returns {@link SideSignal} with {@link Side#LONG} when upper BollingerBands line is lower then {@link Prices#getHigh()} and {@link SideSignal} with
 * {@link Side#SHORT} when downer BollingerBanks line is higher then {@link Prices#getLow()}.
 */
public final class BollingerBandsSignaliser extends StockAlgorithm {

	private final String bollingerBandsSubAlgorithmName;
	private final BollingerBands bollingerBands;

	public BollingerBandsSignaliser(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.bollingerBandsSubAlgorithmName = init.getExecutionName() + "_BB";
		this.bollingerBands = createBollingerBands(bollingerBandsSubAlgorithmName, init);
	}

	private BollingerBands createBollingerBands(String subAlgorithmName, StockAlgorithmInit init) throws BadAlgorithmException {
		final StockAlgorithmInit subInit = init.createInit(subAlgorithmName);
		return new BollingerBands(subInit);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		return Optional.of(new LimitSignalsSerie<SerieSignal>(SideSignal.class));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		bollingerBands.process(day);
		final List<Double> bbValue = getSignal(bollingerBandsSubAlgorithmName, day.getDate()).getContent(ListOfDoubleSignal.class).getValues();
		if (bbValue.get(0) > day.getPrices().getLow()) {
			addSignal(day.getDate(), new SideSignal(Side.LONG, bbValue.get(0) - day.getPrices().getLow()));
		} else if (bbValue.get(1) > day.getPrices().getHigh()) {
			addSignal(day.getDate(), new SideSignal(Side.SHORT, bbValue.get(1) - day.getPrices().getHigh()));
		}
	}

}
