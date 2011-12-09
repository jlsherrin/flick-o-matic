package edu.ualr.swe.flickomatic;
///////////////////////////////////////////////////////////////////////////////////////////
//                                                   41 Post                             //
// Android: loading images from a remote location, SD card and from the Resources folder //
// Created by DimasTheDriver in 25/Jan/2011                                      		 //
// Availiable at:       http://www.41post.com/?p=2744                           		 //
///////////////////////////////////////////////////////////////////////////////////////////

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

//import edu.ualr.swe.LoadImagesFromSDCardActivity.Photo;
import edu.ualr.swe.flickomatic.images.ImageAdapter;
import edu.ualr.swe.flickomatic.tasks.GetOAuthTokenTask;
import edu.ualr.swe.flickomatic.tasks.OAuthTask;
import edu.ualr.swe.flickomatic.tasks.LoadImagesTask;

/**
 * Loads images from SD card. 
 * 
 * @author Mihai Fonoage
 *
 */
public class FlickOMaticActivity extends Activity implements OnItemClickListener {
    
	private GridView sdcardImages;
    private ImageAdapter imageAdapter;
    private Display display;
    ImageView img;
    Bitmap image;
    //ArrayList urlsArray = new ArrayList();
    String apiKey = "8d7dec18e1e325fa0df671b184ff91db";
    String apiSecret = "1cf670e8f539eda9";
    String sha1Key = "";
	
    public static final String CALLBACK_SCHEME = "flick-o-matic-oauth";
	public static final String PREFS_NAME = "prefFile"; 
	public static final String KEY_OAUTH_TOKEN = "flick-o-matic-oauthToken"; 
	public static final String KEY_TOKEN_SECRET = "flick-o-matic-tokenSecret"; 
	public static final String KEY_USER_NAME = "flick-o-matic-userName"; 
	public static final String KEY_USER_ID = "flick-o-matic-userId"; 
	
	private ListView listView;
	
	private static final Logger logger = LoggerFactory.getLogger(FlickOMaticActivity.class);
    

    /**
     * Creates the content view, sets up the grid, the adapter, and the click listener.
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

		TextView dbg = (TextView)findViewById(R.id.textView1);
		display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();        
        setupViews();logger.debug("views");
		
        OAuth oauth = getOAuthToken();
        if (oauth == null || oauth.getUser() == null) {
			OAuthTask task = new OAuthTask(this);
			task.execute();
		}else{
	        setProgressBarIndeterminateVisibility(true); 
	        logger.debug("");
	        loadImages(oauth);
		}
    }

    /**
     * Free up bitmap related resources.
     */
    protected void onDestroy() {
        super.onDestroy();
        final GridView grid = sdcardImages;
        grid.setAdapter(null);
//        final int count = grid.getChildCount();
//        ImageView v = null;
//        for (int i = 0; i < count; i++) {
//            v = (ImageView) grid.getChildAt(i);
//            ((BitmapDrawable) v.getDrawable()).setCallback(null);
//        }        
    }
    
    /**
     * Setup the grid view.
     */
    private void setupViews() {
        sdcardImages = (GridView) findViewById(R.id.sdcard);
        sdcardImages.setNumColumns(display.getWidth()/95);
        sdcardImages.setClipToPadding(false);
        sdcardImages.setOnItemClickListener(this);
        imageAdapter = new ImageAdapter(getApplicationContext()); 
        sdcardImages.setAdapter(imageAdapter);
    }
   
    /**
     * Load images.
     */
    private void loadImages(OAuth oauth) {
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            new LoadImagesTask(oauth, this).execute();
        } else {
            final PhotoList photos = (PhotoList) data;
            if (photos.getTotal() == 0) {
                new LoadImagesTask(oauth, this).execute();
            }
            addImage(photos);
            }
        }
        
     public void addImage(PhotoList value) {
		// TODO Auto-generated method stub
		for (com.gmail.yuyang226.flickr.photos.Photo image : value) {
            imageAdapter.addPhoto(image);
            imageAdapter.notifyDataSetChanged();
        }
	}

	/* Add image(s) to the grid view adapter.
     * 
     * @param value Array of Photos references
     */
    public void addImage(Photo value) {
        //for (com.gmail.yuyang226.flickr.photos.Photo image : value) {
            imageAdapter.addPhoto(value);
            imageAdapter.notifyDataSetChanged();
        //}
    }
     
    /**
     * Save bitmap images into a list and return that list. 
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final GridView grid = sdcardImages;
        final int count = grid.getChildCount();
        final Photo[] list = new Photo[count];

        for (int i = 0; i < count; i++) {
            final ImageView v = (ImageView) grid.getChildAt(i);
            list[i] = new Photo(((BitmapDrawable) v.getDrawable()).getBitmap());
        }

        return list;
    }
   
    /**
     * When an image is clicked, load that image as a puzzle. 
     */
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {        
        int columnIndex = 0;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection,
                null, 
                null, 
                null);
        if (cursor != null) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToPosition(position);
            String imagePath = cursor.getString(columnIndex); 

            FileInputStream is = null;
            BufferedInputStream bis = null;
            try {
                is = new FileInputStream(new File(imagePath));
                bis = new BufferedInputStream(is);
                Bitmap bitmap = BitmapFactory.decodeStream(bis);
                Bitmap useThisBitmap = Bitmap.createScaledBitmap(bitmap, parent.getWidth(), parent.getHeight(), true);
                bitmap.recycle();
                //Display bitmap (useThisBitmap)
            } 
            catch (Exception e) {
                //Try to recover
            }
            finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                    cursor.close();
                    projection = null;
                } catch (Exception e) {
                }
            }
        }
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		//this is very important, otherwise you would get a null Scheme in the onResume later on.
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		OAuth savedToken = getOAuthToken();
		logger.debug("scheme = "+scheme);
		if (CALLBACK_SCHEME.equals(scheme) && (savedToken == null || savedToken.getUser() == null)) {
			Uri uri = intent.getData();
			String query = uri.getQuery();
			logger.debug("Returned Query: {}", query); //$NON-NLS-1$
			String[] data = query.split("&"); //$NON-NLS-1$
			if (data != null && data.length == 2) {
				String oauthToken = data[0].substring(data[0].indexOf("=") + 1); //$NON-NLS-1$
				String oauthVerifier = data[1].substring(data[1].indexOf("=") + 1); //$NON-NLS-1$
				logger.debug("OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier); //$NON-NLS-1$

				OAuth oauth = getOAuthToken();
				if (oauth != null && oauth.getToken() != null && oauth.getToken().getOauthTokenSecret() != null) {
					GetOAuthTokenTask task = new GetOAuthTokenTask(this);
					task.execute(oauthToken, oauth.getToken().getOauthTokenSecret(), oauthVerifier);
				}
			}
		}
	}  
    
    public void onProgressUpdate(PhotoList value) {
        addImage(value);
    }
	
	private ArrayList buildUrlsArray(String jsonResponse) {	   
		   ArrayList<String> farmsArray = new ArrayList<String>();
	       ArrayList<String> serversArray = new ArrayList<String>();
	       ArrayList<String> idsArray = new ArrayList<String>();
	       ArrayList<String> secretsArray = new ArrayList<String>();
	       ArrayList localUrlsArray =new ArrayList();
		   JSONObject jObject = null;
			
		   try {
				jObject = new JSONObject(jsonResponse);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			JSONObject photosObject = null;
			try {
				photosObject = jObject.getJSONObject("photos");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			
			JSONArray photoArray = null;
			try {
				photoArray = photosObject.getJSONArray("photo");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			int interestsCount = photoArray.length();
		
			//String interestsBody = "";
			for (int i=0; i<interestsCount; i++)
			{
				try {
					farmsArray.add(photoArray.getJSONObject(i).getString("farm").toString());
					serversArray.add(photoArray.getJSONObject(i).getString("server").toString());
					idsArray.add(photoArray.getJSONObject(i).getString("id").toString());
					secretsArray.add(photoArray.getJSONObject(i).getString("secret").toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			for (int i = 0; i < farmsArray.size(); i++) {
				localUrlsArray.add("http://farm"+ farmsArray.get(i) +".static.flickr.com/" + serversArray.get(i) + "/"+ idsArray.get(i) +"_"+ secretsArray.get(i) +"_t.jpg");
			}
			return localUrlsArray;
	   }

	//OAUTH HELPER METHODS
	public void onOAuthDone(OAuth result) {
		if (result == null || result.getUser() == null) {
			Toast.makeText(this,"Authorization failed",	Toast.LENGTH_LONG).show();  //$NON-NLS-1$
		} else {
			User user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(this,"Authorization failed",	Toast.LENGTH_LONG).show(); //$NON-NLS-1$
				return;
			}
			String message = String.format(Locale.US, "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
					user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			logger.debug(message);
			Toast.makeText(this,message,Toast.LENGTH_LONG).show();
			saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			loadImages(result);
		}
	}
	
	public OAuth getOAuthToken() {
    	 //Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
    	SharedPreferences sp = getSharedPreferences(PREFS_NAME,	Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(KEY_OAUTH_TOKEN, token);
		editor.putString(KEY_TOKEN_SECRET, tokenSecret);
		editor.putString(KEY_USER_NAME, userName);
		editor.putString(KEY_USER_ID, userId);
		editor.commit();
    }

	    
}
