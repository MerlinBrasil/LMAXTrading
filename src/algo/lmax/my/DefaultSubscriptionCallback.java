package algo.lmax.my;

import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;

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
