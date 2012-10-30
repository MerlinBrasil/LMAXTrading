package algo.lmax.my;


import java.util.HashMap;
import java.util.Map;

import com.lmax.api.FixedPointNumber;

public class InstrumentsInfo {
	

	/**
	 * Couple of important notes:
	 * 1 - This class is a singleton
	 * 2 - The methods that access the HashMaps (the accessors) are all static
	 * 3 - For performance reason the static accessors do not check
	 * if the HashMaps exist. 
	 * 4 - As a result this class must be instantiated at least once
	 * before the static accessors can be used otherwise an
	 * exception will be thrown
	 */
	
	// Main HasMap
	private static HashMap<String,String[]> instrumentsByName = null;
	
	// Stores {InstruID; InstruName} keypairs
	private static HashMap<String,String> instrumentsByID = new HashMap<String, String>();
	
	
	
	public static void loadInstruments() {	
			if (instrumentsByName == null) {
				createHashMaps();			
			}
	}
	
	
	public static class getName {
		
		public static String ByID(String key) {
			return instrumentsByID.get(key);
		}

		public static String ByName(String key) {
			return (String) ((String[]) instrumentsByName.get(key))[1];
		}
	}
	
	public static class getID {
		
		public static Long ByName(String key) {
			return Long.valueOf((String) ((String[]) instrumentsByName.get(key))[0]);
		}
	}	
	
	public static class getTickSize {

		public static FixedPointNumber byName(String key) {
			return FixedPointNumber.valueOf((String) ((String[]) instrumentsByName.get(key))[3]);
		}
	}	

	public static String printAll() {
		return instrumentsByName.toString();
	}
	
	
	private static void createHashMaps() {
		// TODO Auto-generated method stub
		
		createByNameHashMap();
		createByIDHashMap();
		
	
	}
	
	// This HashMap is built off the main HashMap "instrumentsByName"
	// This hashMap is necessary to retrieve CCY Name by ID in constant time
	// in my algo implementation
	private static void createByIDHashMap() {
		// TODO Auto-generated method stub

		for (Map.Entry<String,String[]> entry : instrumentsByName.entrySet()) {

			instrumentsByID.put((String) ((String[]) entry.getValue())[0], (String) ((String[]) entry.getValue())[1]);
			// System.out.println(instrumentsByID.toString());
			
		}
	}

	// This is the main HashMap
	private static void createByNameHashMap() {
		// TODO Could store those in text file instead
		// example of data to feed in Instru
		// EUR/USD,4001,EUR/USD,10000,0.00001,0.1,09/07/2010,,USD
		instrumentsByName = new HashMap<String, String[]>();
		instrumentsByName.put("EURUSD", new String[]{"4001","EURUSD","10000","0.00001","0.1","USD"});
		instrumentsByName.put("USDJPY", new String[]{"4004","USDJPY","10000","0.001","10","JPY"});
		instrumentsByName.put("USDSGD", new String[]{"100535","USDSGD","10000","0.00001","0.1","SGD"});		


		
		
	}
	
	
}
