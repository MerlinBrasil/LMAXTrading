package algo.lmax;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lmax.api.FixedPointNumber;





/**
 * Creates lists that hold LMAX-listed instruments description
 * and provides static public accessors to retrieve that data.
 * The lists need to be initialised before they can be accessed
 * for the first time by calling the loadInstruments static method
 * 
 * @author julienmonnier
 * 
 */

public class InstrumentsInfo {
	
	// TODO create method to ensure field name changes
	// in the csv file are captured during load process
	
	

	/**
	 * The primary List.
	 * <p><code>instruinfo</code> holds the following LMAX instruments information (in this order):
	 * <p><ul><li>uiinstruname (see makeInstruList method for details)
	 * <li>Instrument Name <li>LMAX ID <li>LMAX symbol <li>Contract Multiplier
	 * <li>Tick Size <li>Tick Value <li>Effective Date <li>Expiry Date <li>Quoted CCY
	 * </ul>
	 */
	private static List<String[]> instruinfo = null;

	// the following HashMaps are created for fast information retrieval
	// ByName in the list name indicates that the key is the 
	// name of the instrument (the uiinstruname)
	private static HashMap<String,String[]> instrumentsByName = null;
	private static HashMap<String,String[]> instrumentsByID = null;



	// public accessors
	public static class getSymbol {

		/**
		 * returns the LMAX instrument Symbol of the LMAX instrument name
		 * provided in the <code>key</code>
		 * <p>returns <code>null</code> if the key is missing in the list
		 * @param key
		 * @return LMAX instrument Symbol or <code>null</code>
		 */
		public static String byName(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByName.get(key)) == null)) {
				return (String) ((String[]) instrumentsByName.get(key))[3];
				}
			keyDoesNotExist(key);
			return null;
		}

		/**
		 * returns the LMAX instrument Symbol of the LMAX instrument ID
		 * provided in the <code>key</code>
		 * <p>returns <code>null</code> if the key is missing in the list
		 * @param key
		 * @return LMAX instrument Symbol or <code>null</code>
		 */
		public static String byID(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByID.get(key)) == null)) {
				return (String) ((String[]) returnval)[3];
			}
			keyDoesNotExist(key);
			return null;
		}
	}


	public static class getID {
		/**
		 * returns the LMAX instrument ID of the LMAX instrument name
		 * provided in the <code>key</code>
		 * <p>returns <code>null</code> if the key is missing in the list
		 * @param key
		 * @return LMAX instrument ID or <code>null</code>
		 */
		public static Long byName(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByName.get(key)) == null)) {
				return Long.valueOf((String) ((String[]) returnval)[2]);
			}
			keyDoesNotExist(key);
			return null;
		}

	}


	public static class getTickSize {
		/**
		 * returns the LMAX instrument TickSize of the LMAX instrument name
		 * provided in the <code>key</code>
		 * <p>returns <code>null</code> if the key is missing in the list
		 * @param key
		 * @return LMAX instrument TickSize or <code>null</code>
		 */	
		public static FixedPointNumber byName(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByName.get(key)) == null)) {
				return FixedPointNumber.valueOf((String) ((String[]) returnval)[5]);
			}
			keyDoesNotExist(key);
			return null;
		}
		/**
		 * returns the LMAX instrument TickSize of the LMAX instrument ID
		 * provided in the <code>key</code>
		 * <p>returns <code>null</code> if the key is missing in the list
		 * @param key
		 * @return LMAX instrument TickSize or <code>null</code>
		 */	
		public static FixedPointNumber byID(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByID.get(key)) == null)) {
				return FixedPointNumber.valueOf((String) ((String[]) returnval)[5]);
			}
			keyDoesNotExist(key);
			return null;
		}
	}

	private static void keyDoesNotExist(String key) {
		System.out.println("! " + key + " does not exist");
	}
	
	public static void printAll() {
		System.out.println("instrument / instrument short name");
		for (Iterator<String[]> iterator = instruinfo.iterator(); iterator.hasNext();) {
			String[] info = iterator.next();
			System.out.println(info[1] + " " + info[0]);
		}
	}

	

	private static void createHashMaps() {

		instrumentsByName = new HashMap<String, String[]>();
		instrumentsByID = new HashMap<String, String[]>();

		for (Iterator<String[]> iterator = instruinfo.iterator(); iterator.hasNext();) {
			String[] e = iterator.next();
			// uiinstruname is used as key
			instrumentsByName.put(e[0], e);
			// LMAX ID is used as key
			instrumentsByID.put(e[2], e);
		}
	}

	private static void makeInstruList() {

		List<String> instruinfotp = new ArrayList<String>();
		instruinfo = new ArrayList<String[]>();
		
		try {
			BufferedReader f = new BufferedReader(new FileReader("LMAX-Instruments.csv"));
			String content;
			while ((content = f.readLine()) != null) {
				instruinfotp.add(content + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// build main instru info array.  Needs to reflect following
		// structure: uiinstruname, Instru Name, ID, multiplier, tick size, tick value, quoted ccy

		for (Iterator<String> iterator = instruinfotp.iterator(); iterator.hasNext();) {


			String[] i = (iterator.next()).split(",");
			// concatenate Instrument Name whitout "/" for easy user input
			String[] j = i[2].split("/");
			String uiinstruname;
			if (j.length == 2) {
				uiinstruname = j[0]+j[1];
			} else {
				uiinstruname = j[0];
			}
			
			// keep all fields + uiinstruname into final list element
			String[] ifinal = {uiinstruname,i[0],i[1],i[2],i[3],i[4],i[5],i[6],i[7],i[8]};

			instruinfo.add(ifinal);
		}
		// remove headers
		instruinfo.remove(0);
	}
	
	/**
	 * Kicks the process of loading the information contained
	 * in the LMAX-Instruments CVS file into a set of lists
	 * <p>Should be called by the client before calling the lists' getter
	 * methods for the first time.
	 * 
	 * @see {@link #instruinfo}
	 * @see {@link getSymbol}, {@link getTickSize}, {@link getID}
	 */
	public static void loadInstruments() {
		if (instruinfo == null) {
			makeInstruList();
			createHashMaps();
		}
	}
}