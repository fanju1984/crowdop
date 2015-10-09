package sg.edu.nus.comp.crowdop.sql;

/**
 * The type of a sql-like statement or command
 * @author fanj
 *
 */

public enum CommandType {
	UNKNOWN		(0x00, null),
	SELECTDB	(0x02, SelectDatabaseCommand.class),
	SELECT		(0x03, SelectCommand.class);
//	QUERYTABLE		(0x04, QueryTableCommand.class);

	
	
	private int protocolId;
	private Class<? extends Command> instanceClass;
	
	private CommandType(int protocolId, Class<? extends Command> instanceClass) {
		this.protocolId = protocolId;
		this.instanceClass = instanceClass;
	}
	
	public int getProtocolId() {
		return protocolId;
	}
	
	public Class<? extends Command> getCommandClass() {
		return instanceClass;
	}
	
	public static CommandType findType(int protocolId) {
		for (CommandType type: CommandType.values()) {
			if (type.protocolId == protocolId) {
				return type;
			}
		}
		
		return UNKNOWN;
	}
}
