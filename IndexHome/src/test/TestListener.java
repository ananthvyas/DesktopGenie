package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import voiceCommand.Speaker;

public class TestListener implements NativeKeyListener {
	static Process p;
	static boolean pressed = false;
	static int lastpressed;
	static String indexHome="";
	public void nativeKeyPressed(NativeKeyEvent e) {
		//System.out.println("Key Pressed: "
			//	+ NativeKeyEvent.getKeyText(e.getKeyCode()));

		//if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
		// GlobalScreen.unregisterNativeHook();
		// }
		
		lastpressed=e.getKeyCode();
		
		
	}

	public void nativeKeyReleased(NativeKeyEvent e) {
		//System.out.println("Key Released: "
		//		+ NativeKeyEvent.getKeyText(e.getKeyCode()));
		if(e.getKeyCode()==NativeKeyEvent.VK_SHIFT && lastpressed==e.getKeyCode()){
			if(!pressed){
				try {
					System.out.println("Recording");
					p = Runtime.getRuntime().exec(
							"sox -t alsa default recording.flac");
					pressed=true;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}else{
				p.destroy();
				try {
					System.out.println("Done.");
					Process pr=Runtime.getRuntime().exec("java -jar speechtotext.jar "+indexHome);
					pr.waitFor();
					BufferedReader stdInput = new BufferedReader(
							new InputStreamReader(pr.getInputStream()));
					String fl = "";
					while ((fl = stdInput.readLine()) != null) {
						System.out.println(fl);
					}
					pressed=false;
				} catch (Exception e1) {
					pressed=false;
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public void nativeKeyTyped(NativeKeyEvent e) {
		//System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
	}

	public static void main(String[] args) throws Exception {
		try {
			indexHome=args[0];
			GlobalScreen.registerNativeHook();

		} catch (NativeHookException ex) {
			System.err
					.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			System.exit(1);
		}

		// Construct the example object and initialze native hook.
		GlobalScreen.getInstance().addNativeKeyListener(new TestListener());
	}
}
