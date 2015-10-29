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
 * First test attempt to implement market trend based algorithm. <br/>
 * Has 4 borders: <br/>
 * 1. When we should open Long;<br/>
 * 2. When we should close Long;<br/>
 * 3. When we should open Short;<br/>
 * 4. When we should close Short.<br/>
 */
public final class MarketTrendEquity extends EodAlgorithm {

	private final int positionSharesSize;
	private final String subExecution;

	private final double openLongBorder;
	private final double openShortBorder;
	private final double closeLongBorder;
	private final double closeShortBorder;

	private final Map<String, Integer> longPositions = new HashMap<>();
	private final Map<String, Integer> shortPositions = new HashMap<>();

	public MarketTrendEquity(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		final List<String> subExecutions = init.getSettings().getSubExecutions();
		if (subExecutions.size() < 1) {
			throw new BadAlgorithmException(MarketTrendEquity.class.toString() + " require at least one sub execution parameter");
		}
		subExecution = subExecutions.get(0);
		positionSharesSize = init.getSettings().getIntegerSetting("PSS", 100);
		openLongBorder = init.getSettings().getDoubleSetting("OLB", 60.0);
		openShortBorder = init.getSettings().getDoubleSetting("OSB", -60.0);
		closeLongBorder = init.getSettings().getDoubleSetting("CLB", 40.0);
		closeShortBorder = init.getSettings().getDoubleSetting("CSB", -40.0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.empty();
	}

	@Override
	public void process(final Date date, final HashMap<String, Day> datafeed) throws BadSignalException {
		for (Entry<String, Day> e : datafeed.entrySet()) {
			final String stockName = e.getKey();
			final double v = getSignal(e.getKey(), subExecution, date).getContent(DoubleSignal.class).getValue();
			processStock(stockName, v);
		}
	}

	private void processStock(final String stockName, final double v) {
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
