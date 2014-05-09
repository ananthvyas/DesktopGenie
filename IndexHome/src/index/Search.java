package index;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import speech.SpeechToText;

public class Search {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	private HashMap<String, String> fileToMime;

	public HashMap<String, String> getFiles() {
		return fileToMime;
	}

	public Search(String name, String mime, String path) throws IOException,
			ParseException {
		// TODO Auto-generated method stub
		fileToMime = new HashMap<String, String>();
		Directory index = FSDirectory
				.open(new File(SpeechToText.indexPath));
		Query q = new WildcardQuery(new Term("title", name + "*"));
		Query p = new WildcardQuery(new Term("mimetype", "*" + mime + "*"));
		if (path.endsWith("/") || path.equals(""))
			path += "*";
		else
			path += "/*";
		Query s = new WildcardQuery(new Term("path", path));
		BooleanQuery a = new BooleanQuery();
		a.add(q, Occur.MUST);
		a.add(p, Occur.MUST);
		a.add(s, Occur.MUST);
		// 3. search
		int hitsPerPage = 20;
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);
		searcher.search(a, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		// System.out.println(hits.length);
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;

			Document d = searcher.doc(docId);
			// System.out.println(d.get("title"));
			fileToMime.put(d.get("execpath"), d.get("mimetype"));
		}

		// reader can only be closed when there
		// is no need to access the documents any more.
		reader.close();
	}

}
