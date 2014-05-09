package runapps;

import index.Search;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import ui.MultiButton;

import org.apache.lucene.queryparser.classic.ParseException;

public class RunFiles {

	/**
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 */
	public String json="";
	public RunFiles(String search, String type, String path) throws IOException, ParseException, org.json.simple.parser.ParseException {
		// TODO Auto-generated method stub
		Search s = new Search(search, type, path);
		HashMap<String, String> files = s.getFiles();
		if(files.size()==0){
			Runtime.getRuntime().exec(new String[]{"notify-send","No file by the name "+search});
			return;
		}
		if (files.size() == 1) {
			File fl = new File(files.keySet().iterator()
					.next());
			if(!fl.isDirectory()){
			Desktop dt = Desktop.getDesktop();
		    dt.open(new File(files.keySet().iterator()
					.next()));
			}else{
				Runtime.getRuntime().exec("nemo "+files.keySet().iterator()
					.next().replace(" ", "\\ "));
			}
		}else{
			json="{\"message\":\"Which do you mean?\",\"options\":";
			json+="[";
			int i=0;
			int cnt=files.size();
			for(String key:files.keySet()){
				if(i==cnt-1){
					json+="\""+key+"\"]}";break;
				}
				json+="\""+key+"\",";
				i++;
			}
			System.out.println(json);
			MultiButton.main(new String[]{json});
		}
	}

}
