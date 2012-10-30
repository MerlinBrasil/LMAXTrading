package algo.lmax.my;

import com.lmax.api.Callback;
import com.lmax.api.FailureResponse;
import com.lmax.api.Session;
import com.lmax.api.heartbeat.HeartbeatCallback;
import com.lmax.api.heartbeat.HeartbeatEventListener;
import com.lmax.api.heartbeat.HeartbeatRequest;
import com.lmax.api.heartbeat.HeartbeatSubscriptionRequest;

public class HeartBeatHandler implements HeartbeatEventListener, Runnable {

	
	private Session session;
    public static Thread heartbeatthread;
    
	
	public HeartBeatHandler(Session session) {
		this.session = session;
		initialiseHeartBeat();
	}
	

	
    private void initialiseHeartBeat() {

        session.registerHeartbeatListener(this);

        session.subscribe(new HeartbeatSubscriptionRequest(), new Callback()
        {
            public void onSuccess()
            {System.out.println("subscribed to heartbeat event");}

            @Override
            public void onFailure(final FailureResponse failureResponse)
            {throw new RuntimeException("Failed");}
        });		
	}

	// Heartbeat event listener
    @Override
    public void notify(long accountId, String token)
    {
        System.out.printf("Received heartbeat: %d, %s%n", accountId, token);
    }
	
	@Override
	public void run() {		
		heartbeatthread = Thread.currentThread();
		System.out.println("starting heartbeat thread");
        while (!Thread.currentThread().isInterrupted()) {
        	try {
        		Thread.sleep(10000);
        		requestHeartbeat();
        	} catch (InterruptedException e) {
        	}
        }
	}
	
	
	
	
    private void requestHeartbeat()
    {
        session.requestHeartbeat(new HeartbeatRequest("token"), new HeartbeatCallback()
        {
            @Override
            public void onSuccess(String token)
            {}
            
            @Override
            public void onFailure(FailureResponse failureResponse)
            {
                throw new RuntimeException("requestHeartbeat() Failed.\n" +
                		"Check session is still alive");
            }
        });
    }     
}
