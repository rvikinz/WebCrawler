package university.ucr.crwler.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool 
{

	static String seedURLFile = "//Users/vikashkumar//Documents//SeedURL//seedURL.txt";
	static String downloadDirPath = "//Users/vikashkumar//Documents//IR//DownloadNew//";
	static String URLsDirPath = "/Users/Piyush/Documents/workspace/Crawler/src";
	public static Queue< String > frontier = new LinkedList< String >();
	static ConcurrentHashMap< String, Long > mapChkSum = new ConcurrentHashMap< String, Long >();
	static int maxNumThread = 100;
	
	
	static ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> mapSites 
	= new ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>>();
	
	static long cnt = 0;
	static int levels = 0;
	static int maxLevels = 999;
	public static int filesDownloaded = 0;
	public static int maxFilesDownload = 100;


	
	public static void main(String[] args) {
		Integer threadCounter = 0;
		int noOfThread = -1;
		int noOfHopes = 99;
		String pathDocStore = "";
		String pathSeedURL = "";
		int totalNoPages = 1000;
		
		try
		{
			if( args.length == 5 )
			{
				noOfHopes = Integer.parseInt( args[0] );
				totalNoPages = Integer.parseInt( args[1] );
				pathDocStore = args[2] + "//";
				pathSeedURL = args[3] + "//";
				noOfThread = Integer.parseInt( args[4] );
			}
			else if( args.length == 4 )
			{
				noOfHopes = Integer.parseInt( args[0] );
				totalNoPages = Integer.parseInt( args[1] );
				pathDocStore = args[2];
				pathSeedURL = args[3];
			}
			else
			{
				System.out.println(" Number of input is less than require !!");
			}
			
			if( noOfHopes <= 0 || totalNoPages <= 0 || noOfThread <= 0 )
			{
				System.out.println(" Please enter number greater than 0 ");
				return;
			}
		}
		catch ( NumberFormatException e )
		{
			System.out.println(" \nERROR --> Please enter integer");
			return;
		}
		
		if ( totalNoPages == 0)
		{
			System.out.println(" Total number of pages to be downloaded is zero .. Setting by default - 100");
		}
		else
			maxFilesDownload = totalNoPages;
		
		if( pathSeedURL != null && pathSeedURL != "" )
			seedURLFile = pathSeedURL;
		else
		{
			System.out.println(" Seed URL path not provided");
			return;
		}	
		
		if( pathDocStore != null && pathDocStore != "" )
			downloadDirPath = pathDocStore;
		else
		{
			System.out.println(" Store Document path not provided:" );
			return;
		}
		
		if( noOfThread == -1 )
		{
			System.out.println(" Number of threads not provided, taking by default no of thead 100");
		}
		else
		{
			maxNumThread = noOfThread;
			//Create a new file to store the downloaded URLs
			File file = new File(downloadDirPath +"//downloadedURLs.txt");
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		// Initializing the frontier queue with Seed URLs from text file saved on disk
		try {
			
			Scanner in = new Scanner(new FileReader(ThreadPool.seedURLFile));
			while(in.hasNextLine()){
				//synchronized (WebCrawlerMultiThreaded.class )
				{
					ThreadPool.frontier.add(in.nextLine());
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("ERROR: Seed URL path not Valid !!");
			System.exit(0);
		}
		
		
		
		BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
		CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(50, maxNumThread, 5000, TimeUnit.MILLISECONDS, blockingQueue );
		
		executor.setRejectedExecutionHandler( new RejectedExecutionHandler(){
			
			public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
			{
				//System.out.println("Demo Task rejected: " + (( Tasks ) r ).getName() );
				
				try
				{
					Thread.sleep(1000);
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				
				//System.out.println("Lets add another time: " + ( (Tasks) r).getName() );
				executor.execute(r);
			}
		});
		
		// Lets start all core thread initially
		
		executor.prestartAllCoreThreads();
		
		while( true )
		{
			threadCounter++;
			//System.out.println("Adding Demo task: " + threadCounter );
			executor.execute(new WebCrawlerMultiThreaded( ));
		
			if( threadCounter == maxNumThread )
			{
				//System.out.println("stopinng ------");
				break;
			}
		}
		}
		}
}

