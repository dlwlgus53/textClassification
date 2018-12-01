package BugSeverityPrediction ;

import java.util.* ;
import java.io.* ;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.configuration.* ;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Vectorization
{
	public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;

	public static HashSet<Integer> loadTargetReportIDs(String fname, String keyword)
	{
		HashSet<Integer> ids = new HashSet<Integer>() ;

		try {
			DocumentBuilder dBuilder = factory.newDocumentBuilder() ; //new builder
			Document doc = dBuilder.parse(new File(fname)) ;
			doc.getDocumentElement().normalize() ;

			NodeList reports = doc.getElementsByTagName("report");

			for (int i = 0; i < reports.getLength(); i++) {
				Element report = (Element) reports.item(i) ;
				int reportID = Integer.parseInt(report.getAttribute("id")) ;

				//NodeList updates = report.getElementsByTagName("update") ;
				//Node update = updates.item(updates.getLength() - 1) ;

				//NodeList attr = ((Element)report.getElementsByTagName("summary")) ;

				//if (attr.item(0).getTextContent().indexOf("Layout") != -1) 
				ids.add(reportID) ;
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1) ;
		}
		return ids ;
	}

	public static HashMap<Integer, Boolean> loadSeverityLabel(String fname, HashSet<Integer> reportIDs)
	{
		HashMap<Integer, Boolean> labels = new HashMap<Integer, Boolean>() ;

		try {
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File(fname));
			doc.getDocumentElement().normalize();

			NodeList reports = doc.getElementsByTagName("report");

			for (int i = 0; i < reports.getLength(); i++) {
				Element report = (Element) reports.item(i);
				int reportID = Integer.parseInt(report.getAttribute("id")) ;

				if (reportIDs.contains(new Integer(reportID)) == false)
					continue ;

				//NodeList updates = report.getElementsByTagName("update") ;
				//Element lastUpdate = (Element) updates.item(updates.getLength() - 1) ;

				Integer score = Integer.parseInt(report.getElementsByTagName("score").item(0).getTextContent()) ;
					
				switch (score) {
					case 5:
					case 4:
						labels.put(reportID, true) ;
						break ;
					default:
						labels.put(reportID, false) ;
						break ;
				}
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return labels ;
	}

	public static HashMap<Integer, String> loadDescription(String fname, HashSet<Integer> reportIDs)
	{
		HashMap<Integer, String> descriptions = new HashMap<Integer, String>() ;

		try {
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File(fname));
			doc.getDocumentElement().normalize();

			NodeList reports = doc.getElementsByTagName("report");

			for (int i = 0; i < reports.getLength(); i++) {
				Element report = (Element) reports.item(i);
				int reportID = Integer.parseInt(report.getAttribute("id")) ;

				if (reportIDs.contains(new Integer(reportID)) == false)
					continue ;

				//NodeList updates = report.getElementsByTagName("update") ;
				//Element lastUpdate = (Element) updates.item(updates.getLength() - 1) ;
				String description = report.getElementsByTagName("summary").item(0).getTextContent() ;
				
				descriptions.put(reportID, description) ;
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return descriptions ;
	}

	public static TreeMap<String, Integer> buildDictionary(HashMap<Integer, String> descriptions, int threshold) {
		TreeMap<String, Integer> dictionary = new TreeMap<String, Integer>() ;
		TreeMap<String, Integer> frequency = new TreeMap<String, Integer>() ;
		List edictionary = new ArrayList();

		


		
		/*
			word|word frequency
		*/

		// TO-DO: implement here
		/*for(Map.Entry<String, Integer> pair : frequency.entrySet()){
			String str = pair.getKey();
				if(frequency.get(str)>threshold){
					dictionary.put(str,nWords);
					nWords++;
		}
	}*/

	try{ 
		BufferedReader eread = new BufferedReader(new FileReader("edictionary"));
		String s;
  
		while ((s = eread.readLine()) != null) {
		  edictionary.add(s);
		}

		eread.close();
	


		for(Map.Entry<Integer, String> pair: descriptions.entrySet()){
			String str = pair.getValue().toLowerCase();
			StringTokenizer token = new StringTokenizer(str,"-(){}[]/|.?!:;, <>?|\"`");
			while(token.hasMoreTokens()){
				String temp = token.nextToken();
				if(edictionary.contains(temp))
					continue;
				if(frequency.containsKey(temp)){
					Integer num;
					num = frequency.get(temp);
					num++;
					frequency.replace(temp, num);
				}
				else{
					frequency.put(temp,1);
				}
			}
		}

		Integer nWords=0;

		PrintWriter diction = new PrintWriter(new FileWriter("dictionary"));
		for(Map.Entry<String, Integer> pair2 : frequency.entrySet()){
			if( threshold < pair2.getValue()){
				dictionary.put(pair2.getKey(),nWords);
				diction.print(pair2.getKey());
				diction.print(",");
				diction.println(pair2.getValue());
				nWords++;
			}
		}	
	diction.close();
	}
	catch (IOException e) {
		System.err.println(e) ;
		System.exit(1) ;
	}

	

		return dictionary ;
	}

	public static double [] getVector(TreeMap<String, Integer> dictionary, String description) {
		description = description.toLowerCase() ;
		double [] v = new double[dictionary.keySet().size()] ;
		// TO-DO: implement here
		//토커나이즈
		//단어하나
		//몇번 단어인지 알고 - >1로 바꾸기

			StringTokenizer token = new StringTokenizer(description,"-(){}[]/|.?!:;, <>?|\"`");

			while(token.hasMoreTokens()){
				String temp = token.nextToken();
				for(Map.Entry<String, Integer> pair:dictionary.entrySet()){
				if(temp.equals(pair.getKey())){
					v[pair.getValue()]=1;
				}
			}
		}
		return v ;
	}

	public static PropertiesConfiguration loadConfig(String fname) 
	{
		PropertiesConfiguration config = null ;
		try {
			config = new PropertiesConfiguration(fname) ;
		}
		catch (ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
		return config ;
	}


	public static void main(String[] args)
	{
		HashSet<Integer> 			reportIDs ;
		HashMap<Integer, Boolean>	labels ;
		HashMap<Integer, String> 	descriptions ;
		TreeMap<String, Integer> 	dictionary ;
		

		PropertiesConfiguration config = loadConfig("config.properties") ;

		reportIDs = loadTargetReportIDs(config.getString("data.dir"), config.getString("data.module")) ;
		labels = loadSeverityLabel(config.getString("data.dir") , reportIDs) ; //severity
		descriptions = loadDescription(config.getString("data.dir") , reportIDs) ; //error message

		dictionary = buildDictionary(descriptions, config.getInt("dictionary.minEvidences")) ;//build dictionary according 
		//the description 

		/* description[0]= " System crashes"
			description[1]=" System gets slow
			description[2]="Null pointer dereference occurs
			
			Dictionary should be something like
			
			System : 0
			crashes :1
			...
			occurs : 9
			
			then Vector represention should be something like 
			v[0] = {1,1,.....,0}*/ 

		// Print out arff file
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(config.getString("arff.filename"))) ;
			//PrintWriter diction = new PrintWriter(new FileWriter("dictionary"));
			Set<String> dictionKey = dictionary.keySet();
			out.println("@relation foodreport") ;
			for (int i = 0 ; i < dictionary.keySet().size() ; i++){
				out.println("@attribute c" + i +" numeric") ;
			}

			/*Iterator<String> k = dictionKey.iterator(); 
            while(k.hasNext()){                           
				 diction.print(k.next(),dictionary.get(k.next));
			}
			diction.close();*/

		

			out.println("@attribute l {bad, good}") ;
			out.println("@data") ;
			for (Iterator<Integer> i = reportIDs.iterator() ; i.hasNext() ; ) {
				Integer reportID = i.next().intValue() ;


				double [] v = getVector(dictionary, descriptions.get(reportID)) ;
				for (int j = 0 ; j < v.length ; j++)
					out.print(v[j] + ",") ;

				out.println(labels.get(reportID) ? "good" : "bad") ;
		}
			out.close() ;
		}
		catch (IOException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}
}