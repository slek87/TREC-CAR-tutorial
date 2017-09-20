package dev;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.unh.cs.treccar.Data;

public class Assignment2_3 
{
	private double calculateRprec(ArrayList<String> rel, ArrayList<String> ret)
	{
		int r = rel.size();
		int retsize = ret.size();
		int count = 0;
		
		for(int i=0;i<r;i++){
			if(retsize<=i)
				break;
			if(rel.contains(ret.get(i)))
				count++;
		}
		//System.out.println("count: "+count+"relsize: "+r+"retsize: "+retsize);
		return (double)count/(double)r;
	}
	
	public HashMap<String, Double> getPageRprecMap(ArrayList<Data.Page> pageList, HashMap<String, ArrayList<String>> qrelsMap, String runPath)
	{
		HashMap<String, Double> rprecScores = new HashMap<String, Double>();
		String pageid, qid, pid;
		ArrayList<String> relParaIds, retParaIds;
		for(Data.Page page:pageList)
		{
			pageid = page.getPageId();
			relParaIds = qrelsMap.get(pageid);
			retParaIds = new ArrayList<String>();
			try 
			{
				BufferedReader br = new BufferedReader(new FileReader(runPath));
				String line;
				while((line = br.readLine())!=null)
				{
					qid = line.split(" ")[0];
					if(qid.equals(pageid))
						retParaIds.add(line.split(" ")[2]);
				}
				rprecScores.put(pageid,calculateRprec(relParaIds, retParaIds));
				br.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rprecScores;
	}

}
