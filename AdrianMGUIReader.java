import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


public class AdrianMGUIReader {
    
    //Fields of the class
    public static JFrame mainFrame;
    public static ButtonGroup group;
    public static boolean groupStarted = false;
    public static boolean endWindow = false;

	//Main method that runs the application
	public static void main(String[] args) throws Exception {
		//Create 
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Enter the textfile to be converted to a GUI: ");
		






		String filename = keyboard.nextLine();
		File file = new File(filename);

		//Create Scanner to read file
		Scanner scanFile = new Scanner(file);

		//Read lines to create components as needed calling the recursive
		//parses function
		mainFrame = (JFrame)parseGuiComponent(scanFile);
		while(scanFile.hasNextLine()) {
		    String line = scanFile.nextLine();
		    if(!line.isBlank()) {
		        throw new Exception("Invalid Grammar entered. CANNOT BE PARSED!!!");
		    }
		}
		//If all valid, we make it visible!
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	//Recursive function that parses the next line on the file scanner
	public static Component parseGuiComponent(Scanner scanFile) throws Exception {
		//Declaration of variables
		String name, rem;
		String[] quotes;
		int width, height;

		//Try-catch to read and parse line
		try {
			//Read next line and check for component to create
			String line = scanFile.nextLine();
			String[] parts = line.split(" ");
			switch (parts[0]) {
			case "Window":
				//Read name and dimensions
				quotes = line.split("\"");
				rem = quotes[2].trim();
				parts = rem.split(" ");
				name = quotes[1];
				width = Integer.parseInt(parts[0].trim().
						substring(1,parts[0].length()-1)); //trim and remove "," and "("
				height = Integer.parseInt(parts[1].trim().
						substring(0,parts[1].length()-1)); //trim and remove ")"
				//Read layout to use
				if(parts[2].equals("Layout")) {
					line = "";
					for(int i = 3; i < parts.length; i++) {
						line += parts[i]+" ";
					}
					//Construct JFrame
					JFrame frame = new JFrame(name);
					frame.setSize(width, height);
					parseLayout(line.trim(), frame);
					//Add components to frame
					while(true) {
						Component comp = parseGuiComponent(scanFile);
						//If no component, it means we got to the end line
						if(comp == null) {
						    if(endWindow) {
						        if(group != null) {
    						        //The end of the window was found but it can't
    						        //be when there's an open group, so error
    						        throw new Exception();
						        } else {
						            //We hit the end of window, so break
						            break;
						        }
						    } else {
    						    if (group != null && !groupStarted) {
    						        //If comp is null and group is not null,
    						        //it means we start a new group, just continue
    						        groupStarted = true;
    						        continue;
    						    } else if(group != null && groupStarted) {
    						        //If startGroup is true, this means the group
    						        //was started already and now it has to end
    						        groupStarted = false;
    						        group = null;
    						        continue;
    						    } else {
    						        //No end window but there should be one, so
    						        //there's an error
    						        throw new Exception();
    						    }
					    	}
						}
						//If not, we add it
						frame.add(comp);
					}
					//Finally, we return the frame created
					return frame;
				} else {
					throw new Exception();
				}
			case "Textfield":
				if(!line.endsWith(";")) {
					throw new Exception();
				}
				//Get name of field
				width = Integer.parseInt(line.substring(10, line.length()-1));
				//Create text field and return it
				JTextField tf = new JTextField(width);
				return tf;
				
				
			case "Button":
				if(!line.endsWith(";")) {
					throw new Exception();
				}
				name = line.substring(8, line.length()-2);
				JButton button = new JButton(name);
				return button;


			case "Panel":
			    //Read layout
				parts = line.split(" ");
				//Read layout to use
				if(parts[1].equals("Layout")) {
					line = "";
					for(int i = 2; i < parts.length; i++) {
						line += parts[i]+" ";
					}
					//Construct JPanel
					JPanel panel = new JPanel();
					parseLayout(line.trim(), panel);
					//Add components to panel
					while(true) {
						Component comp = parseGuiComponent(scanFile);
						//If no component, it means we got to the end line
						if(comp == null) {
						    if(endWindow) {
						        //The end of the window was found but it can't
						        //be when there's an open panel, so error
						        throw new Exception();
						    } else if(group != null && !groupStarted) {
						        //If comp is null and group is not null,
						        //it means we start a new group, just continue
						        groupStarted = true;
						        continue;
						    } else if(group != null && groupStarted) {
						        //If startGroup is true, this means the group
						        //was started already and now it has to end
						        groupStarted = false;
						        group = null;
						        continue;
						    } else {
						        //We hit the end of panel, so break
						        break;
						    }
						}
						//If not, we add it
						panel.add(comp);
					}
					//Finally, we return the panel created
					return panel;
				} else {
					throw new Exception();
				}
				
			case "Label":
				if(!line.endsWith(";")) {
					throw new Exception();
				}
				//Get name of field
				name = line.substring(7, line.length()-2);
				//Create label field and return it
				JLabel label = new JLabel(name);
				return label;


			case"Radio":
				if(!line.endsWith(";")) {
					throw new Exception();
				}
				name = line.substring(7, line.length()-2);
				//Create radio button, add it to group and return it
				JRadioButton jrb = new JRadioButton(name);
				group.add(jrb);
				return jrb;


			case "Group:":
			    //Init a radio button group
			    group = new ButtonGroup();
			    return null;

			case "End;":
				return null;
				
			case "End.":
			    endWindow = true;
			    return null;
			default:
				throw new Exception();
			}
		} catch(Exception e) {
			throw new Exception("Invalid Grammar entered. CANNOT BE PARSED!!!");
		}
	}

	//Helper method that parses the layout needed
	public static void parseLayout(String line, Container comp) throws Exception {
		//Check if line ends with colon. If not, throw an exception
		if(!line.endsWith(":")) {
			throw new Exception();
		}
		//Check on possible layouts
		if(line.startsWith("Flow")) {
			comp.setLayout(new FlowLayout());
		} else if(line.startsWith("Grid")) {
			//Get int values separately
			line = line.substring(5,line.length()-2);
			String[] nums = line.split(",");
			int rows = Integer.parseInt(nums[0].trim());
			int cols = Integer.parseInt(nums[1].trim());
			int hgap = 0, vgap = 0;
			if(nums.length > 2) {
				hgap = Integer.parseInt(nums[2].trim());
				vgap = Integer.parseInt(nums[3].trim());
			}
			comp.setLayout(new GridLayout(rows, cols, hgap, vgap));
		}
		else {
			throw new Exception();
		}
	}
}
