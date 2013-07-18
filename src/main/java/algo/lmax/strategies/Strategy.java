package algo.lmax.strategies;

import com.lmax.api.orderbook.OrderBookEvent;

import algo.lmax.OrderBookEventProcessor;

public interface Strategy {
	
	/**
	 * called when an OrderBookEventProcessor notifies this
	 * strategy that an new OrderBookEvent is available
	 * @param orderBookEvent an OrderBookEvent object
	 */
	void notify(String instruname, OrderBookEvent orderBookEvent);

	/**
	 * called when an OrderBookEventProcessor notifies this
	 * strategy that an new OrderBookEvent is available
	 * @param orderBookEvent an OrderBookEvent object
	 */
	void notify(String instruname, long price);
	
	/**
	 * Registers for OrderBookEventProcessor notifications
	 * @param orderBookEventProcessor an OrderBookEventProcessor object
	 */
	void register(OrderBookEventProcessor orderBookEventProcessor);
	
}
