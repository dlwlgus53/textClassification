package BugSeverityPrediction ;

import java.util.* ;
import java.util.Map.Entry;
import java.io.* ;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.configuration.* ;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class VectorizationBag
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

	public static HashMap<Integer, List<String>> loadDescription(String fname, HashSet<Integer> reportIDs)
	{
		HashMap<Integer, List<String>> descriptions = new HashMap<Integer, List<String>>() ;

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
				List<String> tokens = new ArrayList<String>();
				String description = report.getElementsByTagName("summary").item(0).getTextContent().toLowerCase();
				StringTokenizer token = new StringTokenizer(description,"-(){}[]/|.?!:;, <>?|\"`");

				while(token.hasMoreTokens()){
					String temp = token.nextToken();
					tokens.add(temp);
			}	
				descriptions.put(reportID, tokens) ;
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return descriptions ;
	}

	public static int isThere(TreeMap<Integer,List<String>> bag, List<String>list){
		int size = bag.size();
		List<String> tokens = new ArrayList();
		for(int i =0; i<size; i++){
			tokens = bag.get(i);
			if(tokens.get(0).equals(list.get(0))&&tokens.get(1).equals(list.get(1)))
				return i;
		}
		

		return -1;
	}

	

	public static TreeMap<Integer, List<String>> buildDictionary(HashMap<Integer, List<String>> descriptions, int threshold) {
		TreeMap<Integer,List<String>> dictionary = new TreeMap<Integer, List<String>>() ;
		TreeMap<Integer, List<String>> bag = new TreeMap<Integer, List<String>>();
		TreeMap<Integer, Integer> frequency = new TreeMap<Integer, Integer>();

	try{ 
		
		for (Map.Entry<Integer, List<String>> entry : descriptions.entrySet()) {
			List<String> tokens = entry.getValue();//1번 설명 가져오기
			Integer size  = tokens.size();
			for(int i=0;i<size-2;i++){	
				List<String> pair = new ArrayList<String>(2);
				pair.add(tokens.get(i));
				pair.add(tokens.get(i+1));
				int index = isThere(bag,pair);//-1:there is no pair in bag
				if(index!=-1){
					Integer num;
					num = frequency.get(index);
					num++;
					frequency.replace(index, num);
				}
				else{
					bag.put(bag.size(),pair);
					frequency.put(frequency.size(),1);
				}
			}
		}
		
		PrintWriter freq = new PrintWriter(new FileWriter("freqBag"));
		for(Map.Entry<Integer, Integer> entry : frequency.entrySet()){
				int index = entry.getKey();
				freq.print(bag.get(index));
				freq.print(",");
				freq.println(frequency.get(index));
		}	

		freq.close();
		

		Integer nWords=0;
		PrintWriter diction = new PrintWriter(new FileWriter("dictionaryBag"));

		for(Map.Entry<Integer, Integer> entry : frequency.entrySet()){
			if( threshold < entry.getValue()){
				int index = entry.getKey();
				dictionary.put(nWords,bag.get(index));

				diction.print(nWords);
				diction.print(",");
				diction.print(bag.get(index));
				diction.print(",");
				diction.println(frequency.get(index));
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

	public static double [] getVector(TreeMap<Integer,List<String>> bag, List<String> description) {
		//description = description.toLowerCase() ;
		double [] v = new double[bag.size()] ;
		// TO-DO: implement here
		//토커나이즈
		//단어하나
		//몇번 단어인지 알고 - >1로 바꾸기
		
		Integer size = description.size();
			for(int i=0;i<size-2;i++){	
				List<String> pair = new ArrayList<String>(2);
				pair.add(description.get(i));
				pair.add(description.get(i+1));//자 일단 들고왔다..
				int index = isThere(bag,pair);
				if(index!=-1)
					v[index]=1;
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
		HashMap<Integer, List<String>> 	descriptions ;
		TreeMap<Integer,List<String>> 	dictionary ;
		

		PropertiesConfiguration config = loadConfig("config.properties") ;

		reportIDs = loadTargetReportIDs(config.getString("bagdata.dir"), config.getString("data.module")) ;
		labels = loadSeverityLabel(config.getString("bagdata.dir") , reportIDs) ; //severity
		descriptions = loadDescription(config.getString("bagdata.dir") , reportIDs) ; //error message

		dictionary = buildDictionary(descriptions, config.getInt("bagdicionary.minEvidences")) ;//build dictionary according 
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
			PrintWriter out = new PrintWriter(new FileWriter(config.getString("arff.bagfilename"))) ;
			//PrintWriter diction = new PrintWriter(new FileWriter("dictionary"));
			out.println("@relation foodreport") ;
			for (int i = 0 ; i < dictionary.size() ; i++){
				out.println("@attribute c" + i +" numeric") ;
			}

			/*Iterator<String> k = dictionKey.iterator(); 
            while(k.hasNext()){                           
				 diction.print(k.next(),dictionary.get(k.next));
			}
			diction.close();*/

		

			out.println("@attribute l {good, bad}") ;
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