package dev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
5 Graduate students: NDCG@20
In a programming language of your choice, implement the NDCG@20 measure as described in
the lecture. Don't forget to normalize with the ideal ranking loaded from the qrel file (first all
relevant documents, then the rest).
In your report, include the NDCG@20 eval score obtained by each of the scoring functions. Are you obtaining the same NDCG@20 eval score as trec eval? If not, explore how
your NDCG@20 implementation is different from the one in trec eval, which is based on this
paper by Jarvelin and Kekalainen (ACM ToIS v. 20, pp. 422-446, 2002): http://www.sis.uta.fi/infim/julkaisut/fire/KJJK-nDCG.pdf
 */
public class Assignment2_5 {
	
	private HashMap<String, ArrayList<String>> mapOut;
	private HashMap<String, ArrayList<String>> mapRel;
	
	public void initNDCG(HashMap<String, ArrayList<String>> output, HashMap<String, ArrayList<String>> relevent) {
		mapOut = output;
		mapRel = relevent;
	}
	
	public float getNDCG20(String docId ){
		ArrayList<String> arrOut = mapOut.get(docId);
		ArrayList<String> arrRel = mapRel.get(docId);
		
		float dcg = 0;
		float idcg = 0;
		int rank = 0;
		String paraId;
		
		// calculating dcg@20
		for (int i = 0; i < 20; i++ ) {
			if (arrOut == null || arrRel == null)
				continue;
			rank++;
			paraId = arrOut.get(i);
			if ( arrRel.contains(paraId)) {
				dcg += 1 / ( Math.log10(rank+1)/Math.log10(2) );
			}
			
			if ( i > arrRel.size()) {
				break;
			}
		}
		
		rank = 0;
		// calculating idcg
		for (int i = 0; i < 20; i++ ) {
			if (arrOut == null || arrRel == null)
				continue;
			paraId = arrOut.get(i);
			if ( arrRel.contains(paraId)) {
				rank++;
				idcg += 1 / ( Math.log10(rank+1)/Math.log10(2) );
			}
			
			if ( i > arrRel.size()) {
				break;
			}
		}
		
		return dcg/idcg;
	}
	
	
}
