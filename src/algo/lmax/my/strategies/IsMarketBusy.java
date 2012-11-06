package algo.lmax.my.strategies;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;

import algo.lmax.my.OrderBookEventProcessor;

import com.lmax.api.FixedPointNumber;
import com.lmax.api.orderbook.OrderBookEvent;

/**
 * Determines if the level of activity increase 
 * in the underlying instrument (for example EUR/USD) 
 * using a time-sensitive and price-sensitive indicator
 * 
 * @author julienmonnier
 *
 */
public class IsMarketBusy implements Strategy {

	
	
	/**
	 * Contains values representing the time difference between
	 * two successive orderbook event updates received from the exchange
	 */
	MaxSum timevars;
	

	long lastprice = 0;
	int moveindex = 0;
	long var;
	long newp;
	String msg;
	double longAvg;
	long prevtime = System.nanoTime();
	HashMap<String,Integer>  instrutimesize;
	HashMap<String,Integer>  instrupricechange;
	HashMap<String,Integer> instruspeedchange;
	int TIMESIZE;
	int PRICECHANGE;
	int SPEEDCHANGE;
	int pricechangeindex = 1;
	int speedincreaseindex = 0;
	int alertindex = 12;
	int displaypriceindex = 12;
	int moduloindex = TIMESIZE/2;
	int signvar;
	int lastsignvar = 0;
	int lastsignvarsave = 0;
	String instruname;
	long askprice;
	long timevar;
	
	
	
	public IsMarketBusy(String instruname){
		this.instruname = instruname;
		instrutimesize = new HashMap<String,Integer>();
		instrupricechange = new HashMap<String,Integer>();
		instruspeedchange = new HashMap<String,Integer>();
			
		
		
		loadParams();
		TIMESIZE = (int) instrutimesize.get(instruname);
		PRICECHANGE = (int) instrupricechange.get(instruname);
		SPEEDCHANGE = (int) instruspeedchange.get(instruname);
		
		timevars = new MaxSum(TIMESIZE*2);
	}

	

	private void loadParams() {
		// TODO
		// should be reading static
		// data and exit early if data already
		// loaded
		
		String[] instrus = {"EUR/USD","USD/JPY","CLZ2"};
		int[] times = {5,4,4};
		int[] pricechanges = {4,4,3};
		int[] speedchanges = {times[0]*2+2,times[1]*2+2,times[2]*2+2};
		
		for (int i = 0; i < instrus.length; i++) {
			instrutimesize.put(instrus[i], times[i]);
			instrupricechange.put(instrus[i], pricechanges[i]);
			instruspeedchange.put(instrus[i], speedchanges[i]);
		}
	}
	
	
	/**
	 * Maintains a set of sums of long values
	 * <p>Provides basic statics on the sums, including the
	 * sums' average
	 */
	private class MaxSum {

		final int MAX_SIZE;
		final int MAX_SIZE_MINUS_ONE;
		long[] values;
		int sizehalfarrays;
		
		// sums the values of the values array
		long sum = 0;
		
		// sums half of values array's elements
		// containing the newest received values
		long h1sum = 0;
		
		// sums half of values array's elements
		// containing the oldest received values
		long h2sum = 0;
		
		// holds the the value to be removed
		// from h1sum and added to h2sum at each iteration
		long midvalue;
		
		boolean isready = false;
		private int index = 0;
			
		
		MaxSum(int size) {
			
			// size needs to be even as need to split
			// array evenly afterwards
			if (!(size%2==0)) {
				size = size + 1;
			}
			
			MAX_SIZE = size;
			MAX_SIZE_MINUS_ONE = MAX_SIZE-1;
			values = new long[MAX_SIZE];
			// initialize array values to zero
			for (int i = 0; i < MAX_SIZE; i++) {
				values[i]=0;
			}
			
			sizehalfarrays = MAX_SIZE/2;
			
		}
		
		
		public void add(long newvalue) {

			if(index > MAX_SIZE_MINUS_ONE) {
					// reset index to add newprice to begining of
					// values array
					isready = true;
					index = 0;
			}
			
			
			// adding latest received value to sum
			sum += newvalue;
			// remove oldest value in values array
			// from sum
			sum -= values[index];

			// as per definition in class instances
			midvalue = values[(index+sizehalfarrays)%MAX_SIZE];
			
			// h1sum adds newvalue to itself
			// since it keeps the sum of the 
			// most recent values received
			h1sum += newvalue;
			
			// removes oldest value from h1sum as per h1sum's
			// definition.
			// for example if values array size is 6 and index = 0
			// then h1sum now contains values at index 3,4,5 plus
			// newvalue (that is once the values array becomes isready as
			// per the condition above). Therefore to keep the sum
			// to 3 values we need to remove from h1sum the value at index 3
			// of values array. This value will then be added to h2sum
			
			h1sum -= midvalue;
			
			
			// ditto h1sum except h2sum keeps the oldest portion
			// of the values array
			h2sum += midvalue;
			h2sum -= values[index];
			
			
			// once all the sums have been updated
			// the oldest value in the values array
			// can be replaced with newvalue
			values[index] = newvalue;
			
			
			/////////////////

			++index;
			
		}
		
		/**
		 * Calculates the average of <code>sum</code>
		 * @return the average of <code>sum</code>
		 */
		public double getAverage() {

			return (double) ((sum*100)/ MAX_SIZE)/100;
		}
		
		public double getAverageh1() {

			return (double) ((h1sum*100)/ sizehalfarrays)/100;
		}
		
		public double getAverageh2() {

			return (double) ((h2sum*100)/ sizehalfarrays)/100;
		}
		
	}
	
	@Override
	public void register(OrderBookEventProcessor orderBookEventProcessor) {
		orderBookEventProcessor.add(this);
	}
	
	@Override
	public void notify(String instruname,OrderBookEvent orderBookEven){
		// not used in this strategy
	}
	
	
	@Override
	public void notify(String instruname,long askprice) {
		if(!instruname.equals(this.instruname)) {
			return;
		}
		processEvent(askprice);
	}

	private void processEvent(long askprice) {
	
		// TODO check if big diff between LastTradedPrice and bid/ask prices
		// TODO watch out autoboxing
		
		this.askprice = askprice;
		
		// store current time in milliseconds
		long newtime = System.nanoTime();
		long timevar = (newtime - prevtime)/1000000;
		
		
		this.timevar = timevar;
		
		timevars.add(timevar);

		prevtime = newtime;
		
		var = askprice - lastprice;
		lastprice = askprice;
		

		
		
		if (displaypriceindex > 0) {
			System.out.printf(instruname + " price is "+ askprice +", price change is "+var+", time change is " 
					+ ((double) timevar)/1000 + "s" + " events received at average time of %.3fs (h1) and %.3fs (h2)\n"
					,((double) timevars.getAverageh1())/1000,((double) timevars.getAverageh2())/1000);
		} else {
//			System.out.println(instruname + " price is "+ askprice +", price change is "+var+", time change is " 
//					+ ((double) timevar)/1000 + "s");
		}

		if (!(displaypriceindex==0)) {
			--displaypriceindex;
		}
		
		
		if(isMarketBusy()) {
			System.out.println(">>>> ALERT MARKET IS BUSY");
			displaypriceindex = 12;
		}

	}
	
	
	/**
	 * The market is deemed "busy" when either one of isPriceChanging and isSpeedIncreased
	 * return true
	 */
	private boolean isMarketBusy() {

		if (isSpeedIncreased() || isPriceChanging()) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns true when the number of successive increase in the average speed of market price updates
	 * received from the exchange over a period of time equal to TIMESIZE (called h1 in the print statement below)
	 * compared to the average speed in the preceding period (called h2 in the print statement below)
	 * is greater than the value in SPEEDCHANGE
	 * @return
	 */
	private boolean isSpeedIncreased() {
		if (timevars.isready && (timevars.getAverageh1()<timevars.getAverageh2())) {
			++speedincreaseindex;
			System.out.printf(instruname + " speed increasing, for "+speedincreaseindex
					+" times in a row. average speed is %.3fs (h1) vs %.3fs\n",((double) timevars.getAverageh1())/1000,((double) timevars.getAverageh2())/1000);
			if(!(speedincreaseindex < SPEEDCHANGE)){
				System.out.println(">>>> ALERT " + instruname + ">>  SPEED increased matterially");
				speedincreaseindex = 0;
				return true;
			}
			return false;
		}
		speedincreaseindex = 0;
		return false;
	}


	/**
	 * Returns true when the number of successive price variations of the same sign
	 * is equal to or greater than PRICECHANGE
	 */
	private boolean isPriceChanging() {
		
		signvar = signum(var);
		lastsignvar = lastsignvarsave;
		lastsignvarsave = signvar;
		
		if(((signvar == lastsignvar) && (signvar != 0))) {
			++pricechangeindex;
			System.out.println(instruname + " price changing for " + pricechangeindex 
					+" times in a row. Last price variation was "+var);
			if(!(pricechangeindex < PRICECHANGE)) {
				String signword;
				if (signvar<0)
					signword = "decreased";
				else signword = "increased";
				
				System.out.println(">>>>  ALERT "+ instruname + ">>  PRICE "+signword.toUpperCase()+" matterially");
				return true;
			// return false without reseting index
			} return false;
		}
		pricechangeindex = 1;
		return false;
	}
	

	int signum(long i) {
	    if (i == 0) return 0;
	    if (i >> 63 != 0) return -1;
	    return +1;
	}
	
	public static void main(String[] args) {
	}
}
