package in.ac.iitb.cse.dbms.pg_indextuning;

public class DBTime {
	private Double time;
	
	public DBTime(Double x){
		time = x;
	}
	
	public Double get(){
		return time;
	}
	
	public void set(Double x){
		time = x;
	}
	
	public void add(DBTime y){
		time = time + y.get();
	}
	
	public void diff(DBTime y){
		time = time - y.get();
	}

}
