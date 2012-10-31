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

public class InstrumentsInfo {

	/**
	 * Creates lists that contain LMAX
	 * intruments information and provides
	 * static public accesors to retrieve that
	 * information
	 */

	// Main List
	// structure is as follow:
	// uiinstruname, Instru Name, LMAX Symbol, ID, multiplier, tick size, tick value, quoted ccy
	private static List<String[]> instruinfo = null;

	private static HashMap<String,String[]> instrumentsByName = null;
	private static HashMap<String,String[]> instrumentsByID = null;



	public static class getSymbol {
		public static String byID(String key) {
			return (String) ((String[]) instrumentsByID.get(key))[2];
		}
		public static String byName(String key) {
			return (String) ((String[]) instrumentsByName.get(key))[2];
		}
	}

	public static class getID {
		public static Long byName(String key) {
			return Long.valueOf((String) ((String[]) instrumentsByName.get(key))[2]);
		}
	}	

	public static class getTickSize {
		public static FixedPointNumber byName(String key) {
			return FixedPointNumber.valueOf((String) ((String[]) instrumentsByName.get(key))[5]);
		}
		public static FixedPointNumber byID(String key) {
			return FixedPointNumber.valueOf((String) ((String[]) instrumentsByID.get(key))[5]);
		}
	}

	public static void printAll() {
		// TODO implements
	}

	
	// hashmaps that contains instruments names and ID
	// are created to allow quick retrival of
	// instruments details during live trading
	private static void createHashMaps() {

		instrumentsByName = new HashMap<String, String[]>();
		instrumentsByID = new HashMap<String, String[]>();

		for (Iterator<String[]> iterator = instruinfo.iterator(); iterator.hasNext();) {
			String[] type = iterator.next();
			instrumentsByName.put(type[0], type);
			instrumentsByID.put(type[2], type);
		}
	}

	private static void makeInstruList() {

		instruinfo = new ArrayList<String[]>();
		List<String> instruinfotp = new ArrayList<String>();
		
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
			// concatenate instru name whitout "/" for easy user input
			String[] j = i[2].split("/");
			String uiinstruname;
			if (j.length == 2) {
				uiinstruname = j[0]+j[1];
			} else {
				uiinstruname = j[0];
			}

			String[] ifinal = {uiinstruname,i[0],i[1],i[2],i[3],i[4],i[5],i[8]};

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