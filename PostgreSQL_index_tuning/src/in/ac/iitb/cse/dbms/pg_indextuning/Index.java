package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;



public class Index implements Comparable<Index>{
	private int id;
	private String name;
	private String table_name;
	private int table_id;
	private ArrayList<String> column_names;
	private ArrayList<Integer> column_indexes;
	private boolean hypothetical;
	private String dbname;
	private boolean materialized;
	
	public Index(int a_id, String a_name, String a_table_name, int a_table_id, 
			String a_column_names, String a_column_indexes, boolean a_hypothetical, String a_dbname, boolean a_materialized){
		id				= a_id;
		name			= a_name;
		table_name		= a_table_name;
		table_id 		= a_table_id;
		column_names	= new ArrayList<String>(Arrays.asList(a_column_names.split("\\s*,\\s*")));
		column_indexes 	= new ArrayList<Integer>(); //TODO
		Collections.sort(column_names);
		Collections.sort(column_indexes);
		hypothetical 	= a_hypothetical;
		dbname			= a_dbname;
		materialized	= a_materialized;
	}
	
	public boolean equals(Index x){
		if (x.column_names.equals(this.column_names) &&
				x.table_name.equals(this.table_name) &&
				x.dbname.equals(this.dbname)){
			return true;			
		} else {
			return false;			
		}
	}
	
	public boolean equals(String a_dbname, String a_table_name, String a_column_names){
		if(dbname.equals(a_dbname) &&
				table_name.equals(a_table_name) && 
				column_names.equals(new ArrayList<String>(Arrays.asList(a_column_names.split("\\s*,\\s*"))))){
			return true;
		}
		return false;
	}
	
	
	public void drop(DBConnection con) throws Exception{
		//TODO: Change curHypConfig automatically
		if(!materialized){
			System.out.println("ERROR: Trying to drop a non-materialized index "+ name);
			return;
		}
		if(con.getDBName().equals(dbname)){
			if(hypothetical){
				con.execStmt("DROP HYPOTHETICAL INDEX "+ name);
			}else{
				con.execStmt("DROP INDEX "+ name);
			}
			this.materialized = false;
		}else{
			System.out.println("ERROR: Dropping "+name+" : DB name didn't match connection");
		}
	}
	
	public void create(DBConnection con) throws Exception{
		//TODO: Change curHypConfig automatically
		if(materialized){
			System.out.println("ERROR: Trying to re-create materialized index "+ name);
			return;
		}
		if(con.getDBName().equals(dbname)){
			if(hypothetical){
				con.execStmt("CREATE HYPOTHETICAL INDEX "+name+" ON "+table_name+" ("+getColumnNames()+")");
			}else{
				con.execStmt("CREATE INDEX "+name+" ON "+table_name+" ("+getColumnNames()+")");				
			}
			this.materialized = true;
		}else{
			System.out.println("ERROR: Dropping "+name+" : DB name didn't match connection");
		}
	}
	
	public String getDBName(){
		return dbname;
	}
	
	public String getTableName(){
		return table_name;
	}
	
	public String getColumnNames(){
		return column_names.toString().replace("[", "").replace("]", "");
	}
	
	public boolean isHypothetical(){
		return hypothetical;
	}
	
	public static void main(String[] args){
		ArrayList<Integer> v = new ArrayList<Integer>(20);
		System.out.println(v.size());
		v.add(111);
		System.out.println(v.size());	
	}

	@Override
	public int compareTo(Index o) {
		// TODO Auto-generated method stub
		if (this.column_names.size() < o.column_names.size()){
			return -1;
		} else if (this.column_names.size() > o.column_names.size()){
			return 1;
		} else {
			for (int i = 0; i < this.column_names.size(); i++){
				int x = this.column_names.get(i).compareTo(o.column_names.get(i));
				if (x != 0){
					return x;
				}
			}			
			return 0;
		}
	}
	
	
}
