import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import jdk.internal.org.xml.sax.InputSource;

public class DiaFileParser extends FileParserInput{
	
	private ArrayList alTables, alFields, alConnectors;
	private DatabaseTable[] tables;
    private DatabaseField[] fields;	
    private EdgeConnector[] connectors;
	
	public static final String EDGE_ID 	= "EDGE Diagram File"; //first line of .edg files should be this
	public static final String SAVE_ID 	= "EdgeConvert Save File"; //first line of save files should be this
	public static final String DELIM 	= "|";

	public DiaFileParser(File constructorFile){
		alTables 		= new ArrayList();
		alFields 		= new ArrayList();
		alConnectors 	= new ArrayList();
		this.openFile(constructorFile);
	}

	@Override
	public DatabaseTable[] getTables() {
		return tables;
	}

	@Override
	public DatabaseField[] getFields() {
		return fields;
	}		
	
	@Override
	public void parseFile(BufferedReader br) throws IOException {
		
	}
	public void parseFile(InputStream is) throws IOException {
		try{
			int tableNum = 0;
			int fieldNum = 0;
			int connectorNum = 0;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
	        Document doc = builder.parse(is);
	    	

	        NodeList nl = doc.getElementsByTagName("dia:object");
	        Node n;

	        for (int i=0; i<nl.getLength(); i++)
	        {
	           n = nl.item(i);
	           Element e1 = (Element)n;
	           if(e1.getAttribute("type").equals("Database - Table"))
	           {
				   NodeList tl = e1.getChildNodes();
				   Node t;

		    	   DatabaseTable table = null;
				   for (int j=0; j<tl.getLength(); j++)
				   {
				       t = tl.item(j);

				       if(t.getNodeType() == Node.ELEMENT_NODE)
				       {
					       Element e2 = (Element)t;
						   if(e2.getAttribute("name").equals("name")){
			                   table = new DatabaseTable(tableNum,e2.getTextContent().trim().replace("#",""));
			                   alTables.add(table);
						   }  
						   if(e2.getAttribute("name").equals("attributes")){
							   NodeList fl = e2.getElementsByTagName("dia:composite");
							   Node f;
							   DatabaseField field = null;
							   for(int k = 0; k<fl.getLength();k++){
								   f = fl.item(k);
								   NodeList al = ((Element)f).getElementsByTagName("dia:attribute");
								   Element a;
								   for(int l = 0;l<al.getLength();l++){
									   a = (Element)al.item(l);
									   if(a.getAttribute("name").equals("name")){
										   field = new DatabaseField(fieldNum,a.getTextContent().trim().replace("#",""));
										   field.setTableID(tableNum);
										   table.addNativeField(fieldNum);
									   }
								   }
//								   alFields.add(new DatabaseFeild(feildNum, ))
	//							   feildNum++;
								   
								   fieldNum++;
								   connectorNum++;
								   alFields.add(field);
							   }
						   }
				       }
				   }
                   tableNum++;
	           }
	        }
		}
		catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public void parseSaveFile(BufferedReader br) throws IOException {
	      StringTokenizer stTables, stNatFields, stRelFields, stNatRelFields, stField;
	      DatabaseTable tempTable;
	      DatabaseField tempField;
	      String currentLine = br.readLine();
	      int numFigure = 0;
	      currentLine = br.readLine(); //this should be "Table: "
	      while (currentLine.startsWith("Table: ")) {
	    	 int numFields = 0;
	    	 int numTables = 0;
	    	 String tableName = "";
	         numFigure = Integer.parseInt(currentLine.substring(currentLine.indexOf(" ") + 1)); //get the Table number
	         currentLine = br.readLine(); //this should be "{"
	         currentLine = br.readLine(); //this should be "TableName"
	         tableName = currentLine.substring(currentLine.indexOf(" ") + 1);
	         tempTable = new DatabaseTable(numFigure,tableName);
	         
	         currentLine = br.readLine(); //this should be the NativeFields list
	         stNatFields = new StringTokenizer(currentLine.substring(currentLine.indexOf(" ") + 1), DELIM);
	         numFields = stNatFields.countTokens();
	         for (int i = 0; i < numFields; i++) {
	            tempTable.addNativeField(Integer.parseInt(stNatFields.nextToken()));
	         }
	         
	         currentLine = br.readLine(); //this should be the RelatedTables list
	         stTables = new StringTokenizer(currentLine.substring(currentLine.indexOf(" ") + 1), DELIM);
	         numTables = stTables.countTokens();
	         for (int i = 0; i < numTables; i++) {
	            tempTable.addRelatedTable(Integer.parseInt(stTables.nextToken()));
	         }
	         tempTable.makeArrays();
	         
	         currentLine = br.readLine(); //this should be the RelatedFields list
	         stRelFields = new StringTokenizer(currentLine.substring(currentLine.indexOf(" ") + 1), DELIM);
	         numFields = stRelFields.countTokens();

	         for (int i = 0; i < numFields; i++) {
	            tempTable.setRelatedField(i, Integer.parseInt(stRelFields.nextToken()));
	         }

	         alTables.add(tempTable);
	         currentLine = br.readLine(); //this should be "}"
	         currentLine = br.readLine(); //this should be "\n"
	         currentLine = br.readLine(); //this should be either the next "Table: ", #Fields#
	      }
	      while ((currentLine = br.readLine()) != null) {
	         stField = new StringTokenizer(currentLine, DELIM);
	         numFigure = Integer.parseInt(stField.nextToken());
	         String fieldName = stField.nextToken();
	         tempField = new DatabaseField(numFigure, fieldName);
	         tempField.setTableID(Integer.parseInt(stField.nextToken()));
	         tempField.setTableBound(Integer.parseInt(stField.nextToken()));
	         tempField.setFieldBound(Integer.parseInt(stField.nextToken()));
	         tempField.setDataType(Integer.parseInt(stField.nextToken()));
	         tempField.setVarcharValue(Integer.parseInt(stField.nextToken()));
	         tempField.setIsPrimaryKey(Boolean.valueOf(stField.nextToken()).booleanValue());
	         tempField.setDisallowNull(Boolean.valueOf(stField.nextToken()).booleanValue());
	         if (stField.hasMoreTokens()) { //Default Value may not be defined
	            tempField.setDefaultValue(stField.nextToken());
	         }
	         alFields.add(tempField);
	      }
		
	}

	@Override
	public void openFile(File inputFile) {
	      try {
	          InputStream fileStream = new FileInputStream(inputFile);
	          InputStream gzipStream = new GZIPInputStream(fileStream);
	          
	          parseFile(gzipStream);
             this.makeArrays(); //convert ArrayList objects into arrays of the appropriate Class type
             this.resolveConnectors(); //Identify nature of Connector endpoints

	       } // try
	       catch (FileNotFoundException fnfe) {
	          System.out.println("Cannot find \"" + inputFile.getName() + "\".");
	          System.exit(0);
	       } // catch FileNotFoundException
	       catch (IOException ioe) {
	          System.out.println(ioe);
	          System.exit(0);
	       } // catch IOException
		
	}
	
	private boolean isTableDup(String testTableName) {
		for (int i = 0; i < alTables.size(); i++) {
			 DatabaseTable tempTable = (DatabaseTable)alTables.get(i);
			 if (tempTable.getName().equals(testTableName)) {
			    return true;
			 }
		}
		return false;
   	}	

	private void makeArrays() { //convert ArrayList objects into arrays of the appropriate Class type
		if (alTables != null) {
			tables = (DatabaseTable[])alTables.toArray(new DatabaseTable[alTables.size()]);
		}
		if (alFields != null) {
			fields = (DatabaseField[])alFields.toArray(new DatabaseField[alFields.size()]);
		}
		if (alConnectors != null) {
			connectors = (EdgeConnector[])alConnectors.toArray(new EdgeConnector[alConnectors.size()]);
		}
   }

	private void resolveConnectors() { //Identify nature of Connector endpoints
	      int endPoint1, endPoint2;
	      int fieldIndex = 0, table1Index = 0, table2Index = 0;
	      for (int cIndex = 0; cIndex < connectors.length; cIndex++) {
	         endPoint1 = connectors[cIndex].getEndPoint1();
	         endPoint2 = connectors[cIndex].getEndPoint2();
	         fieldIndex = -1;
	         for (int fIndex = 0; fIndex < fields.length; fIndex++) { //search fields array for endpoints
	            if (endPoint1 == fields[fIndex].getNumFigure()) { //found endPoint1 in fields array
	               connectors[cIndex].setIsEP1Field(true); //set appropriate flag
	               fieldIndex = fIndex; //identify which element of the fields array that endPoint1 was found in
	            }
	            if (endPoint2 == fields[fIndex].getNumFigure()) { //found endPoint2 in fields array
	               connectors[cIndex].setIsEP2Field(true); //set appropriate flag
	               fieldIndex = fIndex; //identify which element of the fields array that endPoint2 was found in
	            }
	         }
	         for (int tIndex = 0; tIndex < tables.length; tIndex++) { //search tables array for endpoints
	            if (endPoint1 == tables[tIndex].getNumFigure()) { //found endPoint1 in tables array
	               connectors[cIndex].setIsEP1Table(true); //set appropriate flag
	               table1Index = tIndex; //identify which element of the tables array that endPoint1 was found in
	            }
	            if (endPoint2 == tables[tIndex].getNumFigure()) { //found endPoint1 in tables array
	               connectors[cIndex].setIsEP2Table(true); //set appropriate flag
	               table2Index = tIndex; //identify which element of the tables array that endPoint2 was found in
	            }
	         }
	         
	         if (connectors[cIndex].getIsEP1Field() && connectors[cIndex].getIsEP2Field()) { //both endpoints are fields, implies lack of normalization
	            JOptionPane.showMessageDialog(null, "The Edge Diagrammer file contains composite attributes. Please resolve them and try again.");
	            DatabaseConvertGUI.setReadSuccess(false); //this tells GUI not to populate JList components
	            break; //stop processing list of Connectors
	         }

	         if (connectors[cIndex].getIsEP1Table() && connectors[cIndex].getIsEP2Table()) { //both endpoints are tables
	            if ((connectors[cIndex].getEndStyle1().indexOf("many") >= 0) &&
	               (connectors[cIndex].getEndStyle2().indexOf("many") >= 0)) { //the connector represents a many-many relationship, implies lack of normalization
	               JOptionPane.showMessageDialog(null, "There is a many-many relationship between tables\n\"" + tables[table1Index].getName() + "\" and \"" + tables[table2Index].getName() + "\"" + "\nPlease resolve this and try again.");
	               DatabaseConvertGUI.setReadSuccess(false); //this tells GUI not to populate JList components
	               break; //stop processing list of Connectors*/
	            } else { //add Figure number to each table's list of related tables
	               tables[table1Index].addRelatedTable(tables[table2Index].getNumFigure());
	               tables[table2Index].addRelatedTable(tables[table1Index].getNumFigure());
	               continue; //next Connector
	            }
	         }
	         
	         if (fieldIndex >=0 && fields[fieldIndex].getTableID() == 0) { //field has not been assigned to a table yet
	            if (connectors[cIndex].getIsEP1Table()) { //endpoint1 is the table
	               tables[table1Index].addNativeField(fields[fieldIndex].getNumFigure()); //add to the appropriate table's field list
	               fields[fieldIndex].setTableID(tables[table1Index].getNumFigure()); //tell the field what table it belongs to
	            } else { //endpoint2 is the table
	               tables[table2Index].addNativeField(fields[fieldIndex].getNumFigure()); //add to the appropriate table's field list
	               fields[fieldIndex].setTableID(tables[table2Index].getNumFigure()); //tell the field what table it belongs to
	            }
	         } 
	         else if (fieldIndex >=0) { //field has already been assigned to a table
	            JOptionPane.showMessageDialog(null, "The attribute " + fields[fieldIndex].getName() + " is connected to multiple tables.\nPlease resolve this and try again.");
	            DatabaseConvertGUI.setReadSuccess(false); //this tells GUI not to populate JList components
	            break; //stop processing list of Connectors
	         }
	      } // connectors for() loop
	   } // resolveConnectors()	
	
}
