package dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryparser.classic.ParseException;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

public class Assignment5 {
	Assignment4 a4Laplace, a4JMS, a4Dir;
	Assignment3 lnc_ltn;
	ArrayList<Data.Page> pagelist;
	public static final String RLOUTPUT = "output_lm/ranklib_output";
	public void doStuff() throws ParseException{
		Assignment3 a3 = new Assignment3();
		a4Laplace = new Assignment4(1);
		a4JMS = new Assignment4(2);
		a4Dir = new Assignment4(3);
		lnc_ltn = new Assignment3();
		try {
			a4Laplace.indexAllParas();
			a4JMS.indexAllParas();
			a4Dir.indexAllParas();
			pagelist = a3.getPageListFromPath(Assignment3.CBOR_OUTLINE);
			for(Data.Page p:pagelist){
				a4Laplace.rankParas(p, 10, "a5laplace");
				a4JMS.rankParas(p, 10, "a5jms");
				a4Dir.rankParas(p, 10, "a5dir");
				lnc_ltn.rankParas(p, 10, 1);
			}
		} catch (CborException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int getRank(String q, String d, String run){
		int rank = -1;
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(run));
			String line;
			String[] lineData = new String[6];
			while((line = br.readLine()) != null){
				lineData = line.split(" ");
				if(lineData[0].equals(q) && lineData[2].equals(d)){
					rank = Integer.parseInt(lineData[3])+1;
					break;
				}
			}
			br.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		return rank;
	}
	
	public ArrayList<String> produceRankLibFile(String[] runfiles) throws FileNotFoundException, CborException {
		String qid, ranklibString, fetValString;
		double[] v = new double[runfiles.length];
		int rank, target = 0;
		ArrayList<String> rlibStrings = new ArrayList<String>();
		for(Data.Page p:pagelist){
			qid = p.getPageId();
			System.out.println(qid);
			for(Data.Paragraph para:DeserializeData.iterableParagraphs(new FileInputStream
					(new File(Assignment3.CBOR_FILE)))){
				fetValString = "";
				target = 0;
				for(int i=0; i<runfiles.length; i++){
					rank = this.getRank(qid, para.getParaId(), runfiles[i]);
					if(rank > 0){
						v[i] = 1.0/(double)rank;
						target = 1;
					}
					else{
						v[i] = 0;
					}
					fetValString = fetValString+" "+i+":"+v[i];
				}
				if(target > 0){
					ranklibString = target+" qid:"+(qid+fetValString)+" #"+para.getParaId();
					rlibStrings.add(ranklibString);
					System.out.println(ranklibString);
				}
			}
		}
		return rlibStrings;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Assignment5 a5 = new Assignment5();
		String[] runs = {"output_lm/a5laplace", "output_lm/a5jms", "output_lm/a5dir", "output_lm/lnc_ltn"};
 		try {
 			FileWriter fw = new FileWriter(Assignment5.RLOUTPUT, true);
			a5.doStuff();
			for(String s:a5.produceRankLibFile(runs)){
				fw.write(s+"\n");
			}
			fw.close();
		} catch (ParseException | CborException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
