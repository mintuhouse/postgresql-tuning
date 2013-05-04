package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;

public class Master {
	
	DBConnection ADBCon;
	int k = 7;
	int maxParition = 10;
	ArrayList<String> workbench;
	Partitioner pr;
	ArrayList<Index> allcand = new ArrayList<Index>(); 
	
	IndexTuner[] ik = new IndexTuner[k];
	
	public Master(DBConnection adb){
		ADBCon = adb;
		workbench = new ArrayList<String>();
		pr = new Partitioner(workbench, adb, k, maxParition, 0);
		for (int i = 0; i < k; i++){
			
		}
		
	}

	public ArrayList<Index> greedymk(){
		return null;
	}
	
	public void getCand(String query){
		
	}	
	public void addQuery(String query){
		workbench.add(query);
		getCand(query);
	}
	 

	public void updateCand(){
		
	}
	
	public ArrayList<Index> suggest(ArrayList<Index> FPlus, ArrayList<Index> FMinus) throws Exception{
		ArrayList<Index> curCand  = greedymk();
		ArrayList<ArrayList<Index>> ps = pr.makeParitions(curCand);
		updateCand();
		return null;
	}
	
	
	
		
}
