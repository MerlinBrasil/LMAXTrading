package algo.lmax.my.strategies;

import com.lmax.api.orderbook.OrderBookEvent;

import algo.lmax.my.OrderBookEventProcessor;

public interface Strategy {
	
	/**
	 * called when an OrderBookEventProcessor notifies this
	 * strategy that an new OrderBookEvent is available
	 * @param orderBookEvent an OrderBookEvent object
	 */
	void notify(OrderBookEvent orderBookEvent);
	
	/**
	 * Registers for OrderBookEventProcessor notifications
	 * @param orderBookEventProcessor an OrderBookEventProcessor object
	 */
	void register(OrderBookEventProcessor orderBookEventProcessor);
	
}
