package edu.ualr.swe;
//import java.io.*;

import android.app.Activity;
import android.content.SharedPreferences;

public class DataStorage extends Activity{ 
	private SharedPreferences preferences;
	private SharedPreferences.Editor prefEditor;
	
	public DataStorage(SharedPreferences prefs) {
		preferences = prefs;
		prefEditor = preferences.edit();
		if (preferences.contains("timestamp")){
			
		}
		else{
			prefEditor.putString("oAuthToken", "");
			prefEditor.putString("oAuthSecret","");
			prefEditor.putBoolean("settings", true);
			prefEditor.putLong("timestamp",System.currentTimeMillis()/1000);
		}
	}
	
	//for the user authentication section
	public void saveAuth(String[] newAuth) {
		prefEditor.putString("oAuthToken", newAuth[0]);
		prefEditor.putString("oAuthSecret", newAuth[1]);
		prefEditor.putLong("timestamp",System.currentTimeMillis()/1000);
	}
	public void delAuth() {
		prefEditor.putString("oAuthToken", "");
		prefEditor.putString("oAuthSecret","");
	}

	//for the upload section
	public String[] getUploadData() {
		String[] uploadData = new String[3];
		uploadData[0] = preferences.getString("oAuthToken", "");
		uploadData[1] = preferences.getString("oAuthSecret", "");
		uploadData[2] = ""+preferences.getLong("timestamp", System.currentTimeMillis()/1000);
		return uploadData;
	}
	public void updateTimestamp(){
		prefEditor.putLong("timestamp",System.currentTimeMillis()/1000);
	}
	
	//for display
	public long getTimestamp(){
		return preferences.getLong("timestamp", System.currentTimeMillis()/1000);
	}
	

	
	//for settings section
	public boolean getUpload(){
		return preferences.getBoolean("settings", true);
	}
	public void setUpload(boolean nSet) {
		prefEditor.putBoolean("settings",nSet);
	}
}
