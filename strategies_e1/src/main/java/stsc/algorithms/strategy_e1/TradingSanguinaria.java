package stsc.algorithms.strategy_e1;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Side;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.MapKeyPairToDoubleSignal;
import stsc.signals.commons.KeyPair;
import stsc.stocks.indexes.GlobalMarketIndex;
import stsc.stocks.indexes.RegionMarketIndex;

/**
 * https://en.wikipedia.org/wiki/Sanguinaria is a flower that rise very early in Spring, it like first flower. <br/>
 * This algorithm is first attempt to create full trading algorithm.<br/>
 * <br/>
 * 
 */
public final class TradingSanguinaria extends EodAlgorithm {

	private boolean inPosition = false;
	private int inPositionLength = 0;
	private final Map<String, Integer> openShorts = new HashMap<>();
	private final Map<String, Integer> openLongs = new HashMap<>();

	private final int openLength;
	private final String correlationName;
	private final String globalMarketIndex;
	private final Double positiveCorrelation;
	private final Double negativeCorrelation;
	private final int minimalStocksForSide;
	private final double moneyPerShare;

	public TradingSanguinaria(final EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		if (init.getSettings().getSubExecutions().size() < 1) {
			throw new BadAlgorithmException(TradingSanguinaria.class.getName() + " algorithm should one sub execution - for correlation definition");
		}
		this.openLength = init.getSettings().getIntegerSetting("N", 11);
		this.correlationName = init.getSettings().getSubExecutions().get(0);
		this.globalMarketIndex = init.getSettings().getStringSetting("GM", "spy");
		this.positiveCorrelation = init.getSettings().getDoubleSetting("PC", 0.92);
		this.negativeCorrelation = init.getSettings().getDoubleSetting("NC", -0.92);
		this.minimalStocksForSide = init.getSettings().getIntegerSetting("MS", 2);
		this.moneyPerShare = init.getSettings().getDoubleSetting("M", 1000.0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.empty();
	}

	@Override
	public void process(final Date date, final HashMap<String, Day> datafeed) throws BadSignalException {
		if (inPosition) {
			inPositionLength += 1;
			if (inPositionLength > openLength) {
				closePosition();
			}
			return;
		}
		final Map<KeyPair, Double> correlationCoefficients = getSignal(correlationName, date).getContent(MapKeyPairToDoubleSignal.class).getValues();
		final Set<String> positive = new HashSet<>();
		final Set<String> negative = new HashSet<>();
		for (Entry<KeyPair, Double> e : correlationCoefficients.entrySet()) {
			final KeyPair k = e.getKey();
			if (k.contain(globalMarketIndex)) {
				if (e.getValue() > positiveCorrelation) {
					final String stockName = getAnother(k);
					if (!isGlobal(stockName) && !isRegion(stockName))
						positive.add(stockName);
				}
				if (e.getValue() < negativeCorrelation) {
					final String stockName = getAnother(k);
					if (!isGlobal(stockName) && !isRegion(stockName))
						negative.add(stockName);
				}
			}
		}
		if (positive.size() > minimalStocksForSide && negative.size() > minimalStocksForSide) {
			openPosition(positive, negative, datafeed);
		}
	}

	private String getAnother(KeyPair k) {
		if (k.getLeft().equals(globalMarketIndex)) {
			return k.getRight();
		} else {
			return k.getLeft();
		}
	}

	private boolean isGlobal(String stockName) {
		return Collections.binarySearch(GlobalMarketIndex.getValues(), GlobalMarketIndex.createForSearch(stockName)) >= 0;
	}

	private boolean isRegion(String stockName) {
		return Collections.binarySearch(RegionMarketIndex.getValues(), RegionMarketIndex.createForSearch(stockName)) >= 0;
	}

	private void openPosition(final Set<String> positive, final Set<String> negative, HashMap<String, Day> datafeed) {
		for (String stockName : positive) {
			final double price = datafeed.get(stockName).prices.getClose();
			final int sharesAmount = (int) Math.floor(moneyPerShare / price);
			final int openedSize = broker().buy(stockName, Side.LONG, sharesAmount);
			openLongs.put(stockName, openedSize);
			if (openedSize > 0)
				inPosition = true;
		}
		for (String stockName : negative) {
			final double price = datafeed.get(stockName).prices.getClose();
			final int sharesAmount = (int) Math.floor(moneyPerShare / price);
			final int openedSize = broker().buy(stockName, Side.SHORT, sharesAmount);
			openShorts.put(stockName, openedSize);
			if (openedSize > 0)
				inPosition = true;
		}
	}

	private void closePosition() {
		inPosition = false;
		for (Entry<String, Integer> e : openLongs.entrySet()) {
			if (e.getValue() == 0) {
				continue;
			}
			final int closedSize = broker().sell(e.getKey(), Side.LONG, e.getValue());
			if (e.getValue() == closedSize) {
				e.setValue(0);
			} else {
				e.setValue(e.getValue() - closedSize);
				inPosition = true;
			}
		}
		for (Entry<String, Integer> e : openShorts.entrySet()) {
			if (e.getValue() == 0) {
				continue;
			}
			final int closedSize = broker().sell(e.getKey(), Side.SHORT, e.getValue());
			if (e.getValue() == closedSize) {
				e.setValue(0);
			} else {
				e.setValue(e.getValue() - closedSize);
				inPosition = true;
			}
		}

		if (!inPosition) {
			openShorts.clear();
			openLongs.clear();
			inPositionLength = 0;
		}
	}

}
