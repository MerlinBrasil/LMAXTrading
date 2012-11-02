package algo.lmax.my.userinputs;

import java.util.Scanner;

/**
 * @author julienmonnier
 * Takes user inputs from stdin and forwards the instructions
 * to the UserInputsHandlerImpl class for further dispatching
 * to listening objects
 */
public class UserInput implements Runnable {

	private Scanner sc;
	private UserInputsHandlerImpl uihandler;

	
	UserInput(UserInputsHandlerImpl uihandler) {
		
		this.uihandler = uihandler;		
		sc = new Scanner(System.in);

	}
	
	private void startCapture() {
		

		while (!Thread.currentThread().isInterrupted()) {
			
	    	String input = sc.nextLine();

	    	uihandler.newInput(input);
	    	
		}
	}

	@Override
	public void run() {
		startCapture();
	}

}
