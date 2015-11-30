

public interface IFileParserOutput {
	
	String getProductName(); 
	
	String getDatabaseName();
	
	String generateDatabaseName();
	
	String getSQLString();
	
	void createDDL();
}
