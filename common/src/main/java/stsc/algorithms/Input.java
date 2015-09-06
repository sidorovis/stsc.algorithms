package stsc.algorithms;

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
 * Input on Stock Algorithm - returns series of data from {@link Day}. With 'e'
 * parameter, you can return OPEN, HIGH, LOW, CLOSE, VALUE from {@link Day}
 * class.
 */
public final class Input extends StockAlgorithm {

	enum DayField {
		OPEN, HIGH, LOW, CLOSE, VALUE
	};

	private static DayField fromString(String dayField) {
		if (dayField == null)
			return DayField.OPEN;
		switch (dayField) {
		case "open":
			return DayField.OPEN;
		case "high":
			return DayField.HIGH;
		case "low":
			return DayField.LOW;
		case "close":
			return DayField.CLOSE;
		case "value":
			return DayField.VALUE;
		default:
			return DayField.OPEN;
		}
	}

	DayField dayField;

	public Input(StockAlgorithmInit initialize) throws BadAlgorithmException {
		super(initialize);
		dayField = fromString(initialize.getSettings().getString("e"));
	}

	private DoubleSignal getData(final Day day) {
		switch (dayField) {
		case OPEN:
			return new DoubleSignal(day.getPrices().getOpen());
		case CLOSE:
			return new DoubleSignal(day.getPrices().getClose());
		case HIGH:
			return new DoubleSignal(day.getPrices().getHigh());
		case LOW:
			return new DoubleSignal(day.getPrices().getLow());
		case VALUE:
			return new DoubleSignal(day.getVolume());
		default:
			return new DoubleSignal(day.getPrices().getOpen());
		}
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(final StockAlgorithmInit init) throws BadAlgorithmException {
		final int size = init.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		addSignal(day.getDate(), getData(day));
	}

}
