package edu.ualr.swe;
//import java.io.*;

import android.app.Activity;
import android.content.SharedPreferences;

public class DataStorage extends Activity{
	/*
	 * The following files are utilized by this class:
	 * t.txt - timestamp
	 * a.txt - OAuth token & secret
	 * s.txt - settings value
	 */
	
	/*
	private BufferedReader br;
	private BufferedWriter bw;
	
	private String oAuthToken;
	private String oAuthSecret;
	
	private String timestamp;
	
	private boolean settings;
	*/
	private String PREF_FILE_NAME = "prefFile";
	private SharedPreferences preferences;
	private SharedPreferences.Editor prefEditor;
	
	public DataStorage() {
		preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
		prefEditor = preferences.edit();
		if (preferences.contains("timestamp")){
			
		}
		else{
			prefEditor.putString("oAuthToken", "");
			prefEditor.putString("oAuthSecret","");
			prefEditor.putBoolean("settings", true);
			prefEditor.putLong("timestamp",System.currentTimeMillis());
		}
		/*File newFile;
		try {
			//for OAuth file
			newFile = new File("a.txt");
			if (newFile.createNewFile() == true){
				
			}
			else
				loadFile(0);
			//for settings file
			//fore timestamp file
		}catch(Exception e){}*/
	}
	
	//for the user authentication section
	public void saveAuth(String[] newAuth) {
		prefEditor.putString("oAuthToken", newAuth[0]);
		prefEditor.putString("oAuthSecret", newAuth[1]);
		prefEditor.putLong("timestamp",System.currentTimeMillis());
		/*oAuthToken = newAuth[0];
		oAuthSecret = newAuth[1];
		saveFile(0);*/
	}
	public void delAuth() {
		prefEditor.putString("oAuthToken", "");
		prefEditor.putString("oAuthSecret","");
		/*oAuthToken = "";
		oAuthSecret = "";
		saveFile(0);*/
	}

	//for the upload section
	public String[] getUploadData() {
		String[] uploadData = new String[3];
		uploadData[0] = preferences.getString("oAuthToken", "");
		uploadData[1] = preferences.getString("oAuthSecret", "");
		uploadData[2] = ""+preferences.getLong("timestamp", System.currentTimeMillis());
		return uploadData;
		/*loadFile(0);
		loadFile(2);
		uploadData[0] = oAuthToken;
		uploadData[1] = oAuthSecret;
		uploadData[2] = timestamp;
		timestamp = "" +System.currentTimeMillis();
		saveFile(2);
		return uploadData;*/
	}
	public void updateTimestamp(){
		prefEditor.putLong("timestamp",System.currentTimeMillis());
	}
	
	//for settings section
	public boolean getUpload(){
		return preferences.getBoolean("settings", true);
		/*loadFile(1);
		return settings;*/
	}
	public void setUpload(boolean nSet) {
		prefEditor.putBoolean("settings",nSet);
		//settings = nSet;
		//saveFile(1);
	}
	
	//private methods for use through public methods
	private void saveFile(int switcher) {
		/*
		switch (switcher){
			//OAuth
			case 0:
				try{
					bw = new BufferedWriter(new FileWriter("a.txt"));
					bw.write(oAuthToken + "\n" + oAuthSecret);
				}catch(Exception e){}
				break;
			//settings
			case 1:
				try{
					bw = new BufferedWriter(new FileWriter("s.txt"));
					if (settings)
						bw.write("true");
					else
						bw.write("false");
				}catch(Exception e){}
				break;
			//timestamp
			case 2:
				try{
					bw = new BufferedWriter(new FileWriter("t.txt"));
					bw.write(timestamp);
				}catch(Exception e){}
				break;
		}
		*/
	}
	private void loadFile(int switcher) {
		/*
		String temp;
		switch(switcher){
			//OAuth
			case 0:
				try {
					br = new BufferedReader(new FileReader("a.txt"));
					if ((temp = br.readLine()) != null)
						temp = oAuthToken;
					if ((temp = br.readLine()) != null)
						temp = oAuthSecret;
					br.close();
				}
				catch (Exception e){}
				break;
			//settings
			case 1:
				try {
					br = new BufferedReader(new FileReader("s.txt"));
					temp = br.readLine();
					settings = Boolean.parseBoolean(temp);
					br.close();
				}
				catch (Exception e){}
				break;
			//timestamp
			case 2:
				try {
					br = new BufferedReader(new FileReader("t.txt"));
					if ((temp = br.readLine()) != null)
						temp = timestamp;
					br.close();
				}
				catch (Exception e){}
				break;
		}
		*/
	}
}
