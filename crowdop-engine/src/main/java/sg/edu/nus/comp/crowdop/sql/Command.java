package sg.edu.nus.comp.crowdop.sql;

public abstract class Command {
	protected CommandType commandType;
	
	protected Command(CommandType type) {
		this.commandType = type;
	}
	
	public CommandType getType() {
		return commandType;
	}
	
}
