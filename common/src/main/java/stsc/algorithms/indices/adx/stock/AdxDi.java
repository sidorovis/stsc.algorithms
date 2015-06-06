package stsc.algorithms.indices.adx.stock;

import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.ListOfDoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class AdxDi extends StockAlgorithm {

	private final String adxTrueRangeName;
	private final AdxTrueRange adxTrueRange;

	private final String adxDmName;
	private final AdxDm adxDm;

	public AdxDi(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.adxDmName = init.getExecutionName() + "_AdxDm";
		this.adxDm = new AdxDm(new StockAlgorithmInit(adxDmName, init, init.getSettings()));

		this.adxTrueRangeName = init.getExecutionName() + "_AdxTrueRange";
		this.adxTrueRange = new AdxTrueRange(new StockAlgorithmInit(adxTrueRangeName, init, init.getSettings()));
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return Optional.of(new LimitSignalsSerie<>(ListOfDoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		adxTrueRange.process(day);
		adxDm.process(day);
		final double trueRangeValue = getSignal(adxTrueRangeName, day.getDate()).getContent(DoubleSignal.class).getValue();
		if (Double.compare(trueRangeValue, 0.0) == 0) {
			addSignal(day.getDate(), new ListOfDoubleSignal().add(0.0).add(0.0));
		} else {
			final double dmMinus = getSignal(adxDmName, day.getDate()).getContent(ListOfDoubleSignal.class).getValues().get(0);
			final double dmPlus = getSignal(adxDmName, day.getDate()).getContent(ListOfDoubleSignal.class).getValues().get(1);
			addSignal(day.getDate(), new ListOfDoubleSignal().add(dmMinus / trueRangeValue).add(dmPlus / trueRangeValue));
		}
	}
}
