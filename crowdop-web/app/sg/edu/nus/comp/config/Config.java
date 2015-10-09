package sg.edu.nus.comp.config;

public class Config {
	private static Config ins = null;
	
	
	private Config () {
		
	}
	
	public static String DBServer = "localhost";
	public static String DBUser = "sqlsugg";
	public static String DBPass = "sqlsugg";
	
	public static String workDB = "crowdop_work";
	public static String benchmarkDB = "crowdop_benchmark";
	public static String tmpDB = "crowdop_tmp";
	public static String resultDB = "crowdop_result";
	public static String taskDB = "crowdop_task";
	
	
	public static String querylogTable = "querylog";
	
	public static String KeyAttribute = "id";
	
	public static double sampleRatio = 0.01;
	
	public static String execDir = "execution_tmp/";
	
	public static String htmlTemplatePath = "src/main/resources/basic_html_templates/";

	public static String getMTurkPropFile() {
		return "mturk/mturk.properties";
	}
}
