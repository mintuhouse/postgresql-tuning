package in.ac.iitb.cse.dbms.pg_indextuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

public class Partitioner {
	
	ArrayList<String> workBench;
	DBConnection ADBCon;
	int k;
	int maxPartition;
	int prev;
	
	public Partitioner(ArrayList<String> wb, DBConnection adb, int k1, int mp, int previ){
		workBench = wb; ADBCon = adb; k = k1; maxPartition = mp; prev = previ;
	}
	
	
	private class Edge implements Comparable<Edge> {
		public int a;public  int b; public float wt;
		
		public Edge(int ax, int bx, float wtx){
			a = ax; bx = b; wt = wtx;
		}

		@Override
		public int compareTo(Edge o) {
			// TODO Auto-generated method stub
			if (this.wt > o.wt){
				return -1;
			} else if (this.wt < o.wt){
				return 1;
			} else if (this.a == o.a && this.b == o.b){
				return 0;
			} else {
				if (this.a < o.a){
					return -1; 
				} else if (this.a > o.a){
					return 1;
				} else if (this.b < o.b){
					return -1;
				} else {
					return 1;
				}									
			}
		}
		
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
	
	public float calculateWt(Index a, Index b, ArrayList<Index> cand) throws Exception{		
		
		float res = 0.0f;
		for (int i = 0; i < prev && i < cand.size(); i++){
			String query = workBench.get(workBench.size() - i - 1);
			float denom = whatIf(query, cand).get();
			cand.remove(a);
			float num1 = denom - whatIf(query, cand).get();
			cand.remove(b); cand.add(a);
			float num2 = denom - whatIf(query,cand).get();
			cand.add(b);
			res += (Math.abs(num1+num2 / denom) / Math.sqrt(i + 1.0));			
		}
		
		return res;
	}
	
	private int parent(int x, int[] ed){
		while (x != ed[x]){
			int temp = ed[x];
			ed[x] = ed[ed[x]];
			x = temp;
		}
		return x;
	}
	
	public ArrayList<ArrayList<Index> > makeParitions(ArrayList<Index> cand) throws Exception{
		HashMap<Index, Integer> mp = new HashMap<Index, Integer>();
		int cnt = 0;
		for (Index ix : cand){
			mp.put(ix, cnt); cnt++;
		}
	
		int n = cand.size();
		
		
		ArrayList<Edge> edgeList = new ArrayList<Edge>();
		
		for (int i = 0; i < n; i++){
			for (int j = i + 1; j < n; j++){
				float x = calculateWt(cand.get(i), cand.get(j), cand);
				edgeList.add(new Edge(i, j, x));
			}
		}
		
		int[] map = new int[cand.size()];
		int[] wt = new int[cand.size()];
		for (int i = 0; i < cand.size(); i++){
			map[i] = i;
		}
		
		Collections.sort(edgeList);
		int curEdge = 0;
		int numpar = edgeList.size();
		
		while (curEdge < edgeList.size() && numpar > k){
			int t1, t2;
			t1 = parent(edgeList.get(curEdge).a, map);
			t2 = parent(edgeList.get(curEdge).b, map);
			
			if (t1 != t2){
				if (wt[t1] < wt[t2] && wt[t2] <	maxPartition){
					map[t1] = t2; wt[t2] += wt[t1]; numpar--;
				} else if (wt[t2] < wt[t1] && wt[t1] < maxPartition){
					map[t2] = t1; wt[t1] += wt[t2]; numpar--;
				}
			}
		}
		// calculate the edges
		int[] par = new int[cand.size()];
		TreeSet<Integer> st = new TreeSet<Integer>();
		for (int i = 0; i < cand.size(); i++){
			st.add(parent(i, map));
			par[i] = parent(i, map);
		}
		ArrayList<ArrayList<Index> > res = new ArrayList<ArrayList<Index> >();
		for (int ip : st){
			ArrayList<Index> tmp = new ArrayList<Index>();
			for (int i = 0; i < cand.size(); i++){
				if (par[i] == ip){
					tmp.add(cand.get(i));
				}
			}
			res.add(tmp);
		}				
		return res;		
	}
	

}