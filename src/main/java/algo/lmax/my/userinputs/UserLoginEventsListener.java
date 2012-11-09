package algo.lmax.my.userinputs;




/**
 *  Asynchronous listener for user login instructions.
 */
public interface UserLoginEventsListener {

	
	/**
	 * Called when a user login instruction event occurs on the input stream
	 * @param inputarray - the user instruction to process
	 */
	void notify(String[] inputarray);

}
