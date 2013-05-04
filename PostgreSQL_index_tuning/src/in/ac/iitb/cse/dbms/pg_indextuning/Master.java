package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;
import java.util.TreeSet;

public class Master {
	
	DBConnection ADBCon;
	int k = 7;
	int maxParition = 10;
	ArrayList<String> workbench;
	Partitioner pr;
	ArrayList<Index> prevallCand = new ArrayList<Index>();
	ArrayList<Index> allcand = new ArrayList<Index>(); 
	ArrayList<Index> start;
	ArrayList<Index> curCand;
	IndexTuner[] ik = new IndexTuner[k];
	ArrayList<Index>  overallSugg;
	Boolean first = true;;
	
	public Master(DBConnection adb, ArrayList<Index> startx){
		ADBCon = adb;
		workbench = new ArrayList<String>();
		pr = new Partitioner(workbench, adb, k, maxParition, 0);
		start = startx;
		overallSugg = new ArrayList<Index>(startx);
		for (int i = 0; i < k; i++){
			ik[i] = new IndexTuner(adb);
		}
	}

	public ArrayList<Index> greedymk(){
		return null;
	}
	
	public void getCand(String query){
		
	}	
	public void addQuery(String query) throws Exception{
		workbench.add(query);
		getCand(query);
		ArrayList<Index> curCand  = greedymk();
		ArrayList<ArrayList<Index>> ps = pr.makeParitions(curCand);
		deploy(ps);
		for (int i = 0; i < k; i++){
			ik[i].analyze(query);
		}
		overallSugg.clear();
		for (int i = 0; i < k; i++){
			overallSugg = union(overallSugg, ik[i].sugg);
		}
	}
	 

	public  DBTime updateCand(ArrayList<Index> ix, ArrayList<Index> dm) throws Exception{
		float res = 0.0f;
		if (first){
			return ik[0].changeConfig(start, ix);
		}
		for (int i = 0; i < k; i++){
			ArrayList<Index> inter = intersect(ik[i].allCand, ix);
			res += ik[i].w.get(ik[i].curCand.indexOf(inter)).get();			
		}
		ArrayList<Index> target = subtract(ix, prevallCand);
		ArrayList<Index> curi = subtract(intersect(start, dm), prevallCand);
		res += ik[0].changeConfig(curi, target).get();
		return new DBTime(res);
	}
	
	public ArrayList<Index> intersect(ArrayList<Index> s1, ArrayList<Index> s2){
		ArrayList<Index> result = new ArrayList<Index>();
		TreeSet<Index> s2Set = new TreeSet<Index>(s2); 
		for (Index t : s1){
			if (s2Set.contains(t)){
				result.add(t);
			}
		}
		return result;
	}
	
	public ArrayList<Index> union(ArrayList<Index> s1, ArrayList<Index> s2){
		TreeSet<Index> result = new TreeSet<Index>(); 
		result.addAll(s1); result.addAll(s2);
		return new ArrayList<Index>(result);
	}
	
	
	public ArrayList<Index> subtract(ArrayList<Index> s1, ArrayList<Index> s2){
		ArrayList<Index> result = new ArrayList<Index>();
		TreeSet<Index> s2Set = new TreeSet<Index>(s2); 
		for (Index t : s1){
			if (!s2Set.contains(t)){
				result.add(t);
			}
		}
		return result;
	}
	
	public void deploy(ArrayList<ArrayList<Index>> part) throws Exception{
		for (int i = 0; i < k; i++){
			ArrayList<ArrayList<Index>> subSet = new ArrayList<ArrayList<Index>>();
			ArrayList<DBTime> timeSet = new ArrayList<DBTime>();
			ArrayList<Index> ix = part.get(i);
			int n = ix.size();
			for (int j = 1; j < (1<<n); j++){
				ArrayList<Index> pc = new ArrayList<Index>();
				int px = j;
				for (int k = 0; k < n; k++){
					if ((px & 1) == 1){
						pc.add(ix.get(k));
					}
					px >>= 1;
				}
				subSet.add(pc); timeSet.add(updateCand(pc, ix));
			}
			ik[i].curCand = subSet; ik[i].w = timeSet; ik[i].sugg = intersect(ix, overallSugg);			
		}	
		if (first){
			first = false;
		}
	}
	
	
	
	public ArrayList<Index> suggest(ArrayList<Index> fPlus, ArrayList<Index> fMinus) throws Exception{
		for (int i = 0; i < k; i++){
			ik[i].feedback(fPlus, fMinus);
		}		
		overallSugg.clear();
		for (int i = 0; i < k; i++){
			overallSugg = union(overallSugg, ik[i].sugg);
		}
		return overallSugg;
	}
	
			
}
