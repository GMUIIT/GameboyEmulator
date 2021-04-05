import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;


public class DebuggerTable extends JPanel {
    private boolean DEBUG = false;
 
    public DebuggerTable() {
        super(new GridLayout(1,0));
        
        //Binary read from https://www.codejava.net/java-se/file-io/how-to-read-and-write-binary-files-in-java#:~:text=read()%3A%20reads%20one%20byte,of%20the%20file%20is%20reached.
        
        

        	//This file will need to be adjusted on your computer
            String inputFile = "/Users/nicholaspaschke/GameboyEmulator/src/Tetris.gb";
            
     
            //Memory is used here to dump to the binaries onto the arraylist makes data management much easier.
            ArrayList<String> mem = new ArrayList<String>();
            
            try (
            		//Try is necessary for a fileIOexception 
                    InputStream inputStream = new FileInputStream(inputFile);
                   
                ) {


            	//Bit is used here to read the individual byte that is being read. Yes 1 at a time although this could
            	//Probably be readuced down to a faster speed
                int bit;
                String temp = "";
                int log = 0;
                
                //String temp is used to grab those bytes and collect them into a single string
                while ((bit = inputStream.read()) != -1) {
                	if (bit < 16)
                	{
                		//If the bit is below a certain number it messes with the amount of 0 and 1s in the sequence so I need to make sure to add them in later.
                		temp += "0";
                	}
                	//This is the clearing and keeping track of the bits one after the other and group them
                	//Control words for Gameboys are 8 bytes long and are used for everything so it's important to keep them
                	//In this grouping to read instructions and much more
                	temp += Integer.toHexString(bit);
                	log += 1;
                	if (log == 4)
                	{
                		//Adding the memory 8 bytes at a time
                		mem.add(temp); 
                		temp = "";
                		log = 0;
                	}
                	
                    
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        
    
    
        //You can mess with the column names as well as just abount anything here it isn't bad.
        String[] columnNames = {"Program Counter",
                                "Binary",
                                "OPCode",
                                "Register"};
        //This adjusts to the size of the memory for flexibility as there are several ROMS made for the Gameboy
        Object[][] data = new Object[mem.size()][4];
        
        for (int f = 0; f <mem.size(); f++)
        {		//1st is the index of the array or "program counter"
        		//Followed by the memory dump of the ROM,
        		//The data for OPCodes when it can be decoded will be next and finally the registers when I get arounnd to it
        		data[f][0] = Integer.toHexString(f);
        		
        		data[f][1] = mem.get(f);
        		data[f][2] = 0;
        		data[f][3] = 0;

        }
        
        //This is where the dependancy of Jtable comes into place.
        //Most of this code is taken directly from oracle and has not been touched becuase it works exactly as needed.
        final JTable table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
 
        if (DEBUG) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    printDebugData(table);
                }
            });
        }
 
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
 
    private void printDebugData(JTable table) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();
 
        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("GB ROM debugger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        DebuggerTable newContentPane = new DebuggerTable();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}