package sg.edu.nus.comp.crowdop.core.op.crowdop;

import sg.edu.nus.comp.crowdop.core.op.Op;


public abstract class CrowdOp extends Op {
	protected int DATA_LOAD_BATCH_SIZE = 1000;
	
	public CrowdOp(int oid) {
		super(oid);
	}
	
	
	
	
	
//	public abstract Map<String, Tuple> crowdsource (DataStore tmpStore, 
//			List<String>inputTables) throws Exception ;
	
	
	
//	public abstract void execute (DataStore inStore, List<String> inTableNames,
//			DataStore outStore, String outTableName, 
//			Map<String, String> rowToAnswer) throws Exception;
	
//	public abstract List<Microtask> crowdsource (List<DataTable> inTables, 
//			DataTable opTmpTable, DataStore tmpStore, 
//			JSONObject objTemplate) throws Exception;
	
//	protected String loadOpTemplate (String opType) throws Exception {
//		String curDir = System.getProperty("user.dir");
//		String path = curDir + "/bin/basic_html_templates/" + opType + ".html";
//		String opTemplate = FileUtil.file2String(path);
//		return opTemplate;
//	}
	
//	protected String renderTuple (List<Field> fields, Tuple tuple, JSONObject relTemplate) {
//		JSONArray vars = relTemplate.getJSONArray("variables");
//		Map<String, Integer> fieldName2Index = new HashMap<String, Integer> ();
//		for (int i = 0; i < fields.size(); i ++) {
//			Field field = fields.get(i);
//			fieldName2Index.put(field.getName(), i);
//		}
//		String template = relTemplate.getString("template");
//		Object[] values = new Object[vars.length()];
//		
//		for (int i = 0; i < vars.length(); i ++) {
//			String var = vars.getString(i);
//			String value = null;
//			Integer index = fieldName2Index.get(var);
//			if (index != null) {
//				value = tuple.getValue(index);
//			}
//			values[i] = value;
//		}
//		String tupleStr = String.format(template, values);
//		return tupleStr;
//	}
}
