package in.ac.iitb.cse.dbms.pg_indextuning;

public class AdminDB {
	
	private String DB_NAME = null;
	private String IT_DB_NAME = null;
	
	private DBConnection DBCon = null;
	
	AdminDB(){
		DBCon =  new DBConnection();
		DBCon.createConnection();	
		DB_NAME = DBCon.getDBName();
		IT_DB_NAME = "it_"+DB_NAME;
	}

	public void createStatOnlyDB() throws Exception {
		DBCon.execUpdate("CREATE DATABASE "+IT_DB_NAME+" WITH TEMPLATE "+DB_NAME);
		System.out.println("Successfully created database "+ IT_DB_NAME);
	}
	
	public void deleteStatOnlyDB() throws Exception {
		DBCon.execUpdate("DROP DATABASE "+IT_DB_NAME);
		System.out.println("Dropped the database "+ IT_DB_NAME);
	}
	
	
	public static void main(String[] args) throws Exception{
		AdminDB adb = new AdminDB();
		//adb.deleteStatOnlyDB();
		adb.createStatOnlyDB();
		adb.DBCon.closeConnection();
	}
}
