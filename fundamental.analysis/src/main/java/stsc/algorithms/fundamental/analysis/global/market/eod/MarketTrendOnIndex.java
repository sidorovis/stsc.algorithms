package stsc.algorithms.fundamental.analysis.global.market.eod;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import stsc.algorithms.AlgorithmConfigurationImpl;
import stsc.algorithms.EodToStockAdapter;
import stsc.algorithms.geometry.stock.LeastSquaresStraightStdDev;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class MarketTrendOnIndex extends EodAlgorithm implements EodToStockAdapter.SignalGenerator<DoubleSignal> {

	private final String stockName;
	private final int N;
	private final double level;

	private final String lssvName;

	private final EodToStockAdapter<DoubleSignal> adapter;
	private final LeastSquaresStraightStdDev leastSquaresStraightStdDev;

	public MarketTrendOnIndex(EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		stockName = init.getSettings().getStringSetting("SN", "spy").getValue();
		this.N = init.getSettings().getIntegerSetting("N", 22).getValue();
		this.level = init.getSettings().getDoubleSetting("level", 40.0).getValue();
		this.lssvName = init.getExecutionName() + "_Lssv";

		final String adapterName = init.getExecutionName() + "_AdapterToLssv";

		final AlgorithmConfigurationImpl adapterSettings = new AlgorithmConfigurationImpl();
		final StockAlgorithmInit adapterInit = init.createInit(adapterName, adapterSettings, stockName);
		this.adapter = new EodToStockAdapter<DoubleSignal>(adapterInit, this);
		final AlgorithmConfigurationImpl lssvSettings = new AlgorithmConfigurationImpl();
		lssvSettings.setInteger("N", N);
		lssvSettings.getSubExecutions().add(adapterName);
		final StockAlgorithmInit lssvInit = init.createInit(lssvName, lssvSettings, stockName);
		leastSquaresStraightStdDev = new LeastSquaresStraightStdDev(lssvInit);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		final Day day = datafeed.get(stockName);
		if (day != null) {
			adapter.process(day);
			leastSquaresStraightStdDev.process(day);
			final double value = getSignal(stockName, lssvName, date).getContent(DoubleSignal.class).getValue();
			if (value < level) {
				addSignal(date, new DoubleSignal(value));
			} else {
				addSignal(date, new DoubleSignal(level));
			}
		} else {
			addSignal(date, new DoubleSignal(level));
		}
	}

	// from EodToStockAdapter.SignalGenerator<DoubleSignal>
	@Override
	public DoubleSignal generateSignal(Day day) {
		return new DoubleSignal(day.getPrices().getOpen());
	}

}
