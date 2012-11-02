package algo.lmax.my;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;


import algo.lmax.my.LoginBuilder.LoginInfo;
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
        System.out.println(InstrumentsInfo.getSymbol.byID(String.valueOf(orderBookEvent.getInstrumentId()))
        		+ " " + orderBookEvent.getLastTradedPrice().toString());

        // React to price updates from the exchange.
        //handleBidPrice(orderBookEvent.getBidPrices());
        //handleAskPrice(orderBookEvent.getAskPrices());
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

        session.subscribe(new OrderBookSubscriptionRequest(instrumentId), new DefaultSubscriptionCallback(instrumentName));

        // keep session alive process (sends heartbeat requests to platform)
    	new Thread(new HeartBeatHandler(session)).start();

		// creating a new object to read user inputs relating to instruments
    	// and account admin requests (like adding a new instrument to the tracked
    	// instruments.)
		new UserRequestsHandler(session, uihandler);       
        
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

