package university.ucr.crwler.crawler;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//Add MailTo
public class Parser 
{
	public static void  startCrawling( ) throws Exception 
	{
		//Fetch All the seed URLs from the 'seedURL.txt' file and store in Queue 'linksQueue'
		boolean isCrawlPermit = false;
		ArrayList<String> validAndRectifiedURLsArrayList = new ArrayList<String>();

		
		while(true){
			//Create an ArrayList to store the Valid & rectified URLs, after the processing finished
			//Parse the seed URLs
			
			validAndRectifiedURLsArrayList = new ArrayList<String>();
			
			while( true )
			{
				if( ThreadPool.frontier.isEmpty() )
					break;
				
				String urlToParse ="";
				synchronized(ThreadPool.class){ 
				urlToParse = ThreadPool.frontier.poll();
				}
				ArrayList<String> parsedURLsArrayList = null;
				
				isCrawlPermit  = RobotExclusionUtil.robotsShouldFollow( urlToParse );
				
				if(!isCrawlPermit)
				{
					//System.out.println("Crawling not Allowed for this URL ---->> " + urlToParse);
					continue;
				}
				
				parsedURLsArrayList = parseThisHTML(urlToParse); //'peek()' returns element at head of the Queue
				
				//If parsing the HTML page doesnt fetch any links, skip iteration and continue
				if(parsedURLsArrayList == null || parsedURLsArrayList.size() < 1)
					continue;
				
				//Fetch those URLs which are valid and properly formatted
				ArrayList<String> validURLsArrayList = null;
				validURLsArrayList = returnValidOrMalformedURLs(parsedURLsArrayList, 0);// Pass 0 as parameter to fetch the valid URLs
		
				//Check if the URLs parsed, have any Malformed URLs
				ArrayList<String> malformedURLsArrayList = null;
				malformedURLsArrayList = returnValidOrMalformedURLs(parsedURLsArrayList, 1);// Pass 1 as parameter to fetch the malformed URLs
		
				//If there are any malformed URLs, clean & normalize them
				if( !malformedURLsArrayList.isEmpty() )
				{
					//Encode the URL, to bring it to proper form
					ArrayList<String> rectifiedURLsArrayList = null;
					rectifiedURLsArrayList = returnEncodedURL(malformedURLsArrayList, urlToParse);
					validAndRectifiedURLsArrayList.addAll(rectifiedURLsArrayList); //Store these rectified URLs
				}//End If
		
			validAndRectifiedURLsArrayList.addAll(validURLsArrayList); //Store the valid URLs
			
			}
			
			//Empty the crawler's queue and add all the newly crawled links to the queue
			ThreadPool.levels++;			
			ThreadPool.frontier.addAll(validAndRectifiedURLsArrayList);	
			if(validAndRectifiedURLsArrayList.isEmpty())
				break;
			
			//System.out.println(" ThreadPools Count: : : " + ThreadPool.levels);
		
		}//End Outer While
	}//end startCrawling()

	
	
	//Download the web page to disk, using URL passed as a parameter
	public static String downloadThisFile(String fileURL) throws Exception 
	{	    
 		    URL urlObj 				= new URL( fileURL );
		    HttpURLConnection conn 	= (HttpURLConnection) urlObj.openConnection();
			// Setting time out condition for 3 seconds
			conn.setConnectTimeout( 3000 );
			InputStreamReader isr 	= new InputStreamReader( conn.getInputStream() );
			BufferedReader br 		= new BufferedReader( isr );
			String fileString 		= "";
			FileWriter fw ;
	    	BufferedWriter bw;
			
	    	boolean isEmptyPage = true;
			while( br.ready() )
			{
				String line = br.readLine();
				if( line != "" || line != null )
					isEmptyPage = false;
				fileString += ( line + "\n" );
			}
			
			if( isEmptyPage )
				return "";
			
			Document doc = Jsoup.parseBodyFragment(fileString);
			Element body = doc.body();
			if( body != null )
			{
				String htmlBody = body.toString();
				if( htmlBody =="" || htmlBody.length() < 300 )
					return "";
			}
			else
			{
				return "";
			}
			
			Queue<String> q 	= ThreadPool.frontier;
			String chkSum 		= WebCrawlerMultiThreaded.getCheckSum( fileString );
			String fileName 	= ThreadPool.downloadDirPath + "file" + ThreadPool.cnt + ".html";


			// Check for duplicates pages
			if( !ThreadPool.mapChkSum.containsKey( chkSum ) )
			{
				writeURLToFile( fileURL );
				ThreadPool.mapChkSum.put( chkSum, ThreadPool.cnt );
				
				fileName = ThreadPool.downloadDirPath + "file" + ThreadPool.cnt + ".html";
				ThreadPool.cnt++;
		
				// Open a file to write
			    fw = new FileWriter( fileName );
				bw = new BufferedWriter( fw );
				bw.write( fileString );
				br.close();
				bw.close();
				System.out.println("Downloading ---> " + fileURL);
				ThreadPool.filesDownloaded++;
				return fileName;
			}
			else
			{
				System.out.println( "equal file found ---> " + fileURL);
				return "";
			}

			//return filename;		
	}//end downloadThisFile()

public static ArrayList<String> parseThisHTML(String URLToParse)
{
	Document doc 				=	null;
	Elements elementRobotMeta 	=	null;
	
	try 
	{
		if(!isURLValid(URLToParse))	//If URL is not proper
			return new ArrayList<String>();
		String urlPathOnDisk="";
		
		//Download the html page
		if(ThreadPool.filesDownloaded < ThreadPool.maxFilesDownload)
		{
			urlPathOnDisk = downloadThisFile( URLToParse );
			//writeURLToFile( URLToParse );
		}
		else 
		{
			System.out.println("Exiting ...");
			System.exit(0);
		}
			
		
		System.out.println(" after downloading");
		
		
		if ( urlPathOnDisk.length() == 0 )
			return null;
			 
		File input 			= 	new File( urlPathOnDisk );
		String URLHostName 	= 	new URL( URLToParse ).getHost(); //example.com
		doc 				= 	Jsoup.parse( input, "UTF-8", "http://"+URLHostName );
		
		
			
			//doc = Jsoup.connect(URLToParse).userAgent("Mozilla").get(); //Fetch URL from server directly
			//System.out.println("Parsing: "+URLToParse);
	
			
	 if(doc == null)
		return null;
	 
	 
	 ArrayList<String> parsedLinks = new ArrayList<String>();
	 
	 elementRobotMeta 	= doc.select("meta[name=robots]");
	 if( elementRobotMeta != null )
	 {
		 String ss = elementRobotMeta.attr("CONTENT");
		 if(ss!=null && ss.length()>1){
			String[] parts = ss.split( "," );
		 	ss = parts[1].trim();
		
		 	if( ss.equals( "NOFOLLOW") )
		 	{
			 return parsedLinks;
		 	}
		 }
	 }
	 
	 Elements links = doc.select("a[href]");
	        
	 //Store all the links from Elements object 'links' to our ArrayList 'parsedLinks'
	 for (Element link : links) 
	 {
		 parsedLinks.add(link.attr("abs:href"));
	     //Debugging
	     //System.out.println(link.attr("abs:href"));
	 }
	 //return the ArrayList 'parsedLinks'
	  return parsedLinks;
	 }catch( ArrayIndexOutOfBoundsException e){
		 
	 }
	 catch (IOException e) 
	 {
		 System.out.println("Can not parse URL 1");
	 }
	 catch (InterruptedException e) 
	 {
		
		 System.out.println("Can not parse URL 2");
	 }
	catch (Exception e){
		
	}
	return new ArrayList<String>();
	        
	 
}//End parseThisHTML()

//Check if the URLs ArrayListlist, has any Malformed URLs
public static ArrayList<String> returnValidOrMalformedURLs(ArrayList<String> parsedURLsArrayList,int validOrMalformed) throws InterruptedException
{
	
	if (parsedURLsArrayList==null)
		return new ArrayList<String>();
	
	ArrayList<String> malformedURLsArrayList = new ArrayList<String>() ;
	ArrayList<String> validURLsArrayList = new ArrayList<String>() ;
	
	//Check if any URL is malformed
	for(String URLToCheck: parsedURLsArrayList){
		if(!isURLValid(URLToCheck))
			malformedURLsArrayList.add(URLToCheck);
		else
			validURLsArrayList.add(URLToCheck);
	}
	if(validOrMalformed==0) //We need to return those URLs which are valid
		return validURLsArrayList;
	else //We need to return those URLs which are malformed
		return malformedURLsArrayList;
}//end returnMalformedURLs()

//Check if URL is valid
public static Boolean isURLValid(String URLToCheck) throws InterruptedException{
	//Thread.sleep(100);
	
	//Check if URL is of domain .edu
	String[] schemes = {"http"}; // DEFAULT schemes = "http", "https", "ftp"
	UrlValidator urlValidator = new UrlValidator(schemes);
	if(!URLToCheck.contains(".edu")){
		//System.out.println("URL Not .edu Domain ---> "+ URLToCheck);
		return false;
	}
	else if (urlValidator.isValid(URLToCheck)) {
	   //System.out.println("url Valid: "+ URLToCheck);	
	   return true;
	} else {
	   //System.out.println("url Invalid: "+ URLToCheck);
	   return false;
	}
}//end isURLValid

//Encode the URL, to bring it to proper form
public static ArrayList<String> returnEncodedURL(ArrayList<String> URLsToEncode,String baseURL)
{
	ArrayList<String> rectifiedURLsArrayList = new ArrayList<String>();
	
	for(String URLToProcess : URLsToEncode)
	{
		
		try 
		{
			//'baseURL' will contain the website's main url being crawled
			URL baseUrlObj;
			baseUrlObj = new URL(baseURL);
		
			String baseURLHost = "http://" + baseUrlObj.getHost();
		
			//For Relative links like "/find_people.php" 	
			String resolvedRelativeUrl = "";
			if (URLToProcess.startsWith("/"))
			resolvedRelativeUrl = baseURLHost + URLToProcess;

			else if (URLToProcess.startsWith("http://"))
			resolvedRelativeUrl = URLToProcess;

			else if (URLToProcess.startsWith("www"))
			resolvedRelativeUrl = "http://" + URLToProcess;
		
			else if (URLToProcess.startsWith("https://"))
				continue;
			
			else 
				resolvedRelativeUrl = baseURLHost + "/" + URLToProcess;
	
			//The relative URL is now appended to the base URL and is stored in 'resolvedRelativeUrl' (If it was a case of relative URL)
	
			//For URLs containing spaces/illegal Characters
			if(resolvedRelativeUrl.indexOf('\"')!=-1)
				resolvedRelativeUrl = resolvedRelativeUrl.replace("\"", ""); //Remove Illegal character ' " '
			if(resolvedRelativeUrl.indexOf('\'')!=-1)
				resolvedRelativeUrl = resolvedRelativeUrl.replace("\'", ""); //Remove Illegal character ' ' '
			//Strip bookmarks from the URL
			if(resolvedRelativeUrl.indexOf('#')!=-1){
				int bookMarkLocation = resolvedRelativeUrl.indexOf('#');
				resolvedRelativeUrl = resolvedRelativeUrl.substring(0, bookMarkLocation);
			}
			URL url = new URL(resolvedRelativeUrl);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			uri.normalize(); //piyush nov3
			String encodedURL = uri.toURL().toString(); 
		
			//The URL has now been encoded properly
			rectifiedURLsArrayList.add(encodedURL);
			} 

		    catch (MalformedURLException e) 
		    {
				System.out.println("Malformed URL: "+URLToProcess);
			}			
			catch( URISyntaxException e )
			{
				System.out.println("URI syntax excption" + URLToProcess );
			}
			catch( Exception e )
			{
				System.out.println("\n");
			}
	}//End For
	  
	return rectifiedURLsArrayList;
}//End returnEncodedURL()

public static void testingMethod() throws InterruptedException, UnsupportedEncodingException, URISyntaxException
{
	ArrayList<String> URLsToTest = new ArrayList<String>();
	//URLsToTest.add("/news/GraphenePublicationsIndex");
	
	URLsToTest.add("cf-global-posts.example.com");
	//URLsToTest.add("http://www.pe.com:8080/local-news /riverside-county/riverside/riverside-headlines-index/20120408-riverside-ucr-develops-sensory-detection-for-smartphones.ece?ssimg=532988#ssStory533");
	
	boolean checkResult = isURLValid("cf-global-posts.example.com");
	
	ArrayList<String> validURLsArrayList = null;
	validURLsArrayList = returnValidOrMalformedURLs(URLsToTest,0);// Pass 0 as parameter to fetch the malformed URLs
	
	//Check if the URLs parsed, have any Malformed URLs
	ArrayList<String> malformedURLsArrayList = null;
	malformedURLsArrayList = returnValidOrMalformedURLs(URLsToTest,1);// Pass 1 as parameter to fetch the malformed URLs
	
	//Encode the URL, to bring it to proper form
	ArrayList<String> encodedURLsArrayList = null;
	encodedURLsArrayList = returnEncodedURL(malformedURLsArrayList,"http://www.google.com");
	
}//End testingMethod()

//Check If a filename is valid
public static boolean isFilenameValid(String file) {
	  File f = new File(file);
	  try {
	    f.getCanonicalPath();
	    return true;
	  } catch (IOException e) {
	    return false;
	  }
	}

//Piyush nov3
public static void writeURLToFile( String URLToWrite ){
	String filePath = ThreadPool.downloadDirPath +"//downloadedURLs.txt";
	FileWriter fw ;
	BufferedWriter bw;
    try {
		fw = new FileWriter( filePath , true );
		bw = new BufferedWriter( fw );
		bw.append( URLToWrite + "\n");
		bw.close();
		fw.close();

	} catch (IOException e) {
		e.printStackTrace();
	}


	
}//End function writeURLToFile()

}//End Class