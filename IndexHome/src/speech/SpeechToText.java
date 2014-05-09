package speech;

import init.Configuration;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import runapps.RunFiles;
import voiceCommand.Speaker;

public class SpeechToText {
	static Logger lg = Logger.getLogger("speechtotext");
	public static String indexPath = "";

	public static void main(String[] args) throws Exception {
		// MicrophoneRecorder mr = new
		// MicrophoneRecorder(AudioFormatUtil.getDefaultFormat());
		indexPath = args[0];
		final String authUser = "benchmark";
		final String authPassword = "champ420";
		System.setProperty("mbrola.base", "./lib/mbrola");
		System.setProperty("freetts.voices",
				"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		System.getProperties().put("http.proxyHost", "netmon.iitb.ac.in");
		System.getProperties().put("http.proxyPort", "80");

		Authenticator.setDefault(new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(authUser, authPassword
						.toCharArray());
			}
		});
		// System.exit(0);
		System.setProperty("http.proxyUser", authUser);
		System.setProperty("http.proxyPassword", authPassword);

		Path filePath = new File("recording.flac").toPath();
		byte[] data = Files.readAllBytes(filePath);

		String request = "http://www.google.com/" + "speech-api/v1/recognize?"
				+ "xjerr=1&client=speech2text&lang=en-in";
		URL url = new URL(request);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"audio/x-flac; rate=48000");
		connection.setRequestProperty("User-Agent", "speech2text");
		connection.setConnectTimeout(60000);
		connection.setUseCaches(false);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.write(data);
		wr.flush();
		wr.close();
		connection.disconnect();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String decodedString = "";
		String jsonstr = "";
		while ((decodedString = in.readLine()) != null) {
			jsonstr += decodedString;
		}
		// System.out.println(jsonstr);
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(jsonstr);

		JSONObject jsonObject = (JSONObject) obj;

		long status = (Long) jsonObject.get("status");
		if (status == 0) {
			// String id = (String) jsonObject.get("id");
			JSONArray msg = (JSONArray) jsonObject.get("hypotheses");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = msg.iterator();
			String utterance = "";
			double confidence = 0.0;
			while (iterator.hasNext()) {
				JSONObject jo = iterator.next();
				utterance = (String) jo.get("utterance");
				confidence = (Double) jo.get("confidence");
			}
			utterance = utterance.toLowerCase();
			// utterance = "calculate 2 into 4";

			// JsonParser.main(arr);
			// NotificationPopup.main(arr);
			// utterance = "key control s";
			// confidence = 0.66;
			if (confidence > 0.6) {
				String[] cmds = { "notify-send", "You said " + utterance };
				Process pr = Runtime.getRuntime().exec(cmds);
				pr.waitFor();
				System.out.println(utterance);
				if (utterance.startsWith("open ")) {
					openModule(utterance.split("open")[1].trim());
				} else if (utterance.startsWith("web ")) {
					webModule(utterance.split("web")[1].trim());
				} else if (utterance.startsWith("action ")) {
					actionModule(utterance.split("action")[1].trim());
				} else if (utterance.startsWith("type ")) {
					typeModule(utterance.split("type")[1].trim());
				} else if (utterance.startsWith("run ")) {
					Runtime.getRuntime().exec(utterance.split("run")[1].trim());
				} else if (utterance.startsWith("current ")) {
					currentModule(utterance.split("current")[1].trim());
				} else if (utterance.startsWith("mail ")) {
					mailModule(utterance);
				} else if (utterance.startsWith("weather ")) {
					weatherModule(utterance.split("weather")[1].trim());
				} else if (utterance.startsWith("distance ")) {
					String[] arg = utterance.split("distance")[1].trim().split(
							"to");
					distanceModule(arg[0], arg[1]);
				} else if (utterance.startsWith("calculate ")) {
					calculatorModule(utterance);
				} else if (utterance.startsWith("command ")) {
					commandModule(utterance.split("command")[1].trim());
				}
			} else {
				System.out.println(utterance + " " + confidence);
				String[] cmds = { "notify-send", "-u", "critical",
						"Please repeat !" };
				Process pr = Runtime.getRuntime().exec(cmds);
				pr.waitFor();
				Speaker.speak("Please repeat !");
			}
		} else {
			String[] cmds = { "notify-send", "-u", "critical",
					"Please repeat !" };
			Process pr = Runtime.getRuntime().exec(cmds);
			pr.waitFor();
			Speaker.speak("Please repeat !");
		}
	}

	static void commandModule(String cmd) throws IOException {
		HashMap<String, String> txtToKey = new HashMap<String, String>();
		txtToKey.put("control", "ctrl");
		txtToKey.put("return", "Return");
		txtToKey.put("enter", "Return");
		txtToKey.put("newline", "Return");
		String[] cmds = cmd.split("[ ]+");
		for (int i = 0; i < cmds.length; i++) {
			if (txtToKey.keySet().contains(cmds[i])) {
				cmds[i] = txtToKey.get(cmds[i]);
			}
		}
		cmd = "";
		for (int i = 0; i < cmds.length; i++) {
			cmd += cmds[i] + " ";
		}
		cmd = cmd.trim();
		Runtime.getRuntime()
				.exec("xdotool key " + cmd.trim().replace(" ", "+"));
	}

	static void distanceModule(String origin, String destination)
			throws MalformedURLException, IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			// String origin = "IIT Bombay";
			// String destination = "Mumbai Airport";
			String duration = "";
			String distance = "";
			Document doc = db.parse(new URL(
					"http://maps.googleapis.com/maps/api/distancematrix/xml?origins="
							+ origin + "&destinations=" + destination
							+ "&mode=driving&language=en-US&sensor=false")
					.openStream());
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("duration");
			if (nList.getLength() > 0) {
				Node nNode = nList.item(0);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					NodeList cList = eElement.getElementsByTagName("text");
					if (cList.getLength() > 0) {
						Node cNode = cList.item(0);
						if (cNode.getNodeType() == Node.ELEMENT_NODE) {
							Element cElement = (Element) cNode;
							// System.out.println(cElement.getTextContent());
							duration = cElement.getTextContent();
						}
					}
				}
			}
			nList = doc.getElementsByTagName("distance");
			if (nList.getLength() > 0) {
				Node nNode = nList.item(0);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					NodeList cList = eElement.getElementsByTagName("text");
					if (cList.getLength() > 0) {
						Node cNode = cList.item(0);
						if (cNode.getNodeType() == Node.ELEMENT_NODE) {
							Element cElement = (Element) cNode;
							// System.out.println(cElement.getTextContent());
							distance = cElement.getTextContent();
						}
					}
				}
			}
			Speaker.speak("Distance from " + origin + " to " + destination
					+ " is " + distance + " and time taken will be at least "
					+ duration + " .");
			Runtime.getRuntime().exec(
					new String[] {
							"notify-send",
							"Travel Details",
							"Distance from " + origin + " to " + destination
									+ " is " + distance
									+ " and time taken will be at least "
									+ duration + " ." });
			// System.out.println(retString);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String getStringFromBuffer() throws UnsupportedFlavorException,
			IOException {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);
		return result;
	}

	static boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
			int d = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	static void weatherModule(String city) throws MalformedURLException,
			IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			// String city = "Bangalore";
			String temperature = "";
			String humidity = "";

			Document doc = db
					.parse(new URL(
							"http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D%22"
									+ city + "%22&format=xml").openStream());
			doc.getDocumentElement().normalize();
			String woeid = "";
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("woeid");
			if (nList.getLength() > 0) {
				Node nNode = nList.item(0);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					// System.out.println(">" + eElement.getTextContent());
					woeid = eElement.getTextContent();
					// System.out.println("Staff id : " +
					// eElement.getElementsByTagName("woeid"));
				}
			}
			doc = db.parse(new URL(
					"http://weather.yahooapis.com/forecastrss?w=" + woeid
							+ "&u=c").openStream());
			doc.getDocumentElement().normalize();
			// JSONObject json =
			// readJsonFromUrl("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D%22Mumbai%22&format=json");
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());

			nList = doc.getElementsByTagName("yweather:wind");
			if (nList.getLength() > 0) {
				Node nNode = nList.item(0);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					// System.out.println("Temperature : " +
					// eElement.getAttribute("chill") + " C");
					temperature = eElement.getAttribute("chill")
							+ " centigrade";
					// System.out.println("Temperature : " +
					// eElement.getAttribute("chill") + " C");
					// System.out.println(">" + eElement.getTextContent());
					woeid = eElement.getTextContent();
					// System.out.println("Staff id : " +
					// eElement.getElementsByTagName("woeid"));
				}
			}
			nList = doc.getElementsByTagName("yweather:atmosphere");
			if (nList.getLength() > 0) {
				Node nNode = nList.item(0);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					// System.out.println("Humidity : " +
					// eElement.getAttribute("humidity") + " ");
					humidity = eElement.getAttribute("humidity") + " %";
					// System.out.println("Temperature : " +
					// eElement.getAttribute("chill") + " C");
					// System.out.println(">" + eElement.getTextContent());
					woeid = eElement.getTextContent();
					// System.out.println("Staff id : " +
					// eElement.getElementsByTagName("woeid"));
				}
			}
			Runtime.getRuntime().exec(
					new String[] {
							"notify-send",
							"Weather",
							"Temperature of " + city + " is " + temperature
									+ " and humidity " + humidity + "." });
			// System.out.println(retString);
			// System.out.println(doc.toString() );
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(json.get("id"));
	}

	static void currentModule(String cmd) throws IOException,
			UnsupportedFlavorException, InterruptedException, ParseException,
			org.json.simple.parser.ParseException {
		// Process
		// pr=Runtime.getRuntime().exec("xdotool windowactivate 56623155");
		// pr.waitFor();
		if (cmd.equals("send")) {
			Process pr = Runtime.getRuntime().exec("xdotool key ctrl+KP_Enter");
			pr.waitFor();
			return;
		}
		Process pr = Runtime.getRuntime().exec("xdotool key ctrl+l");
		pr.waitFor();
		pr = Runtime.getRuntime().exec("xdotool key ctrl+c");
		pr.waitFor();
		String txt = getStringFromBuffer().trim();
		if (!txt.equals("")) {
			if (cmd.startsWith("open")) {
				String arg = cmd.split("open")[1].trim();
				if (isNumeric(arg)) {
					int num = Integer.parseInt(arg);
					String str = "ls -1 " + txt;
					System.out.println(str);
					Process proc = Runtime.getRuntime().exec(str);
					BufferedReader stdInput = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String fl = "";
					int count = 0;
					while ((fl = stdInput.readLine()) != null) {
						count++;
						if (count == num) {
							Desktop dt = Desktop.getDesktop();
							dt.open(new File(txt + "/" + fl));
							break;
						}
					}
				} else {
					new RunFiles(arg, "", txt.toLowerCase());
				}
			} else if (cmd.startsWith("search")) {
				txt = getDomain(txt);
				String url = "google-chrome http://www.google.co.in/search?ie=UTF-8&oe=UTF-8&sourceid=navclient&gfns=1&q="
						+ cmd.split("search")[1].trim().replace(" ", "+")
						+ "+site:" + txt;
				Runtime.getRuntime().exec(url);
			}
		}
	}

	static String getDomain(String url) {
		char[] ur = url.toCharArray();
		for (int i = ur.length - 1; i >= 0; i--) {
			if (ur[i] == '/') {
				ur[i] = '\0';
				break;
			}
		}
		return new String(ur);
	}

	static void typeModule(String txt) throws IOException, InterruptedException {
		char[] text = txt.toLowerCase().toCharArray();
		for (int i = 0; i < text.length; i++) {
			if (text[i] != ' ') {

				Process p = Runtime.getRuntime().exec(
						new String[] { "xdotool", "type", "" + text[i] });
				p.waitFor();
			} else {
				Process p = Runtime.getRuntime().exec("xdotool key space");
				p.waitFor();
			}
		}
	}

	@SuppressWarnings("unused")
	static void openModule(String cmd) throws IOException, ParseException,
			org.json.simple.parser.ParseException {
		if (cmd.startsWith("pdf ")) {
			RunFiles rf = new RunFiles(cmd.split("pdf")[1].trim(), "pdf", "");
		} else if (cmd.startsWith("video ")) {
			RunFiles rf = new RunFiles(cmd.split("video")[1].trim(), "video",
					"");

		} else if (cmd.startsWith("audio ")) {
			RunFiles rf = new RunFiles(cmd.split("audio")[1].trim(), "audio",
					"");
		} else if (cmd.startsWith("directory ")) {
			RunFiles rf = new RunFiles(cmd.split("directory")[1].trim(),
					"directory", "");
		} else {
			RunFiles rf = new RunFiles(cmd, "", "");
		}
	}

	static void webModule(String cmd) throws IOException, ParseException {
		Runtime.getRuntime()
				.exec("google-chrome http://www.google.co.in/search?ie=UTF-8&oe=UTF-8&sourceid=navclient&gfns=1&q="
						+ cmd.trim().replace(" ", "+"));
	}

	static void actionModule(String cmd) throws IOException, ParseException,
			InterruptedException {
		if (cmd.startsWith("minimise")) {
			String str = "xdotool search --name "
					+ cmd.split("minimise")[1].trim();
			System.out.println(str);
			Process proc = Runtime.getRuntime().exec(str);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String s = "";
			while ((s = stdInput.readLine()) != null) {
				Runtime.getRuntime().exec("xdotool windowminimize " + s.trim());
			}

		} else if (cmd.startsWith("focus")) {
			String str = "xdotool search --name "
					+ cmd.split("focus")[1].trim();
			System.out.println(str);
			Process proc = Runtime.getRuntime().exec(str);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String s = "";
			while ((s = stdInput.readLine()) != null) {
				Runtime.getRuntime().exec("xdotool windowactivate " + s.trim());
			}
		} else if (cmd.startsWith("close")) {
			String str = "xdotool search --name "
					+ cmd.split("close")[1].trim();
			System.out.println(str);
			Process proc = Runtime.getRuntime().exec(str);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			String s = "";
			while ((s = stdInput.readLine()) != null) {
				Runtime.getRuntime().exec("xdotool windowkill " + s.trim());
			}
		} else if (cmd.startsWith("save")) {
			String str = "xdotool key ctrl+s";
			System.out.println(str);
			Runtime.getRuntime().exec(str);
		}
	}

	static void calculatorModule(String cmd) throws IOException,
			InterruptedException {
		String utterance = cmd;
		String output = "";
		String s = "";
		String expr = utterance.split("calculate")[1].trim();
		HashMap<String, String> map = Configuration.calcMappings();
		boolean noSymbol = false;
		while (!noSymbol) {
			noSymbol = true;
			for (String rep : map.keySet()) {
				if (expr.contains(rep)) {
					noSymbol = false;
					String[] parts = expr.split(rep);
					expr = parts[0].trim() + map.get(rep)
							+ ((parts.length == 2) ? parts[1].trim() : "");
				}
			}

		}
		System.out.println(expr);
		// logger.fine("Calculator expr:" + expr);
		Runtime rt = Runtime.getRuntime();
		String[] bc_cmd = { "/bin/sh", "-c", "echo " + expr + " | bc -l" };

		// String[] cmd = {"/bin/sh", " echo " + expr + " | bc"};

		Process proc = rt.exec(bc_cmd);
		proc.waitFor();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				proc.getErrorStream()));

		// read the output from the command
		// System.out.println("Here is the standard output of the command:\n");

		while ((s = stdInput.readLine()) != null) {
			output += (s + "\n");
		}
		String error = "";
		// read any errors from the attempted command
		while ((s = stdError.readLine()) != null) {
			error += (s + "\n");
		}
		if (!error.equals("")) {
			// logger.severe(error);
		}

		// speaker.allocate();
		String[] res = output.trim().split("\\.");
		if (res.length > 1) {
			String tmp = res[1].substring(0, 2);
			output = res[0] + "." + tmp;
		}
		String op = "My calculation shows, it should be " + output.trim();
		Runtime.getRuntime().exec(new String[] { "notify-send", op });
		Speaker.speak(op);
		// speaker.deallocate();

	}

	static void mailModule(String cmd) throws IOException {
		String utterance = cmd;
		HashMap<String, String> contacts = Configuration.getContactHashMap();
		Pattern mailPattern = Pattern
				.compile("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$");
		String rest = utterance.split("mail")[1].trim();
		String name = null;
		String subject = null;

		Runtime rt = Runtime.getRuntime();
		if (rest.contains("subject")) {
			String[] words = rest.split("subject");
			if (words.length == 2) {
				name = words[0].trim();
				subject = words[1].trim();
			} else {
				name = words[0].trim();
			}

		} else {
			name = rest;
		}
		String person = name;
		Speaker.speak("Mailing " + person);
		if (!mailPattern.matcher(name).find()) {
			if (contacts.containsKey(name)) {
				name = contacts.get(name.trim());
			} else {
				System.err.println("Directory does not contain " + name);
				return;
			}
		}

		if (subject == null) {
			String th_cmd = "thunderbird -compose to='" + name + "'";
			// logger.fine("thunderbird command:" + th_cmd);
			rt.exec(th_cmd);
		} else {
			String th_cmd = "thunderbird -compose to='" + name + "',subject='"
					+ subject + "'";
			// logger.fine("thunderbird command:" + th_cmd);
			rt.exec(th_cmd);
		}
	}
}