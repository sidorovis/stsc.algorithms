package stsc.algorithms.equity.helper.stock.indices;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Side;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;

/**
 * {@link EodAlgorithm} that has 5(+1 sub-execution) parameters: <br/>
 * 1. {@link #positionSharesSize} - position shares size of position (amount of shares). Each signal algorithm will buy / sell this amount of shares. <br/>
 * 2. When sub-execution add {@link DoubleSignal} bigger then {@link #openLongBorder} (and we don't have any opened position for this stock), we send BUY
 * {@link Side#LONG} signal. <br/>
 * 3. When sub-execution add {@link DoubleSignal} smaller then {@link #openShortBorder} (and we don't have any opened position for this stock), we send BUY
 * {@link Side#SHORT} signal. <br/>
 * 4. When sub-execution add {@link DoubleSignal} smaller then {@link #closeLongBorder} (and we have opened {@link Side#LONG} position), we send SELL
 * {@link Side#LONG} signal.<br/>
 * 5. When sub-execution add {@link DoubleSignal} bigger then {@link #closeShortBorder} (and we have opened {@link Side#SHORT} position), we send SELL
 * {@link Side#SHORT} signal.
 */
public final class StockMarketCycleEquity extends EodAlgorithm {

	private final int positionSharesSize;
	private final String subExecution;

	private final double openLongBorder;
	private final double openShortBorder;
	private final double closeLongBorder;
	private final double closeShortBorder;

	private final Map<String, Integer> longPositions = new HashMap<>();
	private final Map<String, Integer> shortPositions = new HashMap<>();

	public StockMarketCycleEquity(EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		final List<String> subExecutions = init.getSettings().getSubExecutions();
		if (subExecutions.size() < 1) {
			throw new BadAlgorithmException(StockMarketCycleEquity.class.toString() + " require at least one sub execution parameter");
		}
		subExecution = subExecutions.get(0);
		positionSharesSize = init.getSettings().getIntegerSetting("PSS", 100);
		openLongBorder = init.getSettings().getDoubleSetting("OLB", 60.0);
		openShortBorder = init.getSettings().getDoubleSetting("OSB", -60.0);
		closeLongBorder = init.getSettings().getDoubleSetting("CLB", 40.0);
		closeShortBorder = init.getSettings().getDoubleSetting("CSB", -40.0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.empty();
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {
		for (Entry<String, Day> e : datafeed.entrySet()) {
			final String stockName = e.getKey();
			final double v = getSignal(e.getKey(), subExecution, date).getContent(DoubleSignal.class).getValue();
			processStock(stockName, v);
		}
	}

	private void processStock(String stockName, double v) {
		if (longPositions.containsKey(stockName)) {
			if (v < closeLongBorder) {
				broker().sell(stockName, Side.LONG, positionSharesSize);
				longPositions.remove(stockName);
			}
		} else if (shortPositions.containsKey(stockName)) {
			if (v > closeShortBorder) {
				broker().sell(stockName, Side.SHORT, positionSharesSize);
				shortPositions.remove(stockName);
			}
		} else {
			if (v > openLongBorder) {
				broker().buy(stockName, Side.LONG, positionSharesSize);
				longPositions.put(stockName, Integer.valueOf(positionSharesSize));
			} else if (v < openShortBorder) {
				broker().buy(stockName, Side.SHORT, positionSharesSize);
				shortPositions.put(stockName, Integer.valueOf(positionSharesSize));
			}
		}
	}
}
