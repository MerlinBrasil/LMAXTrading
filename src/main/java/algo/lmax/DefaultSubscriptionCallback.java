package algo.lmax;

import algo.lmax.userinputs.UserInputsHandler;

import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;

/**
 * Provides implementation of Callback interface
 * for event registration methods used in other
 * parts of the code.
 * 
 * @see {@link UserInputsHandler}, {@link LmaxTrading}
 * 
 * @author julienmonnier
 */
public final class DefaultSubscriptionCallback implements Callback
{
	public DefaultSubscriptionCallback(String key) {
		// TODO Auto-generated constructor stub
		this.instru = key;
	}
	
	private String instru;
	

    public void onSuccess()
    {
    	System.out.println("Subscribing to " + instru + " order book events");
    }

    @Override
    public void onFailure(final FailureResponse failureResponse)
    {
        throw new RuntimeException("Failed");
    }

}
