package scribbles;

import java.util.Iterator;

public class Sribble1 {

	int i = 10; // the number of thread to create
	
	private synchronized void somemethod(){
		
		while (i>1) {
			--i;
			System.out.println(Thread.currentThread().getName() + " now waiting");
			try {
				wait();
				System.out.println(Thread.currentThread().getName() + " has been awoken");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		};
		notifyAll();

	}
	
	class ThreadClass implements Runnable {
		
		Sribble1 q;
		
		ThreadClass(Sribble1 q) { 
			this.q = q; 
			new Thread(this).start();
			}
		
		@Override
		public void run() {
			q.somemethod(); // blocking
		}
	}
	
	public static void main(String[] args) throws InterruptedException {

		Sribble1 q = new Sribble1();
		int i = q.i;
		
		
		
		long finaltime;
		for (int j=0; j<i; j++) {
			q.new ThreadClass(q);
		}
		
		
		

	}
}
