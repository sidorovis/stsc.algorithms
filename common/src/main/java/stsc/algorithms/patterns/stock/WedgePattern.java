package stsc.algorithms.patterns.stock;

import java.util.List;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalsSerie;
import stsc.signals.ListOfDoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class WedgePattern extends GeometryTriangleStockAlgorithmBase {

	private final Double acceptableShortTrendCoefficient;
	private final Double acceptableLongTrendCoefficient;

	public WedgePattern(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);

		this.acceptableShortTrendCoefficient = init.getSettings().getDoubleSetting("STC", -0.05);
		this.acceptableLongTrendCoefficient = init.getSettings().getDoubleSetting("LTC", 0.05);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(ListOfDoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		super.process(day);

		final List<Double> values = processHelper(day);
		if (values.isEmpty()) {
			return;
		}
		final double a1 = values.get(0);
		final double a2 = values.get(1);
		final double x = values.get(4);
		final double y = values.get(5);

		if (a1 < 0 && a2 < 0) {
			if (a2 < acceptableShortTrendCoefficient) {
				addSignal(day.getDate(), new ListOfDoubleSignal().add(-1.0).add(x).add(y));
			}
		} else if (a1 > 0 && a2 > 0) {
			if (a1 > acceptableLongTrendCoefficient) {
				addSignal(day.getDate(), new ListOfDoubleSignal().add(1.0).add(x).add(y));
			}
		}
	}

}
