package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;
import java.util.TreeSet;

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
	
	private ArrayList<Index> extractIndexes(String query){
		return null;
	}
	
	private void greedymk1(){
		
		
	}
	
	/*
	 * Called when a new query is added
	 */
	private ArrayList<Index> greedymk2(ArrayList<Index> newPoss){
		return null;	
	}
	
		
	public DBTime changeConfig(ArrayList<Index> cur, ArrayList<Index> target){
		return null;			
	}
	
	public void analyze(String query){
		ArrayList<DBTime> execTime = new ArrayList<DBTime>(curCand.size());
		for (ArrayList<Index> conf : curCand){
			try {
				execTime.add(whatIf(query, conf));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ArrayList<DBTime> wdash = new ArrayList<DBTime>(w);
		ArrayList<Boolean> pres = new ArrayList<Boolean>(curCand.size());
		
		// TODO: change below as two iterators for 2 arraylist
		for (int i = 0; i < w.size(); i++){
			w.get(i).set(w.get(i).get() +  execTime.get(i).get());
			pres.add(false);
		}
		
		for (int i = 0; i < w.size(); i++){
			float mini = w.get(i).get();
			for (int j = 0; j < w.size(); j++){
				mini = Math.min(mini, w.get(j).get() + 
						changeConfig(curCand.get(i), curCand.get(j)).get());
			}
			if (mini == w.get(i).get()){
				pres.add(true);
			}
			wdash.get(i).set(mini);
		}
		
		for (int i = 0; i < w.size(); i++){
			DBTime changeTime = changeConfig(curCand.get(i), sugg);
			execTime.get(i).set(wdash.get(i).get() + changeTime.get());
			w.get(i).set(wdash.get(i).get());
		}
		
		float mini = execTime.get(0).get();
		for (int i = 1; i < w.size(); i++){
			mini = Math.min(mini, execTime.get(i).get());			
		}
		
		for (int i = 1; i < w.size(); i++){
			if (execTime.get(i).get() == mini && pres.get(i)){
				sugg = curCand.get(i);
			}
		}
	}
	
	public void feedback(ArrayList<Index> fPlus, ArrayList<Index> fMinus){
		ArrayList<Index> newList = new ArrayList<Index>(fPlus); 
		TreeSet<Index> fMinusSet = new TreeSet<Index> (fMinus);
		for (Index ix : sugg){
			if (!fPlus.contains(ix) && !fMinusSet.contains(ix)){
				newList.add(ix);
			} 
		}		
		int indexSugg = curCand.indexOf(sugg);
		
		for (int i = 0; i < curCand.size(); i++){
			ArrayList<Index> s = curCand.get(i);
			ArrayList<Index> sCons = new ArrayList<Index> (fPlus);
			for (Index ix : s){
				if (!fPlus.contains(ix) && !fMinusSet.contains(ix)){
					sCons.add(ix);
				}
			}
			DBTime minDiff = changeConfig(s, sCons);
			minDiff.set(minDiff.get() + changeConfig(sCons, s).get());
			DBTime diff = w.get(i);
			diff.set(diff.get() + changeConfig(s, sugg).get());
			diff.set(diff.get() + w.get(indexSugg).get());
			if (diff.get() < minDiff.get()){
				w.get(i).set(w.get(i).get() + minDiff.get() - diff.get());
			}
		}
		sugg = newList;
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
