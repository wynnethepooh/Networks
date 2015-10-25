package project1;

public class MultipleClients implements Runnable
{
	private Thread thread;
	private String threadName;
	
	MultipleClients(String name)
	{
		threadName = name;
		System.out.println("Creating " + threadName);
	}
	
	public void run()
	{
		System.out.println("Running " + threadName);
		
		try
		{
			EchoClient client = new EchoClient();
		}
		catch (Exception err)
		{
			System.err.println(err);
		}
	}
	
	public void start()
	{
		if (thread == null)
		{
			thread = new Thread(this, threadName);
			thread.start();
		}
	}
}
