package algo.lmax;

import algo.lmax.strategies.Strategy;

public interface OrderBookEventProcessor {

	void add(Strategy strategy);
		// add the strategy to the queue of strategies
}
