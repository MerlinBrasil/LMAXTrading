package algo.lmax.my.userinputs;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import algo.lmax.my.DefaultSubscriptionCallback;
import algo.lmax.my.HeartBeatHandler;
import algo.lmax.my.InstrumentsInfo;
import algo.lmax.my.InstrumentsInfo.getID;
import algo.lmax.my.LmaxTrading;

import com.lmax.api.Session;
import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;
import com.lmax.api.account.AccountStateEvent;
import com.lmax.api.account.AccountStateEventListener;
import com.lmax.api.account.AccountStateRequest;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;

/**
 * @author julienmonnier
 * Processes user instructions received from UserInputsHandlerImpl class
 * that concern order execution, general instruments and account admin task
 * 
 * TODO implement order execution (use own class as needs to be accessed by other
 * classes in future.)
 * 		implement flexi search of instruments (use regexp)
 * 		implement 'add new intrument' feature from list of all instruments 
 */
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
		newInstruction(inputarray);
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

    

	private void newInstruction(String[] inputarray) {
		String[] i = inputarray;
		String baseintruction = i[0];
		
    	if (i.length > 1) {
			String instruction1 = i[1];
			if (baseintruction.equals("add") && isInstru(instruction1)) {
		        session.subscribe(new OrderBookSubscriptionRequest(InstrumentsInfo.getID.byName(instruction1)), 
		        		new DefaultSubscriptionCallback(instruction1));
		        
			}
			// make second condition regexp 'instru*'
			else if (baseintruction.equals("print") && instruction1.equals("instru")) {
				InstrumentsInfo.printAll();
			} 
			else if (baseintruction.equals("acc")) {
				// NOTE this call will trigger a callback of the
				// notify method of the AccountStateEventListener
					session.requestAccountState(new AccountStateRequest(), new Callback() {	
						@Override public void onSuccess() {}
						@Override public void onFailure(FailureResponse failureResponse) {}
					});
			}
		} else if (baseintruction.equals("stop")) {
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
					waitStoringDataFinished(); // blocking method
					System.exit(0);
				}
				
				@Override
				public void onFailure(FailureResponse failureResponse) {
					System.out.println(failureResponse.toString());
				}
			});	
		}
	}


	// checks whether the argument matches an LMAX instrument name
	private boolean isInstru(String instru) {
		if(InstrumentsInfo.getID.byName(instru) == null)
			return false;
		return true;
	}
	
	private void waitStoringDataFinished() {
		
		// when closing the system, either of obevents and
		// obeventstp need to be writen to file manually
		// if LmaxTrading.obeventsuse = true then it means
		// obevents may have some data and needs to be saved
		// manually AFTER ensuring that obeventstp is empty
		// if LmaxTrading.obeventsuse = false then it means
		// obeventstp may have some data and needs to be saved
		// manually AFTER ensuring that obevents is empty
		
		
		if (LmaxTrading.obeventsuse) {
		
			// if above condition is true, it means that
			// obeventstp is in the process of being written to file
			// or has already been written to file.  In any case
			// we need to ensure obeventstp is empty before proceeding
			// to save obevents manually to file
			while(LmaxTrading.obeventstp.size()>0){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			storeToFile(LmaxTrading.obevents);
			return;
			
		}
		else {
			
			// LmaxTrading.obeventsuse is false it means
			// obevents is in the process of being written to file
			// or has already been written to file.  In any case
			// we need to ensure obeventstp is empty before proceeding
			// to save obeventstp manually to file
			while(LmaxTrading.obevents.size()>0){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			storeToFile(LmaxTrading.obeventstp);
			return;
			
		}
	}
	
	
	private void storeToFile(LinkedList<String> obevents) {
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

	}
	
	
		
}

	
	


