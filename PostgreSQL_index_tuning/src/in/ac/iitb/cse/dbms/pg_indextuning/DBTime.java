package in.ac.iitb.cse.dbms.pg_indextuning;


public class DBTime implements Comparable<DBTime> {
	private Float time;
	
	public DBTime(Float x){
		time = x;
	}
	
	public Float get(){
		return time;
	}
	
	public void set(Float x){
		time = x;
	}
	
	public void add(DBTime y){
		time = time + y.get();
	}
	
	public void diff(DBTime y){
		time = time - y.get();
	}

	@Override
	public int compareTo(DBTime o) {
		// TODO Auto-generated method stub
		if (this.time < o.time){
			return -1; 
		} else if (this.time > o.time){
			return 1;
		} else {
			return 0;
		}
	} 
	

}
