// Shubham's code

package dev;
import java.io.*;
import java.util.*;

import javax.management.Query;

import org.apache.commons.*;
import org.apache.lucene.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.DFISimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.playground.*;
import edu.unh.cs.treccar.read_data.*;
import edu.unh.cs.treccar.read_data.DeserializeData.RuntimeCborException;


public class XX 
{
		static final String INDEX_DIR = "lucene_index/dir";
		static final String CBOR_PARA = "test200/train.test200.cbor.paragraphs";
		static final String CBOR_OUTLINE = "test200/train.test200.cbor.outlines";
		static final String OUTPUT_DIR = "output";
		static final String QRELS_FILE = "test200/train.test200.cbor.article.qrels";
		
		private IndexSearcher searcher = null;
		private QueryParser qp = null;
		private boolean customScore = false;
		
		ArrayList<String> dictionary = new ArrayList<String>();
		Map<String,List<Double>> freqMap = new HashMap<String,List<Double>>();
		Map<String,List<Double>> queryTermFreqMap = new HashMap<String,List<Double>>();

		public void indexAllParas() throws CborException, IOException
		{
			Directory indexdir = FSDirectory.open((new File(INDEX_DIR)).toPath());
			IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
			conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter iw = new IndexWriter(indexdir, conf);
			for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(CBOR_PARA)))) 
				this.indexPara(iw, p);
			iw.close();
		}
		public void indexPara(IndexWriter iw, Data.Paragraph para) throws IOException 
		{
			Document paradoc = new Document();
			paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
			paradoc.add(new TextField("parabody", para.getTextOnly(), Field.Store.YES));
			iw.addDocument(paradoc);
		}
		public void createDictionary() throws CborException, IOException
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(CBOR_PARA)))) 
			{
				String para=p.getTextOnly();
				String words[] = para.split(" ");
				for(String w:words)
					if(!(dictionary.contains(w)))
						dictionary.add(w);	
			}
			for(String w:dictionary)
			{
			System.out.println(w);
			br.readLine();
			}
		}
		public List<Double> getTermFreqVector(String str)
		{
			List <Double> vector = new ArrayList<Double>();
			String words[] = str.split(" ");
			double count;
			for(int i=0;i<dictionary.size();i++)
			{
				count=0;
				for(int j=0;j<words.length;j++)
					if(dictionary.get(i).equalsIgnoreCase(words[j]))
						count++;
				if(count!=0)
					vector.add(count);
			}
			return vector;
		}
		public List<Double> getLogTermFreq(List<Double> values)
		{
			List<Double> logValues = new ArrayList<Double>(); ; 
		    for(double i:values)
		    	logValues.add((1+Math.log10(i)));
		    return logValues;
		 }
		public List<Double> getBooleanTermFreq(List<Double> values)
		{
			List<Double> boolValues = new ArrayList<Double>();
			for(double i:values)
			{
				if(i>0)
					boolValues.add(1.0);
				else
					boolValues.add(0.0);
			}
			return boolValues;
		}
		public List<Double> getAugmentedTermFreq(List<Double> values)
		{
			List<Double> augValues = new ArrayList<Double>();
			double a=0.0d,max=Collections.max(values);
			for(double i:values)
			{
				a=(0.5*i)/max;
				a+=0.5;
				augValues.add(a);
			}
			return augValues;
		}
		public List<Double> getNormalValues(List<Double> values)
		{
			double sum=0.0d,normalizationFactor=0.0d;
			List<Double> normalValues = new ArrayList<Double>(); 
			for(double i:values)
            	sum=sum+(i*i);
            normalizationFactor=Math.sqrt(sum);
            for(double i: values)
            {
            	double num=i/normalizationFactor;
            	normalValues.add(num);
            }
            return normalValues;
		}
		public void createDocumentVectors(String type) throws CborException, IOException
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			List<Double> values;
			for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(CBOR_PARA)))) 
			{
				String para=p.getTextOnly();
				freqMap.put(p.getParaId(), getTermFreqVector(para));
			}
			
	        switch(type)
	        {
	        	case "lnc": //Map<String,List<Double>> copy = new HashMap<String,List<Double>>();
	        				//copy.putAll(freqMap);
	        				//List<Double> values;
	        				values=new ArrayList<Double>();
	        				for (Map.Entry<String, List<Double>> entry : freqMap.entrySet()) 
	        			    {
	        					    //normalValues=new ArrayList<Double>(); 
	        			            String key = entry.getKey();
	        			            values = entry.getValue();
	        			            System.out.println("Key = " + key);
	        			            System.out.println("Values = " + values);
	        			            values = getLogTermFreq(values);
	        			            System.out.println("Log values="+values);
	        			            values = getNormalValues(values);
	        			            System.out.println("Normalized values="+values);
	        			            System.out.println("press any key-->");
	        			            br.readLine();
	        			            
	        			           // copy.replace(key, values);
	        			     }
	        				//return values;
	        				 break;
	        	case "bnn" ://Map<String,List<Double>> copy = new HashMap<String,List<Double>>();
							//copy.putAll(freqMap);
							//List<Double> values;
	        				values=new ArrayList<Double>();
							for (Map.Entry<String, List<Double>> entry : freqMap.entrySet()) 
							{
								//normalValues=new ArrayList<Double>();
								String key = entry.getKey();
								values = entry.getValue();
								System.out.println("Key = " + key);
								System.out.println("Values = " + values);
								values = getBooleanTermFreq(values);
								System.out.println("Boolean values="+values);
								System.out.println("press any key-->");
								br.readLine();
								
								//copy.replace(key, values);
							}
							//return values;
							break;
	        	case "anc": //Map<String,List<Double>> copy = new HashMap<String,List<Double>>();
							//copy.putAll(freqMap);
							//List<Double> values;
	        				values=new ArrayList<Double>();
							for (Map.Entry<String, List<Double>> entry : freqMap.entrySet()) 
							{
								//normalValues=new ArrayList<Double>(); 
								String key = entry.getKey();
								values = entry.getValue();
								System.out.println("Key = " + key);
								System.out.println("Values = " + values);
								values = getAugmentedTermFreq(values);
								System.out.println("Augmented values="+values);
								values = getNormalValues(values);
								System.out.println("Normalized values="+values);
								System.out.println("press any key-->");
								br.readLine();
								
								//copy.replace(key, values);
							}
							//return values;
							break;
	        }
		}
		public ArrayList<Data.Page> getPageListFromPath(String path)
		{
			ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
			try 
			{
				FileInputStream fis = new FileInputStream(new File(path));
				for(Data.Page page: DeserializeData.iterableAnnotations(fis)) 
					pageList.add(page);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} 
			catch (RuntimeCborException e) 
			{
				e.printStackTrace();
			}
			return pageList;
		}
		public int getTotalDocs()throws IOException
		{
			
			if (searcher == null)
				searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIR).toPath()))));
			IndexReader reader = searcher.getIndexReader();
			return reader.numDocs();
			
		}
		public long getDocumentFrequency(int docID,String FIELD)throws IOException
		{
			//DefaultSimilarity similarity = new DefaultSimilarity();
			BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
			long docFreq=0;
			IndexReader reader = searcher.getIndexReader();
			IndexReaderContext context = searcher.getTopReaderContext();
			CollectionStatistics collectionStats = searcher.collectionStatistics(FIELD);
			long totalDocCount = collectionStats.docCount();

			Terms termVector = reader.getTermVector(docID, FIELD);
			TermsEnum iterator = termVector.iterator();

			while (true) 
			{
			    BytesRef ref = iterator.next();
			    if (ref == null) 
			    {
			        break;
			    }

			    long termFreq = iterator.totalTermFreq();
			    

			    Term term = new Term(FIELD, ref);
			    TermContext termContext = TermContext.build(context, term);

			    TermStatistics termStats = searcher.termStatistics(term, termContext);
			    docFreq = termStats.docFreq();
			}
			 return docFreq;   

		}

		public void createQueryVector(String type) throws IOException, ParseException 
		{
			ArrayList<Data.Page> pagelist = getPageListFromPath(XX.CBOR_OUTLINE);
			Query q;

			if (qp == null) 
			{
				qp = new QueryParser("parabody", new StandardAnalyzer());
			}
			/*for(Data.Page page:pagelist)
			{
				String query=page.getPageName();
				System.out.println("Query: " + query);
				queryTermFreqMap.put(query, getTermFreqVector(query));
			}*/
			switch(type)
			{
				case "ltn" :List<Long> docFreq = new ArrayList<Long>();
							int count=0;
							for(Data.Page page:pagelist)
							{
								String query=page.getPageName();
								System.out.println("Query: " + query);
								List<Double> vector = getTermFreqVector(query);
								vector = getLogTermFreq(vector);
								int n = getTotalDocs();
								String words[]=query.split(" ");
								try {
									for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(CBOR_PARA))))
									{
										count++;
										for(String w:words)
											docFreq.add(getDocumentFrequency(count,w));
										
									}
								} catch (CborException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								for(long i:docFreq)
								{
									System.out.println(i);
									// br.readLine();
								}
									
							}
					
					
			}
		}
					
		public static void main(String[] args) 
		{
			XX a = new XX();
			
			try 
			{
				a.indexAllParas();
				a.createDictionary();
				a.createDocumentVectors("anc");
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}	
			
		}

	}


