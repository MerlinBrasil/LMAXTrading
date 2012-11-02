package algo.lmax.my;

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
 * @author julienmonnier
 * Creates lists that hold LMAX-listed instruments description
 * and provides static public accessors to retrieve that data.
 * The lists need to be initialised before they can be accessed
 * for the first time by calling the loadInstruments static method
 * 
 * TODO create method to ensure field name changes
 * in the csv file are captured during load process
 */
public class InstrumentsInfo {

	// Main List
	// instruinfo holds the following LMAX instruments information (in this order):
	// uiinstruname (see makeInstruList method for details), Instrument Name,LMAX ID,LMAX symbol ,Contract Multiplier,
	// Tick Size,Tick Value, Effective Date,Expiry Date,Quoted CCY
	private static List<String[]> instruinfo = null;

	// the following HashMaps are created for fast information retrieval
	// ByName in the list name indicates that the key is the 
	// name of the instrument (the uiinstruname)
	private static HashMap<String,String[]> instrumentsByName = null;
	private static HashMap<String,String[]> instrumentsByID = null;



	// public accessors
	public static class getSymbol {

		public static String byName(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByName.get(key)) == null)) {
				return (String) ((String[]) instrumentsByName.get(key))[3];
				}
			return null;
		}
	
		public static String byID(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByID.get(key)) == null)) {
				return (String) ((String[]) returnval)[3];
			}
			return null;
		}
	}

	public static class getID {
		public static Long byName(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByName.get(key)) == null)) {
				return Long.valueOf((String) ((String[]) returnval)[2]);
			}
			return null;
		}
	}

	
	public static class getTickSize {
		public static FixedPointNumber byName(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByName.get(key)) == null)) {
				return FixedPointNumber.valueOf((String) ((String[]) returnval)[5]);
			}
			return null;
		}
		
		public static FixedPointNumber byID(String key) {
			String[] returnval;
			if (!((returnval = instrumentsByID.get(key)) == null)) {
				return FixedPointNumber.valueOf((String) ((String[]) returnval)[5]);
			}
			return null;
		}
	}

	public static void printAll() {
		// TODO implements
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
	
	public static void loadInstruments() {
		if (instruinfo == null) {
			makeInstruList();
			createHashMaps();
		}
	}
}