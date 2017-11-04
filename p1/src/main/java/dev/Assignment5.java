package dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryparser.classic.ParseException;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

public class Assignment5 {
	Assignment4 a4Laplace, a4JMS, a4Dir, a4LncLtn, a4BnnBnn;
	Assignment3 lnc_ltn, bnn_bnn;
	//Assignment3 bnn_bnn;
	
	ArrayList<Data.Page> pagelist;
	HashMap<String, ArrayList<String>> relevanceMap = getRelevanceMapFromQrels("output_lm/article.qrels");
	public static final String RLOUTPUT = "output_lm/ranklib_output";
	
	public HashMap<String, ArrayList<String>> getRelevanceMapFromQrels(String qrelsPath){
		HashMap<String, ArrayList<String>> relMap = new HashMap<String, ArrayList<String>>();
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(qrelsPath));
			String line, paraid, pageid;
			String[] lineData = new String[4];
			while((line = br.readLine()) != null){
				pageid = line.split(" ")[0];
				paraid = line.split(" ")[2];
				if(relMap.keySet().contains(pageid)){
					relMap.get(pageid).add(paraid);
				}
				else{
					ArrayList<String> paralist = new ArrayList<String>();
					paralist.add(paraid);
					relMap.put(pageid, paralist);
				}
			}
			br.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		return relMap;
	}
	
	public ArrayList<String> getUniqueParaIds(String[] runfiles){
		ArrayList<String> paraids = new ArrayList<String>();
		for(int i=0; i<runfiles.length; i++){
			BufferedReader br;
			try{
				br = new BufferedReader(new FileReader(runfiles[i]));
				String line, paraid;
				String[] lineData = new String[6];
				while((line = br.readLine()) != null){
					paraid = line.split(" ")[2];
					if(!paraids.contains(paraid))
						paraids.add(paraid);
				}
				br.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return paraids;
	}
	public void doStuff() throws ParseException{
		Assignment3 a3 = new Assignment3();
		a4Laplace = new Assignment4(1);
		a4JMS = new Assignment4(2);
		a4Dir = new Assignment4(3);
		lnc_ltn = new Assignment3();
		bnn_bnn = new Assignment3();
		try {
			a4Laplace.indexAllParas();
			a4JMS.indexAllParas();
			a4Dir.indexAllParas();
			lnc_ltn.indexAllParas(2);
			bnn_bnn.indexAllParas(3);
			pagelist = a3.getPageListFromPath(Assignment3.CBOR_OUTLINE);
			for(Data.Page p:pagelist){
				a4Laplace.rankParas(p, 10, "a5laplace");
				a4JMS.rankParas(p, 10, "a5jms");
				a4Dir.rankParas(p, 10, "a5dir");
				lnc_ltn.rankParas(p, 10, 2, "output_lm/a5lncltn");
				bnn_bnn.rankParas(p, 10, 3, "output_lm/a5bnnbnn");
				//bnn_bnn.rankParas(p, 10, "a5bnn_bnn");
			}
		} catch (CborException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public HashMap<String, ArrayList<String>> getRunFileMap(String runfile){
		HashMap<String, ArrayList<String>> runfileMap = new HashMap<String, ArrayList<String>>();
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(runfile));
			String line;
			String[] lineData = new String[6];
			while((line = br.readLine()) != null){
				lineData = line.split(" ");
				if(runfileMap.keySet().contains(lineData[0]))
					runfileMap.get(lineData[0]).add(lineData[2]);
				else{
					ArrayList<String> curr = new ArrayList<String>();
					curr.add(lineData[2]);
					runfileMap.put(lineData[0], curr);
				}
			}
			br.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		return runfileMap;
	}
	
	public int getRank(String q, String d, HashMap<String, ArrayList<String>> map){
		
		return map.get(q).indexOf(d);
	}
	
	public int getRank(int d, String[] ranking){
		int rank = -1, count = 1;
		for(int i=0; i<ranking.length; i++){
			if(ranking[i].equals("D"+d)){
				rank = count;
				break;
			}
			count++;
		}
		return rank;
	}
	
	public ArrayList<String> produceRankLibFile(ArrayList<HashMap<String, ArrayList<String>>> runMaps, String[] runfiles) throws FileNotFoundException, CborException {
		String qid, ranklibString, fetValString;
		double[] v = new double[runMaps.size()];
		int rank, target = 0;
		ArrayList<String> rlibStrings = new ArrayList<String>();
		ArrayList<String> uniqueParaIds = new ArrayList<String>();
		uniqueParaIds = this.getUniqueParaIds(runfiles);
		for(Data.Page p:pagelist){
			qid = p.getPageId();
			System.out.println(qid);
			// Do not take all the paras, take only those relevant to the current page
			for(String paraid:uniqueParaIds){
				fetValString = "";
				if(this.relevanceMap.get(qid).contains(paraid))
					target = 1;
				else
					target = -1;
				for(int i=0; i<runfiles.length; i++){
					//for(int i=1; i<=runfiles.length; i++){
					rank = -1;
					if(runMaps.get(i).keySet().contains(qid))
						rank = this.getRank(qid, paraid, runMaps.get(i));
					if(rank > 0)
						v[i] = 1.0/(double)rank;
					else
						v[i] = 0;
					fetValString = fetValString+" "+(i+1)+":"+v[i];
				}
				ranklibString = target+" qid:"+(qid+fetValString)+" #"+paraid;
				rlibStrings.add(ranklibString);
				
				System.out.println(ranklibString);
			}
		}
		return rlibStrings;
	}
	
	public ArrayList<String> doAssignment1(String[][] rankings){
		ArrayList<String> rlibStrings = new ArrayList<String>();
		String qid, ranklibString, fetValString;
		int noOfDocs = 12, noOfFeatures = 4, rank = 0, target = 0;
		double[] v = new double[noOfFeatures];
		for(int i=1; i<=noOfDocs; i++){
			target = 0;
			fetValString = "";
			for(int j=1; j<=noOfFeatures; j++){
				rank = this.getRank(i, rankings[j-1]);
				if(rank > 0){
					v[j-1] = 1.0/(double)rank;
					target = 1;
				}
				else{
					v[j-1] = 0;
				}
				fetValString = fetValString+" "+j+":"+v[j-1];
			}
			if(target > 0){
				ranklibString = target+" qid:q1"+fetValString+" #D"+i;
				rlibStrings.add(ranklibString);
				System.out.println(ranklibString);
			}
		}
		return rlibStrings;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Assignment5 a5 = new Assignment5();
		ArrayList<HashMap<String, ArrayList<String>>> runMaps = 
				new ArrayList<HashMap<String,ArrayList<String>>>();
		String[][] rankings = {{"D1","D2","D3","D4","D5","D6"},
				{"D2","D5","D6","D7","D8","D9","D10","D11"},
				{"D1","D2","D5"},
				{"D1","D2","D8","D10","D12"}
		};
		
		String[] runs = {"output_lm/a5laplace", "output_lm/a5jms", "output_lm/a5dir",
				"output_lm/a5lncltn", "output_lm/a5bnnbnn"};
		for(int i=0; i<runs.length; i++)
			runMaps.add(a5.getRunFileMap(runs[i]));
		//String[] runs = {"output_lm/a5laplace", "output_lm/a5jms", "output_lm/a5dir", "output_lm/lnc_ltn", "output_lm/a5bnn_bnn"};
 		try {
 			
 			FileWriter fw = new FileWriter(Assignment5.RLOUTPUT, true);
			a5.doStuff();
			//a5.doAssignment1(rankings);
			
			for(String s:a5.produceRankLibFile(runMaps, runs)){
				fw.write(s+"\n");
			}
			fw.close();
			
		} catch (IOException | ParseException | CborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}