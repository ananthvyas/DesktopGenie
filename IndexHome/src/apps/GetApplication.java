package apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class GetApplication {

	private String defaultApps = "/home/ananth/.local/share/applications/mimeapps.list";
	private String fileAssocDir = "/usr/share/applications/";
	private HashMap<String, String> mimetoApp = new HashMap<String, String>();
	
	public HashMap<String, String> getFileAssocs(){
		return mimetoApp;
	}
	public GetApplication() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(new File(
				defaultApps)));
		String line = "";
		while (!br.readLine().contains("Default")) {

		}
		while (!(line = br.readLine()).contains("Added Associations")) {
			if (!line.trim().equals("")) {
				String[] assocs = line.split("=");
				String mime = assocs[0];
				String file = fileAssocDir + assocs[1];
				BufferedReader brd = new BufferedReader(new FileReader(
						new File(file)));
				String app = "";
				String exec = "";
				while ((exec = brd.readLine()) != null) {

					if (exec.startsWith("Exec=")) {
						app = exec.split("=")[1].replace("%U", "").replace("%u", "");
						break;
					}

				}
				brd.close();
				mimetoApp.put(mime, app);
			}
		}
		br.close();
	}

}
