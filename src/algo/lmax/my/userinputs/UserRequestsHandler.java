package algo.lmax.my.userinputs;



import java.util.Scanner;

import algo.lmax.my.DefaultSubscriptionCallback;
import algo.lmax.my.HeartBeatHandler;
import algo.lmax.my.InstrumentsInfo;
import algo.lmax.my.InstrumentsInfo.getID;

import com.lmax.api.Session;
import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;
import com.lmax.api.account.AccountStateEvent;
import com.lmax.api.account.AccountStateEventListener;
import com.lmax.api.account.AccountStateRequest;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;

public class UserRequestsHandler implements AccountStateEventListener, UserInstrumentEventsListener {
	

	private Session session;
	private UserInputsHandler uihandler;

	private Boolean accregistered = false;
    
	public UserRequestsHandler(Session session, UserInputsHandler uihandler) {
		// TODO Auto-generated constructor stub
		this.session = session;
		this.uihandler = uihandler;
		uihandler.registerUserInstrumentEvents(this);
		session.registerAccountStateEventListener(this);
	}

	
	// the user instrument instruction event channel
	@Override
	public void notify(String[] inputarray) {
		instructionReceived(inputarray);
	}
	
    @Override
	public void notify(AccountStateEvent accountStateEvent) {
 
    	// the first call back is triggered on registration (which
    	// is initialised automatically). So the condition below
    	// ensures that we print only when the user requests an
    	// account state info
		if (accregistered.equals(true)) {
	    	System.out.println(accountStateEvent.toString());
		} else
    		accregistered = true;
	}

    

	private void instructionReceived(String[] inputarray) {
		String[] i = inputarray;
		String baseintruction = i[0];
		
    	if (baseintruction.equals("stop")) {
    		System.out.println("stop session listeners");    		
    		// TODO seems to block after market is closed
    		session.stop();

    		System.out.println("loging out of session");
			session.logout(new Callback() {
				
				@Override
				public void onSuccess() {
					System.out.println("you are now logged out");
					HeartBeatHandler.heartbeatthread.interrupt();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(">>>  system will now exit");
					System.exit(0);
				}
				
				@Override
				public void onFailure(FailureResponse failureResponse) {
					System.out.println(failureResponse.toString());
				}
			});
			

			

		} else if (baseintruction.equals("add")) {
			String instru = i[1];
			if (instru.equals("USDJPY") || instru.equals("USDSGD") || instru.equals("EURUSD")) {

		        session.subscribe(new OrderBookSubscriptionRequest(InstrumentsInfo.getID.byName(instru)), 
		        		new DefaultSubscriptionCallback(instru));
			}

		
		} else if (baseintruction.equals("acc")) {
			// NOTE this call will trigger a callback of the
			// notify method of the AccountStateEventListener
				session.requestAccountState(new AccountStateRequest(), new Callback() {	
					@Override public void onSuccess() {}
					@Override public void onFailure(FailureResponse failureResponse) {}
				});
		}	
	}
}
	
	


