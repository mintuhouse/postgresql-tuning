package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;

public class IndexTuner {
	DBConnection ADBCon			= null;
	ArrayList<ArrayList<Index> > allCand;
	ArrayList<ArrayList<Index> > curCand;
	ArrayList<DBTime> w;
	ArrayList<Index> sugg;
	
	public IndexTuner(DBConnection conx){
		ADBCon 	= conx; 
		curCand = null; 
		w 		= null; 
		allCand = null; 
		sugg 	= null;
		init();
	}
	
	private void init(){
		//TODO : sugg is the current set of indices in the database
		//allCand : Load all the possible candidates from the previous work load
		//curCand : run greedy(m, k) to get a set of possible indices
		//w : it is transition for each set of indices w[X] =  changeConfig(X, sugg);
				
	}
	
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
	
	/*
	 * Called from the init
	 * can be merged into init
	 */
	
	private void greedymk1(){
		
		
	}
	
	/*
	 * Called when a new query is added
	 */
	private void greedymk2(){
		
		
	}
	public DBTime changeConfig(ArrayList<Index> cur, ArrayList<Index> target){
		return null;			
	}
	
	public void analyze(String query){		

	}
	
	public void feedback(ArrayList<Index> fPlus, ArrayList<Index> fMinus){

	}
	
	public void updateCandidates(String query){
		
	}
	
	/*
	 * This is the main function to called
	 * this returns back the set of indices to be recommended
	 */			
	public ArrayList<Index> recommend(String query, ArrayList<Index> fPlus, ArrayList<Index> fMinus){
		updateCandidates(query);
		analyze(query);
		feedback(fPlus, fMinus);
		return sugg;		
	}
}
