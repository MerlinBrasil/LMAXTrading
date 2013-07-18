package algo.lmax.userinputs;

public interface UserInstrumentEventsListener {

	/**
	 * Called when a user instrument instruction event occurs on the input stream
	 * This is the channel where most user instructions are sent to during the
	 * life of the program
	 * @param inputarray - the user instruction to process
	 */
	void notify(String[] inputarray);

}

