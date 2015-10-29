package stsc.algorithms.indices.bb.stock;

import java.util.Optional;

import stsc.algorithms.indices.primitive.stock.SmStDev;
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
import stsc.signals.ListOfDoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class BollingerBands extends StockAlgorithm {

	private final Integer N;
	private final Double K;
	private Integer size;

	private final String smaName;
	private final Sma sma;
	private final String smStDevName;
	private final SmStDev smStDev;

	public BollingerBands(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		N = init.getSettings().getIntegerSetting("N", 20);
		K = init.getSettings().getDoubleSetting("K", 2.0);
		this.smaName = "BB_Sma_" + init.getExecutionName();
		this.smStDevName = "BB_StDev_" + init.getExecutionName();

		this.sma = createSma(init);
		this.smStDev = createStDev(init);
	}

	private Sma createSma(StockAlgorithmInit init) throws BadAlgorithmException {
		final MutableAlgorithmConfiguration settings = init.createSubAlgorithmConfiguration();
		settings.setInteger("N", N);
		settings.setInteger("size", size);
		settings.getSubExecutions().addAll(init.getSettings().getSubExecutions());
		final StockAlgorithmInit smaInit = new StockAlgorithmInit(smaName, init, settings);
		return new Sma(smaInit);
	}

	private SmStDev createStDev(StockAlgorithmInit init) throws BadAlgorithmException {
		final MutableAlgorithmConfiguration settings = init.createSubAlgorithmConfiguration();
		settings.setInteger("N", N);
		settings.setInteger("size", size);
		settings.getSubExecutions().addAll(init.getSettings().getSubExecutions());
		settings.addSubExecutionName(smaName);
		final StockAlgorithmInit smStDevInit = new StockAlgorithmInit(smStDevName, init, settings);
		return new SmStDev(smStDevInit);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<SerieSignal>(ListOfDoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		sma.process(day);
		smStDev.process(day);
		final Double sma = getSignal(smaName, day.getDate()).getContent(DoubleSignal.class).getValue();
		final Double stDev = getSignal(smStDevName, day.getDate()).getContent(DoubleSignal.class).getValue();
		final ListOfDoubleSignal signal = new ListOfDoubleSignal();
		signal.addDouble(sma - stDev * K);
		signal.addDouble(sma + stDev * K);
		addSignal(day.getDate(), signal);
	}

}