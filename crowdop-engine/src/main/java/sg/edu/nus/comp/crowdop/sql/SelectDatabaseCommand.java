package sg.edu.nus.comp.crowdop.sql;

public class SelectDatabaseCommand extends Command {
	private String dbName;
	
	protected SelectDatabaseCommand() {
		super(CommandType.SELECTDB);
	}
	
	public SelectDatabaseCommand(String dbName) {
		super(CommandType.SELECTDB);
		this.dbName = dbName;
	}
	
	public String getDbName() {
		return dbName;
	}
}
