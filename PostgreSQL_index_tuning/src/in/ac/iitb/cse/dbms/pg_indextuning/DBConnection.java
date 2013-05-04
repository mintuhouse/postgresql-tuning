package in.ac.iitb.cse.dbms.pg_indextuning;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBConnection {
	
	private String DB_HOST;
	private String DB_PORT;
	private String DB_NAME;
	private String DB_USER;
	private String DB_PASS;	
	
	private Connection dbConnection = null;
	private Statement stmt = null;
	
	public ArrayList<Index> curHypConfig = null;
	
	DBConnection(String DB_HOST, String DB_PORT, String DB_USER, String DB_PASS, String aDB_NAME){
		DB_NAME = aDB_NAME;
		try { 
			Class.forName("org.postgresql.Driver");
			createConnection( DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME);			
		} catch (ClassNotFoundException e) { 
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
		} 	
		if(!DB_NAME.equals("")){
			try {
				curHypConfig = new ArrayList<Index>();
				loadCurrentConfiguration();
			} catch (Exception e) {
				System.out.println("Cannot load initial configuration i.e., hyp indexes");
				e.printStackTrace();
			}
		}
	}
		
	public void createConnection(String DB_HOST, String DB_PORT, String DB_USER, String DB_PASS, String DB_NAME ){
		if (dbConnection == null){
			try { 
				System.out.println("jdbc:postgresql://"+DB_HOST+":"+DB_PORT+"/"+DB_NAME);
				dbConnection = DriverManager.getConnection("jdbc:postgresql://"+DB_HOST+":"+DB_PORT+"/"+DB_NAME, DB_USER, DB_PASS);
				stmt = dbConnection.createStatement();
			} catch (SQLException e) {
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
				return;
			}
		}
	}
	
	public DBTime execExplain(String query){
		//TODO: Do it only for select, update, insert queries
		try {
			ResultSet rs = stmt.executeQuery("explain "+ query);
			if(rs.next()){ 
				String mydata = rs.getString(1);
				Pattern pattern = Pattern.compile("cost=(.*?) rows=");
				Matcher matcher = pattern.matcher(mydata);
				if (matcher.find())
				{
					String group = matcher.group(1);
					String[] parts = group.split("\\.\\.");
					DBTime t = new DBTime(Float.parseFloat(parts[1]));
					return t;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error calculating explain query time");
			e.printStackTrace();
		}
		return null;
	}	
	
	public void testConnection(){
		if (dbConnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
	}
	
	public void vacuumAnalyze() throws SQLException {
		stmt.execute("VACUUM ANALYZE");
	}
	

	public void closeConnection() throws SQLException{
		stmt.close();
		dbConnection.close();
	}	

	public String getDBName(){
		return DB_NAME;
	}
	
	public ResultSet getResultSet(String sql) throws SQLException {
		ResultSet rs = stmt.executeQuery(sql);
		return rs;
	}
	
	public void execStmt(String sql) throws SQLException {
		stmt.executeUpdate(sql);
	} 
		
	public void displayResultSet(ResultSet rs) throws SQLException{
		ResultSetMetaData meta= rs.getMetaData();
		String cols[]=new String[meta.getColumnCount()];
		for(int i=0;i< cols.length;++i)
        {
			cols[i]= meta.getColumnLabel(i+1);
			System.out.print(cols[i]+'\t');
        }
		System.out.print("\n");
		while(rs.next())
        {
			for(int i=0;i< cols.length;++i)
            {
				System.out.print(rs.getObject(i+1));
				System.out.print("\t");
            }
			System.out.print("\n");
        }
	}
	
	public ResultSet getIndexColumns() throws Exception {
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
		ResultSet rs = getResultSet(sql);
		//DBCon.displayResult(rs);
		return rs;
	}
	
	private void loadCurrentConfiguration() throws Exception{
		ResultSet rs = getIndexColumns();
		while(rs.next())
        {
			int pgc_idx_oid 	= rs.getInt("index_oid");
			String table_name 	= rs.getString("table_name");
			String index_name 	= rs.getString("index_name");
			String column_names = rs.getString("column_names");
			boolean ishyp		= rs.getBoolean("indishypothetical");
			Index ind = new Index(pgc_idx_oid, index_name, table_name, 0, column_names, "", true, DB_NAME, true);
			if(ishyp){
				curHypConfig.add(ind);
				System.out.println(ind.name);//DEBUG
			}
        }
	}
	/*
	public static void main(String[] args) throws SQLException{
		//DBConnection DBCon =  new DBConnection();
		//DBCon.createConnection(DB_NAME);
		//ResultSet rs = DBCon.execQuery("SELECT * FROM orders WHERE customerid < 100");
		//DBCon.displayResult(rs);
		//DBCon.execQuery(CREATE HYPOTHETICAL INDEX ON );
		//DBCon.execQuery("CREATE DATABASE it_dellstore2 WITH TEMPLATE dellstore2");
		//DBCon.closeConnection();
	}
	*/
}
