package stsc.algorithms.indices.adx.stock;

import java.util.Optional;

import stsc.algorithms.indices.primitive.stock.Sma;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.MutableAlgorithmConfiguration;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class AdxAdx extends StockAlgorithm {

	private final String adxDxiName;
	private final AdxDxi adxDxi;

	private final String adxDxiSmaName;
	private final Sma adxDxiSma;

	public AdxAdx(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		final Integer N = init.getSettings().getIntegerSetting("N", 14);
		final Integer nSma = init.getSettings().getIntegerSetting("nSma", 14);

		this.adxDxiName = init.getExecutionName() + "_adxDxi";
		this.adxDxi = createAdxDxi(N, init);

		this.adxDxiSmaName = init.getExecutionName() + "_adxDxiSma";
		this.adxDxiSma = createSma(nSma, init);
	}

	private AdxDxi createAdxDxi(Integer N, StockAlgorithmInit init) throws BadAlgorithmException {
		final MutableAlgorithmConfiguration settings = init.createSubAlgorithmConfiguration();
		settings.setInteger("N", N);
		settings.setInteger("size", init.getSettings().getIntegerSetting("size", 2));
		final StockAlgorithmInit adxDxiInit = new StockAlgorithmInit(adxDxiName, init, settings);
		return new AdxDxi(adxDxiInit);
	}

	private Sma createSma(Integer nSma, StockAlgorithmInit init) throws BadAlgorithmException {
		final MutableAlgorithmConfiguration settings = init.createSubAlgorithmConfiguration();
		settings.setInteger("N", nSma);
		settings.setInteger("size", init.getSettings().getIntegerSetting("size", 2));
		settings.addSubExecutionName(adxDxiName);
		final StockAlgorithmInit smaInit = new StockAlgorithmInit(adxDxiSmaName, init, settings);
		return new Sma(smaInit);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		adxDxi.process(day);
		adxDxiSma.process(day);
		addSignal(day.getDate(), getSignal(adxDxiSmaName, day.getDate()).getContent(DoubleSignal.class));
	}

}
