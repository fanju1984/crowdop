package sg.edu.nus.comp.importer;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import sg.edu.nus.comp.datastore.schema.*;


public class CSVImporter extends FileImporter{
	File srcFile;
	
	public CSVImporter (String srcFilename) throws Exception {
		srcFile = new File(srcFilename);
	}	

	@Override
	public void runImport(List<Field> fields, List<List<String>> valueTuples) throws Exception {
		Reader in = new FileReader(srcFile.getAbsolutePath());
		Iterator<CSVRecord> csvIter = CSVFormat.EXCEL.parse(in).iterator();
		int row = 0;
		while (csvIter.hasNext()) {
			CSVRecord record = csvIter.next();
			if (row == 0) { // read the new headers from the source file
				for (int i = 0; i < record.size(); i ++) {
					String value = record.get(i);
					Field field = null;
					String[] tmps = value.split("::");
					if (tmps.length == 2) {
						field = new Field (tmps[0], VariableType.STRING, true);
					} else {
						field = new Field(value, VariableType.STRING, false);
					}
					fields.add(field);
				}
			} else {
				String[] values = new String[record.size()];
				for (int i = 0; i < record.size(); i ++) {
					values[i] = record.get(i);
				}
				valueTuples.add(Arrays.asList(values));
			}
			row ++;
		}
		in.close();
	}
}
