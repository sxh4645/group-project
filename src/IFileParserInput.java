import java.io.*;

public interface IFileParserInput {  
		
	/* Default Method for parsing a File */
	void parseFile(BufferedReader br) throws IOException;
	
	void parseSaveFile(BufferedReader br) throws IOException;
	
	void openFile(File inputFile);
	
	DatabaseTable[] getTables();
	
	DatabaseField[] getFields();
	
}