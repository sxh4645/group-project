import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public abstract class FileParserInput implements IFileParserInput {

	public abstract void parseFile(BufferedReader br) throws IOException;

	public abstract void parseSaveFile(BufferedReader br) throws IOException;
	
	public abstract void openFile(File inputFile);

	public abstract DatabaseTable[] getTables();

	public abstract DatabaseField[] getFields();

}
