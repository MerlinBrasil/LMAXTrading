package algo.lmax.my.userinputs;

public interface UserInputsHandler {

	
	/**
	 * Register to user login instructions events
	 * 
	 * @param userLoginEventsListener The listener for all user login instruction updates
	 */
	public void registerUserLoginEvents(UserLoginEventsListener userLoginEventsListener);

	
	
	/**
	 * Register to user order instructions events
	 * 
	 * @param userOrderEventsListener The listener for all user order instruction updates
	 */
	public void registerUserOrderEvents(UserOrderEventsListener userOrderEventsListener);
	
	
	/**
	 * Register to user instrument requests instruction events
	 * 
	 * @param userInstrumentEvents The listener for all user instrument request updates
	 */
	public void registerUserInstrumentEvents(UserInstrumentEventsListener userInstrumentEventsListener);
	
}
