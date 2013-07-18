package algo.lmax;



import javax.swing.*;
import javax.swing.text.DefaultCaret;





/**
 * This class will be used to output events comming from the exchange
 * in a frame.
 * <p>Currently those event are output on stdout
 * @author julienmonnier
 */
public class TextAreaFrame {
 
   JTextArea textArea = null;
 
   public TextAreaFrame() {
 
      JFrame frame = new JFrame();
      frame.setSize( 1400 , 200 );
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 
      textArea = new JTextArea( 10 , 20 );
      textArea.setEditable( false );
      textArea.setLineWrap(true);
      
      textArea.setWrapStyleWord(true);
      
      JScrollPane panel = new JScrollPane(textArea);


      
      //
 
//      JTextPane panel = new JTextPane();
//      panel.add( textArea );
 
      frame.getContentPane().add( panel );
      
      // Allows auto scrolling when text reaches bottom of text area
      // (as per SO thread http://stackoverflow.com/questions/1627028/how-to-set-auto-scrolling-of-jtextarea-in-java-gui)
      
//      DefaultCaret caret = (DefaultCaret)textArea.getCaret();
//      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);      
 
      frame.setVisible(true);
 
   }

   public static void main(String[] args) throws InterruptedException {
	   TextAreaFrame taf = new TextAreaFrame();
	   
	   String returnchar = "\n";

	   taf.textArea.append( ">> Starting Hello World" );
	   
	   
	   for (int i = 0; i < 100; i++) {
		   int secs = (int)(Math.random() * 200);
		   Thread.sleep(secs);
		   
		   
		   taf.textArea.append( returnchar + i + " waited >>>  " + secs + " milli seconds");		
		   taf.textArea.setCaretPosition(taf.textArea.getDocument().getLength());
	   }

	   Thread.sleep(3000);
	   System.exit(0);
}
   
}



