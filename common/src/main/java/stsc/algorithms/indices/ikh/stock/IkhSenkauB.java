package stsc.algorithms.indices.ikh.stock;

import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.MutatingAlgorithmConfiguration;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class IkhSenkauB extends StockAlgorithm {

	private final String prototypeName;
	private final IkhPrototype prototype;

	public IkhSenkauB(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.prototypeName = init.getExecutionName() + "_IhkPrototype";
		final MutatingAlgorithmConfiguration settings = init.createSubAlgorithmConfiguration();
		settings.setInteger("TS", init.getSettings().getIntegerSetting("TL", 52));
		settings.setInteger("TM", init.getSettings().getIntegerSetting("TM", 26));
		this.prototype = new IkhPrototype(init.createInit(prototypeName, settings));
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		prototype.process(day);
		final DoubleSignal v = getSignal(prototypeName, day.getDate()).getContent(DoubleSignal.class);
		addSignal(day.getDate(), v);
	}
}
