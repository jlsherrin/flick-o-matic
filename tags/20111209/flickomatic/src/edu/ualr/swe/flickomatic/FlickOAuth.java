package edu.ualr.swe.flickomatic;
/**
     * OAuth
     */

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.people.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;


public class FlickOAuth {
	
	public static final String PREFS_NAME = "prefFile"; 
	public static final String KEY_OAUTH_TOKEN = "flick-o-matic-oauthToken"; 
	public static final String KEY_TOKEN_SECRET = "flick-o-matic-tokenSecret"; 
	public static final String KEY_USER_NAME = "flick-o-matic-userName"; 
	public static final String KEY_USER_ID = "flick-o-matic-userId";
	
	private static final Logger logger = LoggerFactory.getLogger(FlickOAuth.class);
	
	private FlickOMaticActivity flickoAct;
	
	public FlickOAuth (FlickOMaticActivity activity){
		this.flickoAct = activity;
	}

	public void onOAuthDone(OAuth result) {
		if (result == null) {
			Toast.makeText(flickoAct,
					"Authorization failed", //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			User user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(flickoAct,
						"Authorization failed", //$NON-NLS-1$
						Toast.LENGTH_LONG).show();
				return;
			}
			String message = String.format(Locale.US, "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
					user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			Toast.makeText(flickoAct,
					message,
					Toast.LENGTH_LONG).show();
			saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			//load(result);
		}
	}
    
    public OAuth getOAuthToken() {
   	 //Restore preferences
       SharedPreferences settings = flickoAct.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
       String oauthTokenString = settings.getString(KEY_OAUTH_TOKEN, null);
       String tokenSecret = settings.getString(KEY_TOKEN_SECRET, null);
       if (oauthTokenString == null && tokenSecret == null) {
       	logger.warn("No oauth token retrieved"); //$NON-NLS-1$
       	return null;
       }
       OAuth oauth = new OAuth();
       String userName = settings.getString(KEY_USER_NAME, null);
       String userId = settings.getString(KEY_USER_ID, null);
       if (userId != null) {
       	User user = new User();
       	user.setUsername(userName);
       	user.setId(userId);
       	oauth.setUser(user);
       }
       OAuthToken oauthToken = new OAuthToken();
       oauth.setToken(oauthToken);
       oauthToken.setOauthToken(oauthTokenString);
       oauthToken.setOauthTokenSecret(tokenSecret);
       logger.debug("Retrieved token from preference store: oauth token={}, and token secret={}", oauthTokenString, tokenSecret); //$NON-NLS-1$
       return oauth;
   }
    

	  public void saveOAuthToken(String userName, String userId, String token, String tokenSecret) {
	    	logger.debug("Saving userName=%s, userId=%s, oauth token={}, and token secret={}", new String[]{userName, userId, token, tokenSecret}); //$NON-NLS-1$
	    	SharedPreferences sp = flickoAct.getSharedPreferences(PREFS_NAME,
					Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString(KEY_OAUTH_TOKEN, token);
			editor.putString(KEY_TOKEN_SECRET, tokenSecret);
			editor.putString(KEY_USER_NAME, userName);
			editor.putString(KEY_USER_ID, userId);
			editor.commit();
	    }
}