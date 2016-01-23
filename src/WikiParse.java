import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class WikiParse 
{
	public static HashMap<String, HashMap<String , HashMap<Integer, Integer> > > iIndex =
																			new HashMap<String, HashMap<String , HashMap<Integer, Integer> > >();
	
	public static void main(String[] args)
	{	
		
		String inputFileName = null, outputIndexFileName = null;
		if(args.length == 2)
		{
			inputFileName = args[0].trim();
			outputIndexFileName = args[1].trim();
		}
		else
		{
			System.out.println("Invalid number of command line arguments...");
			System.exit(0);
		}		
		
		//Start the timer
		long startTime = System.currentTimeMillis();		
		try
		{
			parseXML(inputFileName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		
		FileWriter f=null;
		try 
		{
			f = new FileWriter(outputIndexFileName);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated castch block
			e.printStackTrace();
		}
		java.util.Iterator<Entry<String, HashMap<String, HashMap<Integer, Integer>>>> it = WikiParse.iIndex.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Map.Entry pair = (Map.Entry)it.next();
	        try 
	        {
				f.write(pair.getKey() + " = " + pair.getValue()+"\n");
			}
	        catch (IOException e) 
	        {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //System.out.println(pair.getKey() + " = " + pair.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }	
		//End the timer
	    long endTime = System.currentTimeMillis();;
	    System.out.println("\nExecution Time : " + (float)(endTime - startTime)/1000 + " sec");
	}
	
	public static void parseXML(String rawInputFile)
	{	
		//String fileName = "src/inputXML-smalltest.xml";
		//String fileName = "src/inputXML.xml";
		String fileName = rawInputFile;
		try
		{
			//Obtain and configure the SAX based parser
			SAXParserFactory spf = SAXParserFactory.newInstance();			
			//Obtain object for SAX Parser
			SAXParser saxParser;
			saxParser = spf.newSAXParser();			
			UserHandler userHandler = new UserHandler();			
		    saxParser.parse(fileName, userHandler);		    
		}
		catch(Exception e)
		{			
			e.printStackTrace();
		}
	}
}

class UserHandler extends DefaultHandler
{
		static int pageCount = 0;
		public static String pageTitleText = "";
		boolean ID = false;
		boolean pageTitle = false;
		boolean text = false;
		boolean isTaken = false;
		StringBuilder currentDoc = new StringBuilder();
		String pID = "";
		Tokenize t = new Tokenize();
		
		//This method is called every time when parser gets the opening "<" tag
		//identifies which  tag is open at a time by  assigning  an open flag
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			switch(qName.toLowerCase())
			{
				case "id":
					ID = true;
					break;
				case "title":
					pageTitle = true;
					break;
				case "text":
					text = true;
					break;
				default:
					break;
			}
		}
		
		//This method is called every time when the parser get the closing tag ">"
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			//Call the method of the tokenization
			// Switch method is more efficient than if - else if statements coz compiler
			// created the jump table for the instructions
			if(qName.toLowerCase().equals("page"))
			{
				try 
				{
					t.tokenize(pID, docData());
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				ID = false;
				pageTitle = false;
				text = false;
				isTaken = false;
				pageTitleText = "";
				pID = "";
				currentDoc.setLength(0);
				if(++pageCount == 100)
				{
					//	call the write procedure and clear the page count 
					// 	this thing we will take care because in case of large data dump,
					// 	our all variables will get exhausted and also it is very expensive to bring
					//	the file in memory every time, So till then just goon building the index
					//  Hence after this write to the file
					pageCount = 0;
				}
			}
			else if(qName.toLowerCase().equals("text"))
			{
				text = false;
			}
		}
		
		//print the data between the tags "<" & ">"
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			if(ID)
			{
				if(!isTaken)
				{
					pID = new String(ch, start, length);
					isTaken = true;
				}
				/*	What if in case any user searches for the text using the revision, hence add 
				*	extracted data also, Anyway I gonna remove numeric data, coz there is very rare chances of
				* 	sear
				*/
				//	currentDoc.append(" " + new String(ch, start, length));
				ID = false;
			}
			else if(pageTitle)
			{
				//currentDoc.append(" " + new String(ch, start, length));
				pageTitleText = new String(ch, start, length);
				pageTitle = false;
			}
			else if(text)
			{
				currentDoc.append(" " + new String(ch, start, length));
			}
			/*else
			{
				currentDoc.append(" " + new String(ch, start, length));
			}*/	
		}
		
		//This method will give the current page text which we can process later
		public String docData()
		{
			return currentDoc.toString();
		}
}
