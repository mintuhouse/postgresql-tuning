package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


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
		simulateConfig(configuration);
		return ADBCon.execExplain(query);
	}
	
	public DBTime costOf(ArrayList<String> workload, ArrayList<Index> configuration) throws Exception{
		simulateConfig(configuration);
		DBTime cst = new DBTime(0.0);
		for(String query: workload){
			cst.add(ADBCon.execExplain(query));
		}
		return cst;
	}
	
	public void simulateConfig(ArrayList<Index> configuration) throws Exception{
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
	}
	
	private void getSubsets(List<Integer> superSet, int k, int idx, 
			Set<Integer> current,List<Set<Integer>> solution) {
	    if (current.size() == k) {
	        solution.add(new HashSet<Integer>(current));
	        return;
	    }
	    if (idx == superSet.size()) return;
	    Integer x = superSet.get(idx);
	    current.add(x);
	    getSubsets(superSet, k, idx+1, current, solution);
	    current.remove(x);
	    getSubsets(superSet, k, idx+1, current, solution);
	}

	public List<Set<Integer>> getSubsets(List<Integer> superSet, int k) {
	    List<Set<Integer>> res = new ArrayList<Set<Integer>>();
	    getSubsets(superSet, k, 0, new HashSet<Integer>(), res);
	    return res;
	}
		
	/*
	 * Called from the init
	 * can be merged into init
	 */	
	private ArrayList<Index> greedymk(ArrayList<String> workload, ArrayList<Index> CandIndexes, int m, int k) throws Exception{
		//Map< Index, DBTime> Cost = new HashMap< Index, DBTime>();
		List<Integer> superSet = new ArrayList<Integer>();	
		for(int i=0;i<CandIndexes.size();i++){
			superSet.add(i);
		}
		List<Set<Integer>> subSets = new ArrayList<Set<Integer>>();
		subSets = getSubsets(superSet, m);
		ArrayList<ArrayList<Index>> subCIs =  new ArrayList<ArrayList<Index>>();
		for(Set<Integer> subset: subSets){
			ArrayList<Index> subCI = new ArrayList<Index>();
			for(Integer idx: subset){
				subCI.add(CandIndexes.get(idx));
			}
			subCIs.add(subCI);
		}
		Map<DBTime, ArrayList<Index>> cost = new HashMap<DBTime, ArrayList<Index>>();
		for(ArrayList<Index> CIset: subCIs){
			DBTime cst = costOf(workload, CIset);
			cost.put(cst, CIset);
		}
		Map<DBTime, ArrayList<Index>> sortedCost = new TreeMap<DBTime, ArrayList<Index>>(cost);
		//TODO: Select top-k index configurations to support updates
		
		ArrayList<Index> S = null;
		for (Map.Entry entry : sortedCost.entrySet()) {
			S = new ArrayList<Index>((ArrayList<Index>)entry.getValue());
			ArrayList<Index> CIs = new ArrayList<Index>(CandIndexes);
			CIs.removeAll(S);
			DBTime minCost 	= costOf(workload, S);
			DBTime mCost 	=  new DBTime(minCost.get());
			while(S.size() < k){
				Index minI = null;
				for(Index I: CIs){
					ArrayList<Index> Sd = new ArrayList<Index>(S);
					Sd.add(I);
					DBTime costd = costOf(workload, Sd);
					if(costd.get() < minCost.get()){
						minI = I;
					}
				}
				if(mCost.get() <= minCost.get()){
					break;
				}else{
					S.add(minI);					
				}
			}			
			break; //As we need only top-most m-config	
		}
		return S; 
	}
	
	/*
	 * Called when a new query is added
	 */
	private void greedymk2(){
		
		
	}
	public DBTime changeConfig(ArrayList<Index> cur, ArrayList<Index> target) throws Exception{
		//TODO: Improve efficiency by not re-creating all indexes each time.
		ArrayList<Index> commonInd 	= new ArrayList<Index>();
		ArrayList<Index> newInd		= new ArrayList<Index>();
		for(Index tarI: target){
			boolean isNewInd = true;
			for(Index curI: cur){
				if(curI.equals(tarI.getDBName(), tarI.getTableName(), tarI.getColumnNames())){
					isNewInd = false;
					commonInd.add(tarI);
				}
			}
			if(isNewInd){
				newInd.add(tarI);
			}
		}
		simulateConfig(commonInd);
		DBTime cost = new DBTime(0.0);
		for(Index index: newInd){
			cost.add(costOfCreating(index));
		}
		return cost;
	}
	
	//Calculates the cost of creating the index at current cost
	public DBTime costOfCreating(Index index) throws Exception{
		//TODO: Improve this heuristic
		// Use different heuristics and test the algorithm
		String query = "SELECT "+index.getColumnNames()+" FROM "+ index.getTableName()+" ORDER BY "+index.getColumnNames();
		DBTime preIndexCost = ADBCon.execExplain(query);
		index.create(ADBCon);
		DBTime postIndexCost = ADBCon.execExplain(query);
		index.drop(ADBCon);
		preIndexCost.diff(postIndexCost);
		return preIndexCost;
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
