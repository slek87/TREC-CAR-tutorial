package dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.lucene54.Lucene54DocValuesFormat;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity.LMStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

public class Assignment4 {
	
	private IndexSearcher is = null;
	private QueryParser qp = null;
	private static int SMOOTHING = 3;
	private static final String OUTPUT_DIR = "output_lm";
	
	public SimilarityBase getCustomSimilarity(int smoothing, int vocabSize, float lambda, float mu){
		LMSimilarity mySimiliarity = new LMSimilarity() {
			
			protected float score(BasicStats stats, float freq, float docLen) {
				float score = 0;
				switch(smoothing){
				case 1://Laplace
					score = getLaplaceSmoothedScore(freq, docLen, vocabSize);
					System.out.println("Vocab size is: "+vocabSize+" right?");
					break;
				case 2://Jelinek-Mercer
					score = getJMSmoothedScore(freq, docLen, lambda, (LMSimilarity.LMStats)stats);
					break;
				case 3://Dirichlet
					score = getDirichletSmoothedScore(freq, docLen, mu, (LMSimilarity.LMStats)stats);
					break;
				//case 4:
					
				//case 5:
				}
				return score;
			}

			@Override
			public String toString() {
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		return mySimiliarity;
	}
	
	public float getLaplaceSmoothedScore(float termFreq, float docLength, long vocabSize){
		float score = 0;
		score = (termFreq+1)/(docLength+vocabSize);
		return score;
	}
	
	public float getJMSmoothedScore(float termFreq, float docLength, float lambda, LMSimilarity.LMStats stats){
		float score = 0;
		score = lambda*termFreq/docLength+(1-lambda)*stats.getCollectionProbability();
		return score;
	}
	
	public float getDirichletSmoothedScore(float termFreq, float docLength, float mu, LMSimilarity.LMStats stats){
		float score;
		score = (termFreq+mu*stats.getCollectionProbability()) / (docLength + mu);
		return score;
	}
	
	public void indexAllParas() throws CborException, IOException {
		Directory indexdir = FSDirectory.open((new File(Assignment3.INDEX_DIR)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		//conf.setSimilarity(this.getCustomSimilarity(Assignment4.SMOOTHING, vocabSize, 0.9f, 1000));
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);
		Assignment3 a3 = new Assignment3();
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(
				new FileInputStream(new File(Assignment3.CBOR_FILE)))) {
			a3.indexPara(iw, p);
		}
		iw.close();
	}
	
	public int getVocabSize(IndexReader rd){
		int vsize = 0;
		
		return vsize;
	}
	
	public void rankParas(Data.Page page, int n) throws IOException, ParseException {
		if ( is == null ) {
			is = new IndexSearcher(DirectoryReader.open(FSDirectory.open(
					(new File(Assignment3.INDEX_DIR).toPath()))));
		}
		

		/*
		 * The first arg of QueryParser constructor specifies which field of document to
		 * match with query, here we want to search in the para text, so we chose
		 * parabody.
		 * 
		 */
		if (qp == null) {
			qp = new QueryParser("parabody", new StandardAnalyzer());
		}
		

		Query q;
		TopDocs tds;
		ScoreDoc[] retDocs;
		int vocabSize;
		
		System.out.println("Query: " + page.getPageName());
		q = qp.parse(page.getPageName());
		vocabSize = getVocabSize(is.getIndexReader());
		is.setSimilarity(this.getCustomSimilarity(Assignment4.SMOOTHING, vocabSize, 0.9f, 1000));
		tds = is.search(q, n);
		retDocs = tds.scoreDocs;
		Document d;
		ArrayList<String> runStringsForPage = new ArrayList<String>();
		String method = "customLM"+Assignment4.SMOOTHING;
		String outfile = Assignment3.CUSTOM_OUT;
		
		for (int i = 0; i < retDocs.length; i++) {
			d = is.doc(retDocs[i].doc);
			System.out.println("Doc " + i);
			System.out.println("Score " + tds.scoreDocs[i].score);
			// runFile string format $queryId Q0 $paragraphId $rank $score $teamname-$methodname
			String runFileString = page.getPageId()+" Q0 "+d.getField("paraid").stringValue()
					+" "+i+" "+tds.scoreDocs[i].score+" team2-"+method;
			runStringsForPage.add(runFileString);
		}
		
		FileWriter fw = new FileWriter(Assignment4.OUTPUT_DIR+"/"+outfile, true);
		for(String runString:runStringsForPage)
			fw.write(runString+"\n");
		fw.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Assignment4 a4 = new Assignment4();
		Assignment3 a3 = new Assignment3();
		try {
			a4.indexAllParas();
			ArrayList<Data.Page> pagelist = a3.getPageListFromPath(Assignment3.CBOR_OUTLINE);
			for(Data.Page p:pagelist){
				a4.rankParas(p, 50);
			}
			
		} catch (CborException | IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
