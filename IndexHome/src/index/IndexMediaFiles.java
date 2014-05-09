package index;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexMediaFiles {
	public static void main(String[] args) throws IOException, ParseException {
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		KeywordAnalyzer analyzer = new KeywordAnalyzer();

		// 1. create the index
		Directory index = FSDirectory.open(new File("/media/ananth/DATA/MediaIndex"));

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41,
				analyzer);

		IndexWriter w = new IndexWriter(index, config);
		indexDocs(w, new File("/media/ananth/FreeAgent Drive"));
		System.out.println("Done !");
		w.close();
	}

	private static void addDoc(IndexWriter w, String title, String path,
			String mimetype) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("title", title.toLowerCase(), Field.Store.YES));

		// use a string field for isbn because we don't want it tokenized
		doc.add(new StringField("path", path.toLowerCase(), Field.Store.YES));
		doc.add(new StringField("execpath", path, Field.Store.YES));
		doc.add(new StringField("mimetype", mimetype.toLowerCase(),
				Field.Store.YES));
		w.addDocument(doc);
	}

	static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						try {
							if (Files.probeContentType(dir.toPath()).contains(
									"audio")
									|| Files.probeContentType(dir.toPath())
											.contains("video")|| Files.probeContentType(dir.toPath())
											.contains("directory"))
								return true;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return false;
					}
				});
				addDoc(writer, file.getName(), file.getAbsolutePath(),
						"inode/directory");
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {
				String mime = Files.probeContentType(file.toPath());
				addDoc(writer, file.getName(), file.getAbsolutePath(), mime);
			}
		}
	}
}
