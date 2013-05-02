package in.ac.iitb.cse.dbms.pg_indextuning;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
	
	private String DB_HOST;
	private String DB_PORT;
	private String DB_NAME;
	private String DB_USER;
	private String DB_PASS;	
	
	private Connection dbConnection = null;
	private Statement stmt = null;
	
	DBConnection(String DB_HOST, String DB_PORT, String DB_USER, String DB_PASS, String aDB_NAME){
		DB_NAME = aDB_NAME;
		try { 
			Class.forName("org.postgresql.Driver");
			createConnection( DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME);
		} catch (ClassNotFoundException e) { 
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
			return; 
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
	
	public void testConnection(){
		if (dbConnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
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
	
	public void createHypIndex(String table_name, String column_names, String index_name ){
		//TODO: Create a global map of all indexes created
		//TODO: Merge both the classes 
	}

	public static void main(String[] args) throws SQLException{
		//DBConnection DBCon =  new DBConnection();
		//DBCon.createConnection(DB_NAME);
		//ResultSet rs = DBCon.execQuery("SELECT * FROM orders WHERE customerid < 100");
		//DBCon.displayResult(rs);
		//DBCon.execQuery(CREATE HYPOTHETICAL INDEX ON );
		//DBCon.execQuery("CREATE DATABASE it_dellstore2 WITH TEMPLATE dellstore2");
		//DBCon.closeConnection();
	}

}
