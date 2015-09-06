package stsc.algorithms;

import stsc.common.Side;

/**
 * {@link EodPosition} store information about end of day market position for
 * one tradeable element (stock). Store stockName (string), {@link Side} side of
 * the position long or short, shares amount (amount of bought shares,
 * positive).
 */
public final class EodPosition {

	private final String stockName;
	private final Side side;
	private int sharesAmount;

	public int getSharedAmount() {
		return sharesAmount;
	}

	public void setSharedAmount(int sharedAmount) {
		this.sharesAmount = sharedAmount;
	}

	public String getStockName() {
		return stockName;
	}

	public Side getSide() {
		return side;
	}

	public EodPosition(String stockName, Side side, int sharesAmount) {
		this.stockName = stockName;
		this.side = side;
		this.sharesAmount = sharesAmount;
	}

	@Override
	public String toString() {
		return stockName + "(" + side.name() + ":" + String.valueOf(sharesAmount) + ")";
	}
}
