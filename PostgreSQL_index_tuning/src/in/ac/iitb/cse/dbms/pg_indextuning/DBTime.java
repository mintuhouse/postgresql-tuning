package in.ac.iitb.cse.dbms.pg_indextuning;

public class DBTime {
	private long time;
	
	public DBTime(long x){
		time = x;
	}
	
	public long get(){
		return time;
	}
	
	public void set(long x){
		time = x;
	}

}
