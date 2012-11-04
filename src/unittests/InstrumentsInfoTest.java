package unittests;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.junit.Test;

import algo.lmax.my.InstrumentsInfo;

public class InstrumentsInfoTest {

	/**
	 * Tests that the value returned by each of InstrumentsInfo
	 * public accessor is null when the key provided does 
	 * not exist
	 */
	@Test
	public void testReturnNull() {
		
		// General Note: I am using reflection to initialize the static private
		// HashMaps of InstrumentsInfo class.  The other solution was to call
		// InstrumentsInfo.loadInstruments, however this would initialize the 
		// HashMaps with actual values from the CSV file LMAX-Instruments.  
		// I feel using reflection makes the test more targeted and isolate
		// the test from dependencies like the CSV file used
		// by InstrumentsInfo.loadInstruments.
		

		// this will be used to assign an empty testmap to the static maps
		// in InstrumentsInfo
		HashMap<String,String[]> testmap = new HashMap<String,String[]>();
		
		// using reflection to initialize InstrumentsInfo's HashMaps to testmap
		try {
		
		Field f1 = InstrumentsInfo.class.getDeclaredField("instrumentsByName");
		f1.setAccessible(true);
		f1.set(null,testmap);
		
		Field f2 = InstrumentsInfo.class.getDeclaredField("instrumentsByID");
		f2.setAccessible(true);
		f2.set(null,testmap);
		
		} catch (Exception e) {e.printStackTrace();}


		
		
		// values to be tested as missing keys
		String[] e = {"cupcake",null};
		String testkey;
		
		for (int i = 0; i < e.length; i++) {
			testkey = e[i];
			// test by name
			assertEquals(null, InstrumentsInfo.getSymbol.byName(testkey));
			assertEquals(null, InstrumentsInfo.getID.byName(testkey));
			assertEquals(null, InstrumentsInfo.getTickSize.byName(testkey));

			// test by ID
			assertEquals(null, InstrumentsInfo.getSymbol.byID(testkey));
			assertEquals(null, InstrumentsInfo.getTickSize.byID(testkey));
			
		}
	}
}
