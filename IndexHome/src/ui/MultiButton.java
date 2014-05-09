package ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import voiceCommand.Speaker;

public class MultiButton extends Application {

	private Notifier notifier;
	static String msg="";
	static Map<String, String> x;
	static List<String> txt;
	int i;

	public static String parseJson(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json);

		JSONObject jsonObject = (JSONObject) obj;

		String message = (String) jsonObject.get("message");
		// long age = (Long) jsonObject.get("age");
		// System.out.println(age);
		msg=message;
		Speaker.speak(msg);
		// loop array
		JSONArray msg = (JSONArray) jsonObject.get("options");
		Iterator<String> iterator = msg.iterator();
		String mesg = "";
		if (!iterator.hasNext()) {
			// Non ambiguous
			if (message.trim().equals("")) {
				// do nothing
			} else {
				mesg = message;
				// print message
			}
			System.out.println("yes");
		} else {
			// ambiguous
			x = new HashMap<String, String>();
			txt = new ArrayList<>();
			int i = 0;
			while (iterator.hasNext()) {
				String temp = iterator.next();
				x.put("btn" + ((Integer) i).toString(),
						temp);
				txt.add(temp);
				i++;
			}
		}
		return mesg;
	}

	@Override
	public void start(Stage primaryStage) {
		Label btn = new Label();
	        List<Button> btns = new ArrayList<Button>(); 
	        List<ToolBar> tBars = new ArrayList<ToolBar>();
		int count = 0;
		for (String temp : txt) {
			Button t = new Button();
			t.setText(temp);
			t.setId("btn" + count);
			count++;
			t.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					//
					System.out
							.println(event.getSource().toString().split(",")[0]
									.split("id=")[1]);
					;
					// event.getSource().toString().split(",")[0].split("[")[1].split("=")[1];
					final String a = x.get(event.getSource().toString()
							.split(",")[0].split("id=")[1]);
					System.out.println(">" + i + " " + a + " "
							+ event.getSource());
					//notifier.notify(a);
					Desktop dt=Desktop.getDesktop();
					try {
						File fl=new File(a.trim());
						if(!fl.isDirectory())
						dt.open(fl);
						else{
							Runtime.getRuntime().exec("nemo "+a.trim());
						}							
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println(notifier.getMessage());
					Platform.exit();
				}
			});
			t.setStyle("-fx-background-color: #a6b5c9,  linear-gradient(#303842 0%, #3e5577 20%, #375074 100%),      linear-gradient(#768aa5 0%, #849cbb 5%, #5877a2 50%, #486a9a 51%, #4a6c9b 100%);    -fx-background-insets: 0 0 -1 0,0,1;    -fx-background-radius: 5,5,4;    -fx-padding: 7 30 7 30;    -fx-text-fill: #242d35;    -fx-font-family: 'Helvetica';    -fx-font-size: 12px;    -fx-text-fill: white;)        	") ;
			t.setAlignment(Pos.CENTER);    
			 ToolBar tBar = new ToolBar();
			 tBar.getItems().addAll(t);
			 tBars.add(tBar);
			btns.add(t); 	
		}
		btn.setText(msg);

		notifier = Notifier.INSTANCE;

		// StackPane root = new StackPane();
		VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(8);
        root.setStyle("-fx-padding: 5; -fx-background-color: #a6b5c9;  -fx-border-color: linear-gradient(to bottom, chocolate, derive(chocolate, 50%));");
        root.setEffect(new DropShadow());
		root.setPadding(new Insets(100, 100, 100, 100));
		root.getChildren().add(btn);
		for (i = 0; i < txt.size(); i++) {
			root.getChildren().add(tBars.get(i));
		}
		Scene scene = new Scene(root);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent t) {
				notifier.stop();
			}
		});

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be
	 * launched through deployment artifacts, e.g., in IDEs with limited FX
	 * support. NetBeans ignores main().
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		//System.out.println("In main:"+args[0]);
		parseJson(args[0]);
		launch(args);
	}
}
