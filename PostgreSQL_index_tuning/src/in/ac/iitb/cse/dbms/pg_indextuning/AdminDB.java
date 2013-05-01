package in.ac.iitb.cse.dbms.pg_indextuning;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDB {
	
	private String DB_NAME = null;
	private String IT_DB_NAME = null;
	
	private DBConnection DBCon = null;
	//private DBConnection ADBCon = null;
	
	AdminDB(){
		DBCon =  new DBConnection();
		//ADBCon = new DBConnection();
		DB_NAME = DBCon.getDBName();
		IT_DB_NAME = "it_"+DB_NAME;
		DBCon.createConnection(DB_NAME);
		//ADBCon.createConnection(IT_DB_NAME);		
	}

	public void createStatOnlyDB() throws Exception {
		DBCon.execUpdate("CREATE DATABASE "+IT_DB_NAME+" WITH TEMPLATE "+DB_NAME);
		System.out.println("Successfully created database "+ IT_DB_NAME);
	}
	
	public void deleteStatOnlyDB() throws Exception {
		DBCon.execUpdate("DROP DATABASE "+IT_DB_NAME);
		System.out.println("Dropped the database "+ IT_DB_NAME);
	}
	
	private ResultSet getIndexColumns() throws Exception {
		String sql = 	"select"+
						"	i.oid as index_oid,"+
						"    t.relname as table_name,"+
						"    i.relname as index_name,"+
						"    array_to_string(array_agg(a.attname), ', ') as column_names, "+
					    "	 ix.indishypothetical "+
						"from"+
						"    pg_class t,"+
						"    pg_class i,"+
						"    pg_index ix,"+
						"    pg_attribute a "+
						"where"+
						"    t.oid = ix.indrelid"+
						"    and i.oid = ix.indexrelid"+
						"    and a.attrelid = t.oid"+
						"    and a.attnum = ANY(ix.indkey)"+
						"    and t.relkind = 'r'"+
						"    and t.relname IN (select table_name from information_schema.tables WHERE table_schema = 'public') "+
						"    and NOT ix.indisunique "+
						"group by"+
						"    t.relname,"+
						"    i.relname,"+
						"    i.oid," +
						"	 ix.indishypothetical "+
						"order by"+
						"    t.relname,"+
						"    i.relname";
		ResultSet rs = DBCon.execQuery(sql);
		//DBCon.displayResult(rs);
		return rs;
	}
	
	private void dropIdxAndCreateHypIdx() throws Exception{
		ResultSet rs = getIndexColumns();
		while(rs.next())
        {
			int pgc_idx_oid 	= rs.getInt("index_oid");
			String table_name 	= rs.getString("table_name");
			String index_name 	= rs.getString("index_name");
			String column_names = rs.getString("column_names");
			boolean ishyp		= rs.getBoolean("indishypothetical");
			if(! ishyp){
				DBConnection ADBCon1 =  new DBConnection();
				ADBCon1.createConnection(IT_DB_NAME);
				ADBCon1.execUpdate("DROP INDEX "+ index_name);
				ADBCon1.execUpdate("CREATE HYPOTHETICAL INDEX hi_"+index_name+" ON "+table_name+" ("+column_names+")");
				ADBCon1.closeConnection();
				System.out.println("Created hyp index hi_"+index_name);
			}
		}
		System.out.println("Completed Hypothetical Index Creation");
	}
	
	
	public static void main(String[] args) throws Exception{
		AdminDB adb = new AdminDB();
		adb.deleteStatOnlyDB();
		adb.createStatOnlyDB();
		adb.dropIdxAndCreateHypIdx();
		adb.DBCon.closeConnection();
	}
}
