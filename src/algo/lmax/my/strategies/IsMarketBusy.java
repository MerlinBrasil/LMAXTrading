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
	 * Contains the difference between the last two traded prices
	 * for the last twenty one price updates
	 */
	MaxList<Long> along = new MaxList<Long>(10);
	
	/**
	 * Contains the difference between the last two traded prices
	 * for the last four price updates
	 */
	MaxList<Long> ashort = new MaxList<Long>(3);

	Long lastprice = null;
	
	
	/**
	 * Gives a list a maximum size provides a MAX_SIZE-aware add
	 * method
	 */
	private class MaxList <T> {
		
		MaxList(int size) {
			MAX_SIZE = size;
		}
		int MAX_SIZE;
		private int index = 0;		
		
		List<T> list = new ArrayList<T>();
		
		public void add(T o) {
			
			// check if adding a new element to the list
			// will exceed the maximum size of the list
			// and remove the first element of the list
			// if it is the case
			if((++index) > MAX_SIZE) {
				list.remove(0);
				--index;
			}
			list.add(o);
		}

		public Iterator<T> iterator() {
			return list.iterator();
		}
		
		
	}
	
	
	
	
	@Override
	public void register(OrderBookEventProcessor orderBookEventProcessor) {
		orderBookEventProcessor.add(this);
	}
	
	@Override
	public void notify(OrderBookEvent orderBookEvent) {
		

		for (int i = 0; i < 100; i++) {
			
			processEvent(orderBookEvent);
			
		}

	}

	private void processEvent(OrderBookEvent ob) {
		
		
		Long randnum = 1000 + Math.round(Math.random()*25);
				
		// TODO check if big diff between LastTradedPrice and bid/ask prices
		long var;
//		Long newp = ob.getLastTradedPrice().longValue();
		long newp = randnum;
		
		if (lastprice == null) {
			lastprice = newp;
			return;
		}
		
		var = Math.abs(newp - lastprice);
		
//		System.out.println("new price is " + newp + " / var is " + var);
		
		lastprice = newp;
		
		
		// TODO watch out autoboxing
		long t1 = System.nanoTime();
		along.add(var);
		System.out.println((System.nanoTime()-t1));
		ashort.add(var);
		
		if (along.index == along.MAX_SIZE ) {

			double longAvg = calcAverage(along);
			double shortAvg = calcAverage(ashort);
//			System.out.println("short average: " + shortAvg + ", long average: " + longAvg);
			
//			for (Iterator iterator = ashort.iterator(); iterator.hasNext();) {
//				Long price = (Long) iterator.next();
//				System.out.println(price);
//				
//			}
			
//			System.out.printf("shortAvg = %.2f, longAvg = %.2f",shortAvg,longAvg);
//			System.out.println();
			if(isDifferenceMeaningful(longAvg,shortAvg)) {
				System.out.printf("shortAvg = %.2f, longAvg = %.2f >> short average is %.2f times greater\n",shortAvg,longAvg,shortAvg/longAvg);
//				System.out.println();				
//				System.out.println("Last traded price is - " + ob.getLastTradedPrice());
				return;
			}
			
		}
		
	}

	private boolean isDifferenceMeaningful(double longAvg, double shortAvg) {
		
		if(shortAvg > 1.6 * longAvg)
			return true;
		return false;
	}

	private double calcAverage(MaxList<Long> list) {
		Long sum = 0L;
		for (Iterator<Long> iterator = list.iterator(); iterator.hasNext();) {
			Long e = iterator.next();
			sum += e;
		}
//		System.out.println(sum + "   " + list.MAX_SIZE);
		return (double) sum/ (double) list.MAX_SIZE;
		
	}	
	public static void main(String[] args) {
		
		Strategy strat = new IsMarketBusy();
		OrderBookEvent ob = null;
		strat.notify(ob);
	}
}