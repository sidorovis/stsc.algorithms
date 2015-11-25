package stsc.algorithms.geometry.stock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.MutableAlgorithmConfiguration;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SerieSignal;
import stsc.common.signals.SignalContainer;
import stsc.common.signals.SignalsSerie;
import stsc.signals.DoubleSignal;
import stsc.signals.ListOfDoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

/**
 * LeastSquaresStraightStdDev - on stock algorithm that calculate standard deviation distance of sub-execution serie from {@link LeastSquaresStraightValue}.
 * This algorithm returns standard deviation for differences between real and expected by least squares quadratic value (a0 + a1 * x) for last N values.
 * 
 * @input_parameters N - integer (5 by default); <br/>
 *                   size - integer (2) size of output serie; <br/>
 *                   sub-execution algorithm;
 * @output_type {@link DoubleSignal} class.
 * @output_description standard deviation difference for sub-execution serie from expected least squares straight value.
 */
public final class LeastSquaresStraightStdDev extends StockAlgorithm {

	private final int N;
	private final String subExecutionName;

	private final String lssName;
	private final LeastSquaresStraightValue lss;

	private double currentX = 0.0;
	private final LinkedList<Double> y = new LinkedList<>();

	public LeastSquaresStraightStdDev(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.N = init.getSettings().getIntegerSetting("N", 5);
		if (init.getSettings().getSubExecutions().size() < 1) {
			throw new BadAlgorithmException(LeastSquaresStraightStdDev.class + " algorithm require at least one sub algorithms.");
		}
		this.subExecutionName = init.getSettings().getSubExecutions().get(0);

		this.lssName = init.getExecutionName() + "_Lss";
		final MutableAlgorithmConfiguration settings = init.createSubAlgorithmConfiguration();
		settings.setInteger("N", N);
		settings.getSubExecutions().addAll(init.getSettings().getSubExecutions());
		StockAlgorithmInit lssInit = init.createInit(lssName, settings);
		this.lss = new LeastSquaresStraightValue(lssInit);
	}

	public String getLssName() {
		return lssName;
	}

	public double getCurrentX() {
		return currentX;
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		final int size = initialize.getSettings().getIntegerSetting("size", 2);
		return Optional.of(new LimitSignalsSerie<>(DoubleSignal.class, size));
	}

	@Override
	public void process(Day day) throws BadSignalException {
		lss.process(day);
		final SignalContainer<? extends SerieSignal> subSerieValue = getSignal(subExecutionName, day.getDate());
		final SignalContainer<? extends SerieSignal> lssValue = getSignal(lssName, day.getDate());
		if (subSerieValue == null || lssValue == null) {
			return;
		}
		y.addLast(subSerieValue.getContent(DoubleSignal.class).getValue());
		final ListOfDoubleSignal coefficients = lssValue.getContent(ListOfDoubleSignal.class);
		final double a0 = coefficients.getValues().get(0);
		final double a1 = coefficients.getValues().get(1);

		double sumStdDev = 0.0;
		final Iterator<Double> yIterator = y.iterator();
		double x = currentX - y.size() + 1;
		while (yIterator.hasNext()) {
			final double yValue = yIterator.next();
			sumStdDev += Math.pow(yValue - (a0 + a1 * x), 2.0);
			x += 1;
		}
		addSignal(day.getDate(), new DoubleSignal(sumStdDev));

		if (currentX >= N) {
			y.pollFirst();
		}

		currentX += 1;
	}

}
