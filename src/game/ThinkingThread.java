package game;

class ThinkingThread extends Thread 
{
	private final PacManSimulator pacManSimulator;
	public boolean thinking = false;
    private IThinkingMethod method;
    private boolean alive;
    
    public ThinkingThread(PacManSimulator pacManSimulator, String name, IThinkingMethod method) 
    {
    	super(name);
		this.pacManSimulator = pacManSimulator;
        this.method = method;
        alive=true;
        start();
    }

    public synchronized  void kill() 
    {
        alive=false;
        notify();
    }
    
    public synchronized void alert()
    {
        notify();
    }

    public void run() 
    {
    	 try {
        	while(alive) 
	        {
	        	try {
	        		synchronized(this)
	        		{
        				wait(); // waked-up via alert()
	                }
	        	} catch(InterruptedException e)	{
	                e.printStackTrace();
	            }

	        	if (alive) {
	        		thinking = true;
	        		method.think();
	        		thinking = false;
	        		try {
	        			this.pacManSimulator.thinkingLatch.countDown();
	        		} catch (Exception e) {
	        			// thinkingLatch may be nullified...
	        		}
	        	} 
	        	
	        }
        } finally {
        	alive = false;
        }
    }
}
