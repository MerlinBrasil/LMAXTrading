package algo.lmax.my.testcode;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class InstrumentsInfoTest {


	// format expected for HashMap<String,String[]> instrumentsByName
	// is: ({"4001","EURUSD","10000","0.00001","0.1","USD"});
	// expected: uiinstruname, Instru Name, ID, multiplier, tick size, tick value, quoted ccy 
	// Instrument Name,LMAX ID,LMAX symbol ,Contract Multiplier,Tick Size,Tick Value,Effective Date,Expiry Date,Quoted CCY
	// AUD/JPY,4008,AUD/JPY,10000,0.001,10,09/07/2010,,JPY
	// EUR/USD,4001,EUR/USD,10000,0.00001,0.1,09/07/2010,,USD
	
	
	private List<String> instruinfotp = new ArrayList<String>();
	private List<String[]> instruinfo = new ArrayList<String[]>();

	private void makeInstruList() {

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

			String[] ifinal = {uiinstruname,i[0],i[1],i[3],i[4],i[5],i[8]};

			instruinfo.add(ifinal);			
		}
		instruinfo.remove(0);
	}
	
	public static void main(String[] args) {

		new InstrumentsInfoTest().makeInstruList();

	}

}
