package algo.lmax.my;

import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;

/**
 * @author julienmonnier
 * This class is used by several classes, hence
 * the choice of puting it in its own class file
 * rather than implementing it several times as
 * inner class
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
