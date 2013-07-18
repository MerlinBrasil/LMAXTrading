package algo.lmax.userinputs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author julienmonnier
 * this class handles all the user instructions received on the
 * inputstream and forwards the instructions to the
 * listening classes.
 * instances of this class should be passed to any class that
 * needs to listen to events on the input stream
 *
 */
public class UserInputsHandlerImpl implements UserInputsHandler 
{

	private String input;
	private UserLoginEventsListener userLoginEventsListener;
	private UserOrderEventsListener userOrderEventsListener;
	private UserInstrumentEventsListener userInstrumentEventsListener;
	public volatile static String contextflag = null;


	
	
	public UserInputsHandlerImpl() {
		
		new Thread(new UserInput(this)).start();
		
	}
	
	/**
	 * this method is called back by UserInput when a new
	 * user instruction is available
	 * 
	 * @param input
	 */
	public void newInput(String input) {
		this.input = input;
		if (checkvalid())
			processInput();
	
	}

	
	

	/**
	 * This method checks that the input received by the user
	 * matches what the algo expects to receive.  For example
	 * during the login process the algo expects to receive a
	 * login info during a period of 6 seconds. After that it
	 * does not expects such info and will reject any such input
	 * @return
	 */
	private boolean checkvalid() {
		
		boolean bool = true;
	
		
		return bool;
	
	}


	private void processInput() {

		// first line filters
		
		if (input.equals("exit")) {
			
			System.out.println(">>> system exit");
			// add code for closing session
			System.exit(0);
		}
		
		// second line filters
		
		String[] inputarray = input.split(" ");
		
		if (contextflag == "login")
			userLoginEventsListener.notify(inputarray);
		

		if (contextflag.equals("instru"))
			userInstrumentEventsListener.notify(inputarray);
		
	}



	@Override
	public void registerUserLoginEvents(
			UserLoginEventsListener userLoginEventsListener) {
		this.userLoginEventsListener = userLoginEventsListener;
		contextflag = "login";
	}


	@Override
	public void registerUserOrderEvents(
			UserOrderEventsListener userOrderEventsListener) {
		this.userOrderEventsListener = userOrderEventsListener;		
	}


	@Override
	public void registerUserInstrumentEvents(
			UserInstrumentEventsListener userInstrumentEventsListener) {
		this.userInstrumentEventsListener = userInstrumentEventsListener;
		contextflag = "instru";
	}

}

	
//
//public static String getInputflag() {
//	return inputflag;
//}
//
//public static void setInputflag(String inputflag) {
//	UserInputHandler.inputflag = inputflag;
//}
//
//
//public void registerUserInputListener(UserInputEventListener object) {
//	
//	String key = "multi";
//	this.registerUserInputListener(key, object);
//
//}
//
//public void registerUserInputListener(String key, UserInputEventListener object) {
//	
//	
//	
//	listeners.add(somearay);
//	
//}
//
//
//	
	
	
	
	
	
	
	

