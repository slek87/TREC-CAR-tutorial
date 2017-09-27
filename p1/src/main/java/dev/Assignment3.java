package dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;
import edu.unh.cs.treccar.read_data.DeserializeData.RuntimeCborException;

public class Assignment3 {
// test comment
	static final String INDEX_DIR = "lucene_index/dir";
	static final String CBOR_FILE = "test200/train.test200.cbor.paragraphs";
	static final String CBOR_OUTLINE = "test200/train.test200.cbor.outlines";
	static final String OUTPUT_DIR = "output";
	static final String LUCENE_OUT = "lucene_tfidf_run";
	static final String CUSTOM_OUT = "custom_tfidf_bnnbnn_run";
	
	private IndexSearcher is = null;
	private QueryParser qp = null;
	private boolean customScore = true; // make it true to use the custom tfidf scores

	public void indexAllParas(int tfidf) throws CborException, IOException {
		Directory indexdir = FSDirectory.open((new File(INDEX_DIR)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		if(customScore)
			conf.setSimilarity(this.getCustomSimilarity(tfidf));
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(CBOR_FILE)))) {
			this.indexPara(iw, p);
		}
		iw.close();
	}
	public SimilarityBase getCustomSimilarity(int tfidf){
		SimilarityBase mySimiliarity = new SimilarityBase() {
			
			protected float score(BasicStats stats, float freq, float docLen) {
				float freqScore = 0;
				switch(tfidf){
				case 1:
					freqScore = 1+(float)Math.log(freq);
				case 2:
					freqScore = (1+(float)Math.log(freq))*
					((float)Math.log(stats.getNumberOfDocuments()/stats.getDocFreq()));
				case 3:
					if(freq>0)
						freqScore = 1;
					else
						freqScore = 0;
				//case 4:
					
				//case 5:
					
				}
				return freqScore;
			}

			@Override
			public String toString() {
				return null;
			}
		};
		return mySimiliarity;
	}

	public void indexPara(IndexWriter iw, Data.Paragraph para) throws IOException {
		Document paradoc = new Document();
		paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
		paradoc.add(new TextField("parabody", para.getTextOnly(), Field.Store.YES));
		iw.addDocument(paradoc);
	}
	/*

	public void doSearch(String qstring, int n) throws IOException, ParseException {
		if ( is == null ) {
			is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIR).toPath()))));
		}
		
		if ( customScore ) {
			SimilarityBase mySimiliarity = new SimilarityBase() {
				protected float score(BasicStats stats, float freq, float docLen) {
					return freq;
				}

				@Override
				public String toString() {
					return null;
				}
			};
			is.setSimilarity(mySimiliarity);
		}

		/*
		 * The first arg of QueryParser constructor specifies which field of document to
		 * match with query, here we want to search in the para text, so we chose
		 * parabody.
		 * 
		 */
	/*
		if (qp == null) {
			qp = new QueryParser("parabody", new StandardAnalyzer());
		}

		Query q;
		TopDocs tds;
		ScoreDoc[] retDocs;
		
		System.out.println("Query: " + qstring);
		q = qp.parse(qstring);
		tds = is.search(q, n);
		retDocs = tds.scoreDocs;
		Document d;
		for (int i = 0; i < retDocs.length; i++) {
			d = is.doc(retDocs[i].doc);
			System.out.println("Doc " + i);
			System.out.println("Score " + tds.scoreDocs[i].score);
			System.out.println(d.getField("paraid").stringValue());
			System.out.println(d.getField("parabody").stringValue() + "\n");
			
		}
	}

	public void customScore(boolean custom) throws IOException {
		customScore = custom;
	}
	*/
	public void rankParas(Data.Page page, int n, int tfidf) throws IOException, ParseException {
		if ( is == null ) {
			is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIR).toPath()))));
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
		
		System.out.println("Query: " + page.getPageName());
		q = qp.parse(page.getPageName());
		if(customScore)
			is.setSimilarity(this.getCustomSimilarity(tfidf));
		tds = is.search(q, n);
		retDocs = tds.scoreDocs;
		Document d;
		ArrayList<String> runStringsForPage = new ArrayList<String>();
		String method = "lucene";
		String outfile = Assignment3.LUCENE_OUT;
		if(customScore){
			method = "custom";
			outfile = Assignment3.CUSTOM_OUT;
		}
		for (int i = 0; i < retDocs.length; i++) {
			d = is.doc(retDocs[i].doc);
			System.out.println("Doc " + i);
			System.out.println("Score " + tds.scoreDocs[i].score);
			//System.out.println(d.getField("paraid").stringValue());
			//System.out.println(d.getField("parabody").stringValue() + "\n");
			// runFile string format $queryId Q0 $paragraphId $rank $score $teamname-$methodname
			String runFileString = page.getPageId()+" Q0 "+d.getField("paraid").stringValue()
					+" "+i+" "+tds.scoreDocs[i].score+" team2-"+method;
			runStringsForPage.add(runFileString);
		}
		
		FileWriter fw = new FileWriter(Assignment3.OUTPUT_DIR+"/"+outfile, true);
		for(String runString:runStringsForPage)
			fw.write(runString+"\n");
		fw.close();
	}
	
	//public HashMap<String, double> getPageRprecMap();
	
	public ArrayList<Data.Page> getPageListFromPath(String path){
		ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			for(Data.Page page: DeserializeData.iterableAnnotations(fis))
				pageList.add(page);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeCborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageList;
	}

	public static void main(String[] args) {
		Assignment3 a = new Assignment3();
		
		try {
			a.indexAllParas(3);
			ArrayList<Data.Page> pagelist = a.getPageListFromPath(Assignment3.CBOR_OUTLINE);
			for(Data.Page page:pagelist){
				a.rankParas(page, 100, 3);
			}
			
		} catch (CborException | IOException | ParseException e) {
			e.printStackTrace();
		}
		
		
		
		
	}

}
