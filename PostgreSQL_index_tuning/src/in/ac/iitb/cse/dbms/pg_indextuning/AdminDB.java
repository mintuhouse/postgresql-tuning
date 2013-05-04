package in.ac.iitb.cse.dbms.pg_indextuning;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AdminDB {
	
	
	private String DB_HOST = "127.0.0.1";
	private String DB_PORT = "5432";
	private String DB_NAME = "dellstore2";
	private String DB_USER = "postgres";
	private String DB_PASS = "postgres";

	private String ADB_HOST = "127.0.0.1";
	private String ADB_PORT = "5432";
	private String ADB_NAME = "it_dellstore2";
	private String ADB_USER = "postgres";
	private String ADB_PASS = "postgres";
	
	private DBConnection DBCon = null;
	private DBConnection ADBCon = null;
	
	
	AdminDB() throws Exception{
		readConfig("config.cfg");
		initAdmin();
	}
	
	public void readConfig(String file){
		//TODO: Read database credentials from a file
	}
	
	public void initAdmin() throws Exception{
		//deleteStatOnlyDB();
		if(!doesDBExist(ADB_NAME)){
			createStatOnlyDB(ADB_NAME, DB_NAME);
			createDBConnections();
			dropIdxAndCreateHypIdx();
		}
		createDBConnections();
		DBCon.execExplain("SELECT * FROM products p JOIN inventory i ON i.prod_id = p.prod_id");
		DBCon.execExplain("INSERT INTO orders (customerid, netamount, tax , totalamount ) VALUES (1, 100, 121, 220)");
		DBCon.execExplain("UPDATE orders SET tax = 10 WHERE customerid=12");
	}
	
	private void createDBConnections() throws SQLException{
		if(DBCon == null){
			DBCon = new DBConnection(DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME);
			DBCon.vacuumAnalyze();
		}
		if(ADBCon == null){
			ADBCon = new DBConnection(ADB_HOST, ADB_PORT, ADB_USER, ADB_PASS, ADB_NAME);		
			ADBCon.vacuumAnalyze();
		}
	}
	
	private boolean doesDBExist(String ADB_NAME) throws Exception{
		DBConnection con = new DBConnection(ADB_HOST, ADB_PORT, ADB_USER, ADB_PASS, "");
		ResultSet rs = con.getResultSet("SELECT 1 as exist FROM pg_database WHERE datname='"+ADB_NAME+"'");
		if(rs.next()){
			con.closeConnection();
			return true;
		}else{
			con.closeConnection();
			return false;
		}
	}
	
	public void createStatOnlyDB(String ADB_NAME, String TemplateDB) throws Exception {
		//TODO: Make it statistics only database
		DBConnection con = new DBConnection(ADB_HOST, ADB_PORT, ADB_USER, ADB_PASS, "");
		con.execStmt("CREATE DATABASE "+ADB_NAME+" WITH TEMPLATE "+TemplateDB);
		con.closeConnection();
		System.out.println("Successfully created database "+ ADB_NAME); //DEBUG
	}
	
	public void deleteStatOnlyDB() throws Exception {
		/*TODO: Check if it is a Stat Only Database, created for admin purposes */
		DBConnection con = new DBConnection(ADB_HOST, ADB_PORT, ADB_USER, ADB_PASS, "");
		con.execStmt("DROP DATABASE "+ADB_NAME);
		con.closeConnection();
		System.out.println("Dropped the database "+ ADB_NAME); //DEBUG
	}
		
	private void dropIdxAndCreateHypIdx() throws Exception{
		ResultSet rs = ADBCon.getIndexColumns();
		while(rs.next())
        {
			int pgc_idx_oid 	= rs.getInt("index_oid");
			String table_name 	= rs.getString("table_name");
			String index_name 	= rs.getString("index_name");
			String column_names = rs.getString("column_names");
			boolean ishyp		= rs.getBoolean("indishypothetical");
			Index ind = new Index(pgc_idx_oid, index_name, table_name, 0, column_names, "", false, ADB_NAME, true);
			if(! ishyp){
				ind.drop(ADBCon);
				Index hypind = new Index(0, "hi_"+index_name, table_name, 0, column_names, "", true, ADB_NAME, false);
				hypind.create(ADBCon);
				System.out.println("Created hyp index hi_"+index_name);
			}
		}
		System.out.println("Completed Hypothetical Index Creation");
	}
	
	//TODO: Move it to IndexTuner later
	public DBTime whatIf(String query, ArrayList<Index> configuration) throws Exception{
		//TODO: Compare and check the existence of queries	
		// Improve the efficiency by comparing the indexes before deleting
		// Or even better don't make the database query to fetch the list of indexes 
		// 		in DB each time but maintain the list in connection, if DB_NAME is set
		for(Index i: ADBCon.curHypConfig){
			i.drop(ADBCon);
		}
		ADBCon.curHypConfig.clear();
		for(Index i: configuration){
			if(i.isHypothetical()){
				i.create(ADBCon);
				ADBCon.curHypConfig.add(i); //TODO: Check whether materialization status is changed
			}else{
				System.out.println("ERROR: Trying to create materialized index from whatIf");
			}
		}
		return ADBCon.execExplain(query);
	}
		
	public static void main(String[] args) throws Exception{
		AdminDB adb = new AdminDB();
	}
}
