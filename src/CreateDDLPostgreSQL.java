import java.awt.*;
import java.awt.event.*;
import javax.swing.*;   
import javax.swing.event.*;
import java.io.*;
import java.util.*;

public class CreateDDLPostgreSQL extends FileParserOutput {

	public static final String PRODUCT 	= "MyPostgreSQL";
	public static final String DEFAULT 	= "MyPostgreSQLDB";	
	
	public CreateDDLPostgreSQL(DatabaseTable[] inputTables, DatabaseField[] inputFields) {
	      super(inputTables, inputFields);
	      sb = new StringBuffer();
	}	
	
	public CreateDDLPostgreSQL(){
		
	}
	
	@Override
	public String generateDatabaseName() {
	      String dbNameDefault = "MyPostgreSQL";
	      //String databaseName = "";

	      do {
	         databaseName = (String)JOptionPane.showInputDialog(
	                       null,
	                       "Enter the database name:",
	                       "Database Name",
	                       JOptionPane.PLAIN_MESSAGE,
	                       null,
	                       null,
	                       dbNameDefault);
	         if (databaseName == null) {
	            DatabaseConvertGUI.setReadSuccess(false);
	            return "";
	         }
	         if (databaseName.equals("")) {
	            JOptionPane.showMessageDialog(null, "You must select a name for your database.");
	         }
	      } while (databaseName.equals(""));
	      return databaseName;
	}

	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public String getProductName() {
		return "PostgreSQL";
	}

	@Override
	public String getSQLString() {
		createDDL();
		return sb.toString();
	}

	@Override
	public void createDDL() {
      DatabaseConvertGUI.setReadSuccess(true);
      databaseName = generateDatabaseName();
      sb.append("I NEED TO BE IMPLEMENTED");
      
      // TODO Auto-generated method stub
	}

}
