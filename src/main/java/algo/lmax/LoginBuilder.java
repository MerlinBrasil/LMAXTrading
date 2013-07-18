package algo.lmax;


import java.io.IOException;
//import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//import org.apache.commons.io.ProxyInputStream;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.ProxyInputStream;

import com.espertech.esper.epl.generated.EsperEPL2GrammarParser.forExpr_return;

import algo.lmax.userinputs.UserInputsHandler;
import algo.lmax.userinputs.UserInputsHandlerImpl;
import algo.lmax.userinputs.UserLoginEventsListener;

/**
 * Handles the setup process of user login credentials
 * User login setup requires the user to provide
 * a login environment first and then a password (a third step is
 * then automatically triggered to set up the final login
 * credentials object).
 * The main method in this class is {@link #login}. This method 
 * is called at each stage of the login process, hence
 * it is called at least twice.
 * {@code login} calls three methods in turn.  Each of those three methods
 * use the {@link #loginstage} variable to determine at which stage in the login 
 * process we are at and hence what to do.
 * 
 * <p>A {@code DefaultLogin} thread is spawn at the start of the login credentials
 * creation process in {@link #startLoginCreation()}. This thread sets a default 
 * environment variable after a set period of time if the user does not do so 
 * manually (the default environment variable is currently set to 'test').
 * 
 * <p>{@link #getLoginDetails} is the static public method for getting
 * the login details.
 * 
 * @author julienmonnier
 */
public final class LoginBuilder implements UserLoginEventsListener {


	private static LoginInfo logininfo = null;
	private String loginenviro;
	private Lock loginlock = new ReentrantLock();
	private boolean issetlogin = false;
	private int loginstage = 0;
	private String password = null;
	private String[] finallogin = null;
	// TODO store those values in a text file or db
	private String[] testlogin = {"https://testapi.lmaxtrader.com/",
	                                 "QuantMetTest",
	                                 password,
	                                 "CFD_DEMO"};
	private String[] livelogin = {"https://api.lmaxtrader.com/",
        "QuantMetric",
        password,
        "CFD_LIVE"};
	
	
	/**
	 * Registers this class to receive uihandler's user login events
	 */
	public LoginBuilder(UserInputsHandler uihandler) {
		uihandler.registerUserLoginEvents(this);
	}

	@Override
	public void notify(String[] inputarray) {
		loginlock.lock();
		if(!issetlogin){
			login(inputarray);			
		}
		loginlock.unlock();
	}
	
	/** 
	 * @param inputarray - see {@link #notify}
	 */
	private void login(String[] inputarray) {
		setLoginEnviron(inputarray);
		setPassword(inputarray);
		setFinalLogin();
	}

	private void setLoginEnviron(String[] inputarray) {
		if (loginstage == 0) {
			if(inputarray[0].equals("live") || inputarray[0].equals("test")){
				loginenviro = inputarray[0];
				++ loginstage;
			} else { System.out.println("please provide correct login enviro: " +
					"live or test");
			}
		}
	}
	
	private void setPassword(String[] inputarray) {
		if (loginstage == 1)
			System.out.println("Please enter " + loginenviro + " password");
		
		if (loginstage == 2) {
			password = inputarray[0];
			testlogin[2] = password;
			livelogin[2] = password;
		}
		++ loginstage;
	}
	
	private void setFinalLogin() {
		if (loginstage==3) {
			if (loginenviro.equals("live")) {
				finallogin = livelogin;
			} else if (loginenviro.equals("test")) {
				finallogin = testlogin;
			}				
			// setting fields of LoginInfo
			logininfo = new LoginInfo();
			logininfo.url = finallogin[0];
			logininfo.loginname = finallogin[1];
			logininfo.password = finallogin[2];
			logininfo.enviro = finallogin[3];
			issetlogin = true;
		}
	}
	
	public class LoginInfo {
		String url;
		String loginname;
		String password;
		String enviro;
	}

	
	/**
	 * @author julienmonnier
	 * Handles default login process.
	 * This thread waits 6 seconds, after which
	 * it sets the default login enviro automatically (currently it is 'test')
	 * and calls the normal login process, which will prompt for a password.
	 * If the user has already chosen the login enviro manually
	 * this thread dies after 6 seconds with no effect
	 */
	private class DefaultLogin implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			loginlock.lock();
			// a login level other than 0 means that the manual login
			// process is on going and hence the thread skips the
			// block i.e. the default login does not occur
			if (loginstage == 0) {
				String[] defaultenviro = new String[1];
				defaultenviro[0] = "test";
				System.out.println("Setting " + defaultenviro[0] + " login environ ...");
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				login(defaultenviro);
			}
			loginlock.unlock();
		}
	}

	
	private void startLoginCreation() {
		(new Thread(new DefaultLogin())).start();
		System.out.println("Please enter your login");
		while (!issetlogin) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * Called by the client to retrieve the {@link #LoginInfo} object.
	 * The first time this method is called, it triggers the login
	 * credentials creation process.
	 * @param uihandler the user instructions event propagator
	 * @return
	 * @see #UserInputsHandler
	 * @see #startLoginCreation
	 */
	public static LoginInfo getLoginDetails(UserInputsHandler uihandler) {
		if(logininfo == null)
			new LoginBuilder(uihandler).startLoginCreation();
		return logininfo;
	}
}


