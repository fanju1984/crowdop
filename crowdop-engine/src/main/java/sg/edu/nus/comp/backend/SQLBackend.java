package sg.edu.nus.comp.backend;
import java.sql.*;

public class SQLBackend {
	Connection conn;
	
	String defaultServer;
	String defaultUserName;
	String defaultUserPass;
	String defaultDBName;
	
	public SQLBackend() {
        conn = null;
	}
	
	public void connectMySQL (String server, String userName, String password, String dbName) 
		throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://" + 
				server + 
				"/" + dbName + 
				"?useUnicode=true&characterEncoding=gbk&jdbcCompliantTruncation=false", 
				userName, password);
		defaultServer = server;
		defaultUserName = userName;
		defaultUserPass = password;
		defaultDBName = dbName;
		this.useDB(dbName);
	}
	
	public void disconnectMySQL () throws SQLException{
		//System.out.println("disconnect...");
		conn.close();
		conn = null;
        //System.out.println("Database connection terminated");
	}
	
	public void useDB (String dbName) throws Exception {
		String stat = "USE " + dbName;
		this.execute(stat);
	}
	
	public ResultSet getTables () throws Exception {
		return conn.getMetaData().getTables(null, null, null, null);
	}

	public void execute (String statement) throws Exception{
		//System.out.println("Now Execute: " + statement);
		//try {
			Statement stat = this.conn.createStatement();
			stat.execute(statement);
		//} catch (Exception e) {
//			this.connectMySQL(defaultServer, defaultUserName, defaultUserPass, defaultDBName);
//			Statement stat = this.conn.createStatement();
//			stat.execute(statement);
		//	e.printStackTrace();
		//	System.exit(1);
		//}
	}
	
	
	public ResultSet executeQuery (String statement) throws Exception {
		try {
			//System.out.println("Now Execute: " + statement);
			ResultSet rs = null;
			Statement stat = this.conn.createStatement();
			rs = stat.executeQuery(statement);
			return rs;
		} catch (Exception e) {
//			this.connectMySQL(defaultServer, defaultUserName, defaultUserPass, defaultDBName);
//			ResultSet rs = null;
//			Statement stat = this.conn.createStatement();
//			rs = stat.executeQuery(statement);
//			return rs;
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
}
