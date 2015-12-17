package university.ucr.crwler.crawler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor
{
	public CustomThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue )
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	protected void beforeExecute( Thread t, Runnable r)
	{
		super.beforeExecute(t, r);
		//System.out.println(" Perform before execution()  logic");
	}
	
	protected void afterExecute( Runnable r, Throwable t )
	{
		super.afterExecute( r, t );
		if( t != null )
		{
			//System.out.println( " Perform execution handler logic ");
		}
		
		//System.out.println("Perform afterExecute() logic ");
	}
}
