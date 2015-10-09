package sg.edu.nus.comp.importer;

import java.util.*;

import sg.edu.nus.comp.datastore.schema.*;

public abstract class FileImporter {

	public static FileImporter create(String filename) throws Exception {
		//if (filename.endsWith(".csv")) {
			return new CSVImporter(filename);
		//}
		// TODO Auto-generated method stub
		//return null;
	}

	public abstract void runImport(List<Field> fields, 
			List<List<String>> valueTuples) throws Exception;

}
