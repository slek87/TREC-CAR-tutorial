package dev;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class SpearmanCoefficient {	
	
	// Constructor - Read the run files and call function to compute Spearman coefficient
	public SpearmanCoefficient()
	{
		String f_name = "output/lucene_tfidf_run";
		HashMap<String, TreeMap<String, String>> lucene_data = read_data(f_name);
		
		f_name = "output/custom_tfidf_bnnbnn_run";
		HashMap<String, TreeMap<String, String>> custom1_data = read_data(f_name);
		
		f_name = "output/custom_tfidf_lncltn_run";
		HashMap<String, TreeMap<String, String>> custom2_data = read_data(f_name);
		
		System.out.println("Correlation between Lucene and bnnbnn");
		compute_correlation(lucene_data, custom1_data);
		
		System.out.println("Correlation between Lucene and lncltn");
		compute_correlation(lucene_data, custom2_data);
	}
	
	// Function to read run file and store in required data structure (HashMap)
	public static HashMap<String, TreeMap<String, String>> read_data(String file_name)
	{
		HashMap<String, TreeMap<String, String>> query = new HashMap<String, TreeMap<String, String>>();
		
		File f = new File(file_name);
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(f));
			ArrayList<String> al = new ArrayList<>();
			String text = null;
			while((text = br.readLine()) != null)
			{
				String q = text.split(" ")[0];
				String docID = text.split(" ")[2];
				String rank = text.split(" ")[3];
				
				if (al.contains(q))
					query.get(q).put(docID, rank);
				else
				{
					TreeMap<String, String> docs = new TreeMap<String, String>();
					docs.put(docID, rank);
					query.put(q, docs);
					al.add(q);
				}
			}
		}		
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			if (br != null)
				br.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return query;
	}
	
	// Function to compute the Spearman correlation coefficient 
	public static void compute_correlation(HashMap<String, TreeMap<String, String>> lucene_data, HashMap<String, TreeMap<String, String>> custom_data) 
	{		
		double total_rank_correlation = 0.0;
		
		for (String q : lucene_data.keySet())
		{
			double d = 0;
			double d_sqr_sum = 0;
			double rank_correlation = 0.0;
			
			TreeMap<String,String> luceneRanks, customRanks;
			if (custom_data.keySet().contains(q))
			{
				luceneRanks = lucene_data.get(q);
				customRanks = custom_data.get(q);
				
				int missing_count = 0;
				int n = luceneRanks.size();
				
				for (String key : luceneRanks.keySet())
				{
					int num1 = Integer.parseInt(luceneRanks.get(key));
					if (customRanks.containsKey(key)) 
					{
						int num2 = Integer.parseInt(customRanks.get(key));
						
						d = Math.abs(num1 - num2);
						d_sqr_sum += (d * d);
					}
					else
					{
						missing_count++;
											
						d = Math.abs(num1 - (n + missing_count));
						d_sqr_sum += (d * d);
					}
				}
				
				if (n == 1)
					n = 2;
				
				rank_correlation = 1 -  (6 * d_sqr_sum / (n * ((n * n) - 1)));
				System.out.println(rank_correlation);
				total_rank_correlation += rank_correlation;
			}				
		}
		System.out.println("\nSpearman Coefficient : " + (total_rank_correlation / lucene_data.size()) + "\n");		
	}
}
