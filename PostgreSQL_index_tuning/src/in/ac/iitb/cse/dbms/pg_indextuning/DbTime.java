package in.ac.iitb.cse.dbms.pg_indextuning;

public class DbTime {
	private long time;
	
	public DbTime(long x){
		time = x;
	}
	
	public long get(){
		return time;
	}
	
	public void set(long x){
		time = x;
	}

}
