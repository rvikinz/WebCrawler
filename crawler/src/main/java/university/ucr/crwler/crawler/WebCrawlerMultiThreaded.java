package university.ucr.crwler.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;



public class WebCrawlerMultiThreaded implements Runnable
{
	
	
	public void run()
	{
		try
		{
        	Parser.startCrawling();
 		}
		catch( InterruptedException e )
		{
			//e.printStackTrace();
			System.out.println("Thread Interupted");
		}
		catch( IOException ioex )
		{
			ioex.printStackTrace();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	// Getting the stored sead URL from the text file
    public void getSeedURL( String seedURLFileLoc ) throws IOException
    {
    	FileReader fr = new FileReader( seedURLFileLoc );
		BufferedReader br = new BufferedReader( fr );
    	
		try
    	{
    		String line;
    	
    		while( ( line = br.readLine() ) != null )
    		{
    		}
    	}
    	finally
    	{
    		br.close();
    	}
    	
    }
    
    // Download the web pages pointed by URL
    public  String downloadURL() throws IOException , MalformedURLException, Exception
    {
    	String url;
    	//String site;
    	URL urlObj = null;
    	long fileNo = 0;
    	boolean isCrawlPermit = true;
    	FileWriter fw ;
    	BufferedWriter bw;
    	String fileName = "";
    	
    	while( ThreadPool.frontier.size() > 0 )
    	{
    		//url = frontier.remove(); 	
    		url = "";
    		
    		//urlObj = new URL( url );
    		//site = urlObj.getHost();
    		//ConcurrentHashMap<String, Boolean> m = App.mapSites.get(site);
    		//if( m == null )
    		{
    		//	m = new ConcurrentHashMap<String, Boolean>();
    		//	mapSites.put( site, m );
    		
    			isCrawlPermit  = RobotExclusionUtil.robotsShouldFollow( url );
    			System.out.println("Crwaling Allowed: " + isCrawlPermit );
    			if( isCrawlPermit )
    			{
    				HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
    				InputStreamReader isr = new InputStreamReader( conn.getInputStream() );
    				BufferedReader br = new BufferedReader( isr );
	    		
	    				
    				String fileString = "";
    		
    				while( br.ready() )
    				{
    					String line = br.readLine();
    					fileString += ( line + "\n" );
    				}
    				
    				String chkSum = getCheckSum( fileString );
    				// Check for duplicates pages
    				if( !ThreadPool.mapChkSum.containsKey( chkSum ) )
    				{
    					ThreadPool.mapChkSum.put( chkSum, fileNo );
    					
    					fileName = "file" + (fileNo) + ".html";
	    				System.out.println("file Path: " + fileName );
	    		
	    				// Open a file to write
	    			    fw = new FileWriter( ThreadPool.downloadDirPath + fileName );
	    				bw = new BufferedWriter( fw );
    					bw.write( fileString );
        				br.close();
        				bw.close();
    				}
    				else
    				{
    					System.out.println( "equal file found ");
    				}

    				fileNo++;
    			}
    		}
    	}
    	return "0";
    }
    
    public boolean isFileDuplicate( String file1, String file2 ) throws Exception
    {	
    	String chkSum1 = getCheckSum( file1 );
    	String chkSum2 = getCheckSum( file2 );
    	
    	if( chkSum1.equals( chkSum2 ) )
    	{
    		System.out.println(" Files are equals ");
    		return true;
    	}
    	
    	return false;
    }
    
    public static String getCheckSum( String str ) throws Exception
    {
    	MessageDigest md =  MessageDigest.getInstance( "MD5" );
    	byte[] bytesOfStr = str.getBytes( "UTF-8" );
    	
    	byte[] mdBytes = md.digest( bytesOfStr );
    	
    	//convert the byte to hex format
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mdBytes.length; i++) 
        {
        	sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

    	return sb.toString();
    }
}