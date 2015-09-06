package stsc.algorithms.primitive.eod;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import stsc.algorithms.EodPosition;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Side;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodAlgorithm;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.signals.SignalContainer;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.SerieSignal;
import stsc.signals.SideSignal;

/**
 * Algorithm require sub execution with {@link SideSignal} output. This
 * algorithm will open all signaled stocks and keep them open until signal
 * exists. <br/>
 */
public final class OpenWhileSignalAlgorithm extends EodAlgorithm {

	private final HashMap<String, EodPosition> shortPositions = new HashMap<>();
	private final HashMap<String, EodPosition> longPositions = new HashMap<>();

	private final double P;
	private final String sideSignalAlgoName;

	public OpenWhileSignalAlgorithm(EodAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		this.P = init.getSettings().getDoubleSetting("P", 10000.0).getValue();
		if (init.getSettings().getSubExecutions().isEmpty())
			throw new BadAlgorithmException("Open While Signal Algorithm should receive at least one sub-algorithm");
		this.sideSignalAlgoName = init.getSettings().getSubExecutions().get(0);
	}

	@Override
	public Optional<SignalsSerie<SerieSignal>> registerSignalsClass(EodAlgorithmInit init) throws BadAlgorithmException {
		return Optional.empty();
	}

	@Override
	public void process(Date date, HashMap<String, Day> datafeed) throws BadSignalException {

		for (Map.Entry<String, Day> i : datafeed.entrySet()) {
			final String stockName = i.getKey();
			final SignalContainer<? extends SerieSignal> isSignal = getSignal(stockName, sideSignalAlgoName, date);
			if (!isSignal.isPresent()) {
				if (shortPositions.containsKey(stockName)) {
					broker().sell(stockName, Side.SHORT, shortPositions.get(stockName).getSharedAmount());
					shortPositions.remove(stockName);
				} else if (longPositions.containsKey(stockName)) {
					broker().sell(stockName, Side.LONG, longPositions.get(stockName).getSharedAmount());
					longPositions.remove(stockName);
				}
			} else {
				final Optional<SideSignal> ss = isSignal.getSignal(SideSignal.class);
				if (!ss.isPresent()) {
					return;
				}
				final Side signalSide = ss.get().getSide();
				if (signalSide == Side.LONG && !longPositions.containsKey(stockName)) {
					final int sharesSize = getSharesSize(i.getValue().getPrices().getOpen());
					longPositions.put(stockName, new EodPosition(stockName, Side.LONG, sharesSize));
					broker().buy(stockName, Side.LONG, sharesSize);
				} else if (signalSide == Side.SHORT && !shortPositions.containsKey(stockName)) {
					final int sharesSize = getSharesSize(i.getValue().getPrices().getOpen());
					shortPositions.put(stockName, new EodPosition(stockName, Side.SHORT, sharesSize));
					broker().buy(stockName, Side.SHORT, sharesSize);
				}
			}
		}
	}

	private int getSharesSize(final Double price) {
		return (int) Math.round(P / price);
	}
}
