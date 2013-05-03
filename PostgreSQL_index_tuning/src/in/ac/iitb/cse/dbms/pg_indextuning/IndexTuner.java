package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;

public class IndexTuner {
	DBConnection con;
	ArrayList<ArrayList<Index> > allCand;
	ArrayList<ArrayList<Index> > curCand;
	ArrayList<DBTime> w;
	ArrayList<Index> sugg;
	
	
	public IndexTuner(DBConnection conx){
		con = conx; curCand = null; w = null; allCand = null; sugg = null;
		init();
	}
	
	private void init(){
		//TODO : sugg is the current set of indices in the database
		//allCand : Load all the possible candidates from the previous work load
		//curCand : run greedy(m, k) to get a set of possible indices
		//w : it is transition for each set of indices w[X] =  changeConfig(X, sugg);
				
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
