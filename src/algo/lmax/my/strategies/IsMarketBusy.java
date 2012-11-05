package algo.lmax.my.strategies;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

import algo.lmax.my.OrderBookEventProcessor;

import com.lmax.api.FixedPointNumber;
import com.lmax.api.orderbook.OrderBookEvent;

/**
 * Calculates the average price change of an
 * instrument over two periods of time and raises
 * an alert when the difference between those two
 * averages is greater than a set multiple.
 * 
 * @author julienmonnier
 *
 */
public class IsMarketBusy implements Strategy {

//	OrderBookEvent ob;
	
	/**
	 * Contains values representing the difference between
	 * the last two traded prices received from the exchange
	 * It is used to obtain the "longer" average of thos price
	 * differences
	 */
	MaxSum along = new MaxSum(10);
	
	/**
	 * Contains values representing the difference between
	 * the last two traded prices received from the exchange
	 * It is used to obtain the "shorter" average of thos price
	 * differences
	 */
	MaxSum ashort = new MaxSum(2);

	long lastprice = 0;
	int moveindex = 0;
	long var;
	long newp;
	String msg;
	double longAvg;
	
	
	/**
	 * Maintains a sum of long values of up to MAX_SIZE number 
	 * of values and removes the oldest value from the sum for
	 * each new value added to the sum after that.
	 * <p>Provides basic statics on the sum, including the
	 * sum's average
	 */
	private class MaxSum {
		
		MaxSum(int size) {
			MAX_SIZE = size;
			MAX_SIZE_MINUS_ONE = MAX_SIZE-1;
			values = new long[MAX_SIZE];
			// initialize array values to zero
			for (int i = 0; i < MAX_SIZE_MINUS_ONE; i++) {
				values[i]=0;
			}
		}
		
		final int MAX_SIZE;
		final int MAX_SIZE_MINUS_ONE;
		long[] values;
		boolean isready = false;
		
		private int index = 0;
		
		long sum = 0;
		
		public void add(long newprice) {

			if(index > MAX_SIZE_MINUS_ONE) {
					// reset index to add newprice to begining of
					// values array
					isready = true;
					index = 0;
			}
			
			sum += newprice;
			
			// substract oldest value in array from sum
			sum -= values[(index)];
			
			// add new price to list of values, replacing
			// oldest value
			values[index] = newprice;
			
			++index;
		}
		
		/**
		 * Calculates the average of <code>sum</code>
		 * @return the average of <code>sum</code>
		 */
		public double getAverage() {

			return (double) ((sum*100)/ MAX_SIZE)/100;
		}
	}
	
	@Override
	public void register(OrderBookEventProcessor orderBookEventProcessor) {
		orderBookEventProcessor.add(this);
	}
	
	@Override
	public void notify(OrderBookEvent orderBookEven){
		// not used in this strategy
	}
	
	
	@Override
	public void notify(long askprice) {
		
		processEvent(askprice);
	}

	private void processEvent(long askprice) {
	
		// TODO check if big diff between LastTradedPrice and bid/ask prices
		// TODO watch out autoboxing
		
		newp = askprice;
		var = Math.abs(newp - lastprice);
		lastprice = newp;
		
		longAvg = along.getAverage();
		
	
		System.out.printf("price is "+ newp +", change is " + var + " and ratio is %.2f\n", (double) var/longAvg);
		
		if (along.isready && (var > 5 * longAvg)) {
			++moveindex;
			if(moveindex>1) {
				System.out.printf(var +" was a significant price change with a ration of %.2f\n", (double) var/longAvg);
			}

		} else {moveindex = 0;};
		
		along.add(var);
		
	}
}
