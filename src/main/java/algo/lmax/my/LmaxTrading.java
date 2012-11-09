package algo.lmax.my;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.swing.plaf.SliderUI;


import algo.lmax.my.LoginBuilder.LoginInfo;
import algo.lmax.my.strategies.IsMarketBusy;
import algo.lmax.my.strategies.Strategy;
import algo.lmax.my.userinputs.UserInputsHandler;
import algo.lmax.my.userinputs.UserInputsHandlerImpl;
import algo.lmax.my.userinputs.UserRequestsHandler;

import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;
import com.lmax.api.FixedPointNumber;
import com.lmax.api.LmaxApi;
import com.lmax.api.Session;
import com.lmax.api.account.LoginCallback;
import com.lmax.api.account.LoginRequest;
import com.lmax.api.account.LoginRequest.ProductType;
import com.lmax.api.heartbeat.HeartbeatCallback;
import com.lmax.api.heartbeat.HeartbeatEventListener;
import com.lmax.api.heartbeat.HeartbeatRequest;
import com.lmax.api.heartbeat.HeartbeatSubscriptionRequest;
import com.lmax.api.internal.events.OrderBookEventImpl;
import com.lmax.api.order.CancelOrderRequest;
import com.lmax.api.order.LimitOrderSpecification;
import com.lmax.api.order.Order;
import com.lmax.api.order.OrderCallback;
import com.lmax.api.order.OrderEventListener;
import com.lmax.api.order.OrderSubscriptionRequest;
import com.lmax.api.orderbook.OrderBookEvent;
import com.lmax.api.orderbook.OrderBookEventListener;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;
import com.lmax.api.orderbook.PricePoint;
import com.lmax.api.reject.InstructionRejectedEvent;
import com.lmax.api.reject.InstructionRejectedEventListener;

/**
 * This is the main class from where the program is started.
 * <p>the <tt>main</tt> method initiates the following phases
 * in order:
 * <ul>
 * <li>instantiate a user inputs handler object
 * <li>get login credentials
 * <li>load instruments lists
 * <li>create a new LMAX api login call, passing it an instance
 * of this class
 * </ul>
 * the last phase triggers a callback of {@link #onLoginSuccess},
 * which provides us with a {@link com.lmax.api.Session session} object.
 * This <tt>session</tt> object is then used to register this class for various
 * asynchronous events updates coming from the exchange and from the user inputs
 * events channel handled by {@link UserInputsHandlerImpl}.
 * <p>Once this is done, the session is started and kicks the
 * process of listening and processing the asynchronous events.
 * 
 * @author julienmonnier
 */
public class LmaxTrading implements LoginCallback, OrderBookEventListener
{

    enum OrderState
    {
        NONE, PENDING, WORKING
    }

    private Session session;
    private static UserInputsHandler uihandler;
    private final long instrumentId;
    private final FixedPointNumber tickSize;
    private final String instrumentName;
    private Strategy eurusdstrat;
    private Strategy oilstrat;
    private Strategy ws30strat;
    List<Strategy> stratlist;
    public static LinkedList<String> obevents = new LinkedList<String>();
    public static LinkedList<String> obeventstp = new LinkedList<String>();
    public static boolean obeventsuse = true;
    public static boolean obeventstpsizenull = true;
    public static int obeventsindex = 0;
    public static long obeventrowindex = 0;
    public static Lock obeventslock = new ReentrantLock();
    public static int obeventsstoringindex = 0;
    
    
    private final OrderTracker buyOrderTracker = new OrderTracker();
    private final OrderTracker sellOrderTracker = new OrderTracker();
    private Thread heartbeatthread;

    public LmaxTrading(long instrumentId, FixedPointNumber tickSize, String instrumentName)
    {
        this.instrumentId = instrumentId;
        this.tickSize = tickSize;
        this.instrumentName = instrumentName;
    }


    
    @Override
    public void notify(OrderBookEvent orderBookEvent)
    {
    	
//    	long t1 = System.nanoTime();
    	String instruname = InstrumentsInfo.getSymbol.byID(String.valueOf(orderBookEvent.getInstrumentId()));
//    	System.out.println("event received for "+instruname+", price is "+orderBookEvent.getAskPrices().get(0).getPrice());
    	
    	
    	
    	
    	//long timepublishedvar = System.currentTimeMillis() - orderBookEvent.getTimeStamp();
    	

    	
    	

    	
    	
    	
    	
    	
    	
    	
    			
        long askprice = orderBookEvent.getAskPrices().get(0).getPrice().longValue();
        
        for (Iterator<Strategy> iterator = stratlist.iterator(); iterator.hasNext();) {
			iterator.next().notify(instruname,askprice);
		}
        
        // React to price updates from the exchange.
        //handleBidPrice(orderBookEvent.getBidPrices());
        //handleAskPrice(orderBookEvent.getAskPrices());
        
    	
    	
    	addToList(orderBookEvent);
//    	System.out.println("envent was processed in " + (System.nanoTime()-t1)/1000 + " microseconds");
    	
    }


    /**
	* Saves order book events info into a list (the 'main' list). 
	* once main list has reached a certain size, store its values to a file (file 'A').
	* The copy to file A process runs in a separate thread. This is to
	* prevent the system from hanging while the file A is being written
	* As a result of this process a temporary list takes the new order book
	* updates coming from the exchange while the main list is being written to file A.
	* Once the main list is free again, the values in the temp list get written to file A
	* in a separate thread to avoid the system to hang so main list can take new order book
	* events update.
	* 
	* @param orderBookEvent
    */
    private void addToList(OrderBookEvent orderBookEvent) {
    	
    	++obeventrowindex;
    	System.out.println("adding obevent to row: "+obeventrowindex);
    	String instruname = InstrumentsInfo.getSymbol.byID(String.valueOf(orderBookEvent.getInstrumentId()));
    	String obevent = (orderBookEvent.toString()+", timereceived="+"'"+System.currentTimeMillis()+"'"+", instruname="+instruname+", obeventrow="+obeventrowindex);
    	
    	obeventslock.lock();
    	// this if condition is true as long as obeventsindex is less than a set
    	// value (currently 100)
    	if (obeventsuse && obeventstpsizenull) {
    		obevents.add(obevent);
    		++obeventsindex;
//    		System.out.println("added event to obevents, index is "+obeventsindex);
    		
    	
    	// this is true only once per cycle of storing to file
		} else if (obeventsuse && !obeventstpsizenull) {
//			System.out.println("storing obeventsTP to file");
			new Thread(new StoreOrderBookEventsToFile(obeventstp)).start();
			// technically it is not yet null as the thread above is still running
			// but this is necessary to avoid this else if condition true again on the
			// next price update
			obeventstpsizenull = true;
			// statement below is a dup with statement in first if condition above,
			// need improve the overall if statements logic to avoid
			// this.
			obevents.add(obevent);
			++obeventsindex;
		// this becomes true as soon as the first if condition becomes false
		} else {
			// this assumes that obeventstp is empty by the time this else condition
			// becomes true.
//			System.out.println("adding event"+ orderBookEvent.getTimeStamp() +" (time published) to obeventsTP");
			obeventstp.add(obevent);
			obeventstpsizenull = false;
		}
    	obeventslock.unlock();
    	
    	if (!(obeventsindex<500)) {
    		
    		System.out.println("now about to write to file");
    		
    		obeventsuse = false;
    		obeventsindex = 0;
			
			
			new Thread(new StoreOrderBookEventsToFile(obevents)).start();
		}
		
	}


	public class StoreOrderBookEventsToFile implements Runnable {
    	
    	private LinkedList<String> obevents;
    	
    	public StoreOrderBookEventsToFile(LinkedList<String> obevents) {
    		
    		this.obevents = obevents;
    		
    		
    	}
    	
    	
    	public void run() {
    		++obeventsstoringindex;
    		System.out.println("storing index is: "+obeventsstoringindex);
    		File file = new File("obevents.txt");
    		// if file doesnt exists, then create it
    		if (!file.exists()) {
    			try {
    				file.createNewFile();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		
    		try {
    			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
    			BufferedWriter bw = new BufferedWriter(fw);
    			
    			for (Iterator<String> iterator = obevents.iterator(); iterator
    					.hasNext();) {
    				bw.write(iterator.next()+"\n");
    			}
    			
    			bw.close();
    		} catch (IOException e) {}
    		
    		obevents.clear();
    		
    		if (!obeventsuse) {
    			obeventslock.lock();
    			
    			obeventsuse = true;
    			obeventslock.unlock();						
    		}

    		--obeventsstoringindex;
    	}
    }
    
    
    @Override
    public void onLoginSuccess(Session session)
    {
    	
        System.out.println("My accountId is: " + session.getAccountDetails().getAccountId());

        // Hold onto the session for later use.
        this.session = session;

        // Add a listener for order book events.
        session.registerOrderBookEventListener(this);
        
        // session.registerOrderEventListener(this);
        // session.registerInstructionRejectedEventListener(this);

        // Subscribe to my order events.
        // session.subscribe(new OrderSubscriptionRequest(), new DefaultSubscriptionCallback());
        // Subscribe to the order book that I'm interested in

 

        // keep session alive process (sends heartbeat requests to platform)
    	new Thread(new HeartBeatHandler(session)).start();

		// creating a new object to read user inputs relating to instruments
    	// and account admin requests (like adding a new instrument to the tracked
    	// instruments.)
		new UserRequestsHandler(session, uihandler);    

		
		String[] instrutoregister = {"EURUSD","CLZ2"};
		for (String i : instrutoregister) {
			session.subscribe(new OrderBookSubscriptionRequest(InstrumentsInfo.getID.byName(i)), 
					new DefaultSubscriptionCallback(i));
		}

		
		eurusdstrat = new IsMarketBusy("EUR/USD");
		oilstrat = new IsMarketBusy("CLZ2");
		// should be created only if instru is being registered for
		// order book events as for "EURUSD" and "CLZ2" above
		// need sorting
		ws30strat = new IsMarketBusy("WS30");
		
		stratlist = new ArrayList<Strategy>();
		stratlist.add(eurusdstrat);
		stratlist.add(oilstrat);
		stratlist.add(ws30strat);
		
        // Start the event processing loop, this method will block until the session is stopped.
        session.start();
    }

    


    @Override
    public void onLoginFailure(FailureResponse failureResponse)
    {
        System.out.println("Login Failed: " + failureResponse);
    }

    public static void main(String[] args)
    {
		
    	// static ref is created so it can be passed to listener objects
    	// at any point in time during the life of the algo
    	uihandler = new UserInputsHandlerImpl();
    	
    	// calls login setup process
    	// this static method block until login creds are available
    	LoginInfo loginInfo = LoginBuilder.getLoginDetails(uihandler);
       
    	
        String url = loginInfo.url;
        String username = loginInfo.loginname;
        String password = loginInfo.password;
        ProductType productType = ProductType.valueOf(loginInfo.enviro.toUpperCase()); // returns values are "CFD_LIVE" or "CFD_DEMO"

        
        LmaxApi lmaxApi = new LmaxApi(url);
        
        // creating an anonymous instance of InstrumentInfo class so as
        // initialise the static HashMaps in the class.
        // see class doc for more info

        InstrumentsInfo.loadInstruments();
        LmaxTrading trackInstruments = new LmaxTrading(InstrumentsInfo.getID.byName("EURUSD"), 
        		InstrumentsInfo.getTickSize.byName("EURUSD"), InstrumentsInfo.getSymbol.byName("EURUSD"));

        
        // trackInstruments is passed so that the API can callback our onLoginSuccess or onLoginFailure methods
        lmaxApi.login(new LoginRequest(username, password, productType), trackInstruments);

    }
      
    
    private static class OrderTracker
    {
        private long instructionId = -1;
        private long cancelInstructionId = -1;
        private OrderState orderState = OrderState.NONE;
        private FixedPointNumber price;

        public long getInstructionId()
        {
            return instructionId;
        }

        public void setInstructionId(long instructionId)
        {
            this.instructionId = instructionId;
        }

        public long getCancelInstructionId()
        {
            return cancelInstructionId;
        }

        public void setCancelInstructionId(long cancelInstructionId)
        {
            this.cancelInstructionId = cancelInstructionId;
        }

        public OrderState getOrderState()
        {
            return orderState;
        }

        public void setOrderState(OrderState orderState)
        {
            this.orderState = orderState;
        }

        public FixedPointNumber getPrice()
        {
            return price;
        }

        public void setPrice(FixedPointNumber price)
        {
            this.price = price;
        }
    }

    private abstract static class DefaultOrderCallback implements OrderCallback
    {
        @Override
        public void onFailure(FailureResponse failureResponse)
        {
            System.err.println("Failed to place order: " + failureResponse);
        }
    }


}




//@Override
//public void notify(InstructionRejectedEvent instructionRejected)
//{
//  System.err.println(instructionRejected);
//
//  final long instructionId = instructionRejected.getInstructionId();
//  if (buyOrderTracker.getCancelInstructionId() == instructionId)
//  {
//      buyOrderTracker.setOrderState(OrderState.NONE);
//  }
//  else if (sellOrderTracker.getCancelInstructionId() == instructionId)
//  {
//      sellOrderTracker.setOrderState(OrderState.NONE);
//  }
//}
//
//@Override
//public void notify(Order order)
//{
//  OrderState stateForOrder = getStateForOrder(order);
//  System.out.printf("State for order: %d, state: %s%n", order.getInstructionId(), stateForOrder);
//
//  if (order.getInstructionId() == buyOrderTracker.getInstructionId())
//  {
//      buyOrderTracker.setOrderState(stateForOrder);
//  }
//  else if (order.getInstructionId() == sellOrderTracker.getInstructionId())
//  {
//      sellOrderTracker.setOrderState(stateForOrder);
//  }
//}
//
//private OrderState getStateForOrder(Order order)
//{
//  if (order.getCancelledQuantity() == FixedPointNumber.ZERO &&
//      order.getFilledQuantity() == FixedPointNumber.ZERO)
//  {
//      return OrderState.WORKING;
//  }
//
//  return OrderState.NONE;
//}
//
//private void handleAskPrice(List<PricePoint> askPrices)
//{
//  handlePriceChange(askPrices, sellOrderTracker, FixedPointNumber.ONE.negate(), tickSize.negate());
//}
//
//private void handleBidPrice(List<PricePoint> bidPrices)
//{
//  handlePriceChange(bidPrices, buyOrderTracker, FixedPointNumber.ONE, tickSize);
//}
//
//private void handlePriceChange(List<PricePoint> prices, final OrderTracker orderTracker, final FixedPointNumber quantity, final FixedPointNumber priceDelta)
//{
//  final FixedPointNumber currentPrice = orderTracker.getPrice();
//  PricePoint bestPrice = prices.size() != 0 ? prices.get(0) : null;
//
//  // Make sure we have a best bid price, and it's not the same as the order we just placed
//  // and place similar to the ask price change.
//  if (bestPrice != null && (currentPrice == null || currentPrice == bestPrice.getPrice()))
//  {
//      switch (orderTracker.getOrderState())
//      {
//          // Place an order inside the spread if there isn't one currently in the market
//          case NONE:
//              orderTracker.setPrice(FixedPointNumber.valueOf(bestPrice.getPrice().longValue() + priceDelta.longValue()));
//
//              LimitOrderSpecification order =
//                  new LimitOrderSpecification(instrumentId, orderTracker.getPrice(), quantity, TimeInForce.GOOD_FOR_DAY);
//
//              session.placeLimitOrder(order, new DefaultOrderCallback()
//              {
//                  public void onSuccess(long instructionId)
//                  {
//                      System.out.println("Placed Order: " + instructionId);
//                      orderTracker.setOrderState(OrderState.PENDING);
//                      orderTracker.setInstructionId(instructionId);
//                  }
//              });
//              break;
//
//          // Cancel a working order on a price change.
//          case WORKING:
//              cancelOrder(orderTracker);
//
//          default:
//              // No-op
//      }
//  }
//}
//
//private void cancelOrder(final OrderTracker orderTracker)
//{
//  final long instructionId = orderTracker.getInstructionId();
//
//  if (instructionId != -1)
//  {
//      CancelOrderRequest cancelOrderRequest = new CancelOrderRequest(instrumentId, instructionId);
//      session.cancelOrder(cancelOrderRequest, new DefaultOrderCallback()
//      {
//          public void onSuccess(long cancelInstructionId)
//          {
//              System.out.println("Cancled Order: " + cancelInstructionId);
//              orderTracker.setCancelInstructionId(cancelInstructionId);
//          }
//      });
//  }
//}
//
//
//

