package algo.lmax.my;

import algo.lmax.my.strategies.Strategy;

public interface OrderBookEventProcessor {

	void add(Strategy strategy);
		// add the strategy to the queue of strategies
}
