package init;

import java.util.HashMap;

public class Configuration {
	private static HashMap<String,String> contacts=new HashMap<String, String>();
	private static HashMap<String,String> calcMap=new HashMap<String, String>();
	private Configuration(){}
	public static HashMap<String,String> getContactHashMap(){
		contacts.put("rahul", "rdshanragat@gmail.com");
		contacts.put("jyotish", "jyoteshrc@gmail.com");
		contacts.put("rahul", "rdsharnagat@gmail.com");
		contacts.put("anand", "ananth1987@gmail.com");
		contacts.put("raj", "raj@cse.iitb.ac.in");
		contacts.put("swapnil", "dreamblue2@gmail.com");
		contacts.put("anuj", "anuj@gmail.com");
		contacts.put("dan","dan@cse.iitb.ac.in");
		contacts.put("ankur", "ankur@cse.iitb.ac.in");
		contacts.put("shah","subhashish@cse.iitb.ac.in");
		contacts.put("muthuswamy","mchellia@yahoo-inc.co.in");
		contacts.put("prakash", "prakash@cse.iitb.ac.in");
		
		return contacts;
	}
	public static HashMap<String,String> calcMappings(){
		calcMap.put("logarithm", "l");
		calcMap.put("into", "*");
		calcMap.put("divided by", "/");
		calcMap.put("multiplied by", "*");
		//calcMap.put("divided by", "/");
		//calcMap.put("divide","/");
		calcMap.put("point", ".");
		//calcMap.put("points", ".");
		return calcMap;
	}
}

