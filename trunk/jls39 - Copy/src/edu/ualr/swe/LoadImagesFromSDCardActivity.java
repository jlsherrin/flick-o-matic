///////////////////////////////////////////////////////////////////////////////////////////
//                                                   41 Post                             //
// Android: loading images from a remote location, SD card and from the Resources folder //
// Created by DimasTheDriver in 25/Jan/2011                                      		 //
// Availiable at:       http://www.41post.com/?p=2744                           		 //
///////////////////////////////////////////////////////////////////////////////////////////

package edu.ualr.swe;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import edu.ualr.swe.tasks.GetOAuthTokenTask;
import edu.ualr.swe.tasks.LoadPhotostreamTask;
import edu.ualr.swe.tasks.LoadUserTask;
import edu.ualr.swe.tasks.OAuthTask;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * Loads images from SD card. 
 * 
 * @author Mihai Fonoage
 *
 */
public class LoadImagesFromSDCardActivity extends Activity implements
OnItemClickListener {
    
	private GridView sdcardImages;
    private ImageAdapter imageAdapter;
    private Display display;
    ImageView img;
    Bitmap image;
    ArrayList urlsArray = new ArrayList();
    String apiKey = "8d7dec18e1e325fa0df671b184ff91db";
    String apiSecret = "1cf670e8f539eda9";
    String sha1Key = "";
	
    public static final String CALLBACK_SCHEME = "flick-o-matic-oauth";
	public static final String PREFS_NAME = "flick-o-matic-pref"; 
	public static final String KEY_OAUTH_TOKEN = "flick-o-matic-oauthToken"; 
	public static final String KEY_TOKEN_SECRET = "flick-o-matic-tokenSecret"; 
	public static final String KEY_USER_NAME = "flick-o-matic-userName"; 
	public static final String KEY_USER_ID = "flick-o-matic-userId"; 
	
	private ListView listView;
	private TextView textUserTitle;
	private TextView textUserName;
	private TextView textUserId;
	private ImageView userIcon;
	private ImageButton refreshButton;
	
	private static final Logger logger = LoggerFactory.getLogger(AuthActivity.class);
    

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
        
        this.textUserTitle = (TextView) this.findViewById(R.id.profilePageTitle);
		this.textUserName = (TextView) this.findViewById(R.id.userScreenName);
		this.textUserId = (TextView) this.findViewById(R.id.userId);
		this.userIcon = (ImageView) this.findViewById(R.id.userImage);
		this.listView = (ListView) this.findViewById(R.id.imageList);
		this.refreshButton = (ImageButton) this.findViewById(R.id.btnRefreshUserProfile); 
		TextView dbg = (TextView)findViewById(R.id.textView1);
		
       /* OAuth oauth = getOAuthToken();
		if (oauth == null || oauth.getUser() == null) {
			OAuthTask task = new OAuthTask(this);
			task.execute();
		} else {
			load(oauth);
		}
*/
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        
        setupViews();
        setProgressBarIndeterminateVisibility(true); 
        
        //TODO: make user id dynamic
        final HttpGet get = new HttpGet("http://api.flickr.com/services/rest/?&method=flickr.people.getPublicPhotos&api_key=8d7dec18e1e325fa0df671b184ff91db&user_id=69944181@N02&format=json&nojsoncallback=1");
        HttpClient httpclient = new DefaultHttpClient();
	    ResponseHandler<String> responseHandler = new BasicResponseHandler();
	    String jsonResponse="";
	    
		try {		
			jsonResponse = httpclient.execute(get, responseHandler);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			jsonResponse = "IOException";
			e.printStackTrace();
		}
		
		urlsArray = buildUrlsArray(jsonResponse);
		 dbg.setText(jsonResponse);
        loadImages();
        
     
    }

    /**
     * Free up bitmap related resources.
     */
    protected void onDestroy() {
        super.onDestroy();
        final GridView grid = sdcardImages;
        final int count = grid.getChildCount();
        ImageView v = null;
        for (int i = 0; i < count; i++) {
            v = (ImageView) grid.getChildAt(i);
            ((BitmapDrawable) v.getDrawable()).setCallback(null);
        }
        
        listView.setAdapter(null);
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
    private void loadImages() {
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            new LoadImagesFromSDCard().execute();
        } else {
            final LoadedImage[] photos = (LoadedImage[]) data;
            if (photos.length == 0) {
                new LoadImagesFromSDCard().execute();
            }
            for (LoadedImage photo : photos) {
                addImage(photo);
            }
        }
    }
    /**
     * Add image(s) to the grid view adapter.
     * 
     * @param value Array of LoadedImages references
     */
    private void addImage(LoadedImage... value) {
        for (LoadedImage image : value) {
            imageAdapter.addPhoto(image);
            imageAdapter.notifyDataSetChanged();
        }
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
        final LoadedImage[] list = new LoadedImage[count];

        for (int i = 0; i < count; i++) {
            final ImageView v = (ImageView) grid.getChildAt(i);
            list[i] = new LoadedImage(((BitmapDrawable) v.getDrawable()).getBitmap());
        }

        return list;
    }
    /**
     * Async task for loading the images from the SD card. 
     * 
     * @author Mihai Fonoage
     *
     */
    class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {
        
        /**
         * Load images from SD Card in the background, and display each image on the screen. 
         *  
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Object doInBackground(Object... params) {
            //setProgressBarIndeterminateVisibility(true); 
            Bitmap bitmap = null;
            Bitmap newBitmap = null;
            Uri uri = null;            
            
            //MediaStore.Images.Media.
         
            // Set up an array of the Image ID column we want
            String[] projection = {
            		MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN};
            // Create the cursor pointing to the SDCard
            Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, // Which columns to return
                    null,       // Return all rows
                    null,       
                    null); 
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int size = cursor.getCount();
            // If size is 0, there are no images on the SD Card.
            if (size == 0) {
                //No Images available, post some message to the user
            }
            int imageID = 0;
            
            for (int i = 0; i < size; i++) {
                cursor.moveToPosition(i);
                
                imageID = cursor.getInt(columnIndex);
               Long timestamp = 1321403384000l;
               Long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
               uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID);
               //MediaStore.Images.Media.query(getContentResolver(), uri, projection).;
               if (date > timestamp)
                	{
                		
                		try {
                			bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                			if (bitmap != null) {
                				newBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                				bitmap.recycle();
                				if (newBitmap != null) {
                					publishProgress(new LoadedImage(newBitmap));
                				}
                			}
                } catch (IOException e) {
                    //Error fetching image, try to recover
                }
                	}
            }
            cursor.close();
            
            
            /*load flickr images*/
            InputStream is = null;
            for (Object url : urlsArray)  {
	            try {
	            	is = (InputStream) new URL((String) url).getContent();
	            } catch (MalformedURLException e) {
	            	// TODO Auto-generated catch block
	            	e.printStackTrace();
	            } catch (IOException e) {
	            	// TODO Auto-generated catch block
	            	e.printStackTrace();
	            }
           
	            bitmap = BitmapFactory.decodeStream(is);
	            if (bitmap != null) {
	            	newBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
	            	bitmap.recycle();
	            	if (newBitmap != null) {
	            		publishProgress(new LoadedImage(newBitmap));
	            	}
	            }
            }
            
            return null;
        }
        /**
         * Add a new LoadedImage in the images grid.
         *
         * @param value The image.
         */
        @Override
        public void onProgressUpdate(LoadedImage... value) {
            addImage(value);
        }
        /**
         * Set the visibility of the progress bar to false.
         * 
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Object result) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    /**
     * Adapter for our image files. 
     * 
     * @author Mihai Fonoage
     *
     */
    class ImageAdapter extends BaseAdapter {

        private Context mContext; 
        private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();

        public ImageAdapter(Context context) { 
            mContext = context; 
        } 

        public void addPhoto(LoadedImage photo) { 
            photos.add(photo); 
        } 

        public int getCount() { 
            return photos.size(); 
        } 

        public Object getItem(int position) { 
            return photos.get(position); 
        } 

        public long getItemId(int position) { 
            return position; 
        } 

        public View getView(int position, View convertView, ViewGroup parent) { 
            final ImageView imageView; 
            if (convertView == null) { 
                imageView = new ImageView(mContext); 
            } else { 
                imageView = (ImageView) convertView; 
            } 
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(1, 1, 1, 1);
            imageView.setImageBitmap(photos.get(position).getBitmap());
            return imageView; 
        } 
    }

    /**
     * A LoadedImage contains the Bitmap loaded for the image.
     */
    private static class LoadedImage {
        Bitmap mBitmap;

        LoadedImage(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
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
    
    private void load(OAuth oauth) {
		if (oauth != null) {
			new LoadUserTask(this, userIcon).execute(oauth);
			new LoadPhotostreamTask(this, listView).execute(oauth);
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
	
	public void setUser(User user) {
		textUserTitle.setText(user.getUsername());
		textUserName.setText(user.getRealName());
		textUserId.setText(user.getId());
	}
	
	public ImageView getUserIconImageView() {
		return this.userIcon;
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		OAuth savedToken = getOAuthToken();
		if (CALLBACK_SCHEME.equals(scheme) && (savedToken == null || savedToken.getUser() == null)) {
			Uri uri = intent.getData();
			String query = uri.getQuery();
			logger.debug("Returned Query: {}", query); //$NON-NLS-1$
			String[] data = query.split("&"); //$NON-NLS-1$
			if (data != null && data.length == 2) {
				String oauthToken = data[0].substring(data[0].indexOf("=") + 1); //$NON-NLS-1$
				String oauthVerifier = data[1]
						.substring(data[1].indexOf("=") + 1); //$NON-NLS-1$
				logger.debug("OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier); //$NON-NLS-1$

				OAuth oauth = getOAuthToken();
				if (oauth != null && oauth.getToken() != null && oauth.getToken().getOauthTokenSecret() != null) {
					GetOAuthTokenTask task = new GetOAuthTokenTask(this);
					task.execute(oauthToken, oauth.getToken().getOauthTokenSecret(), oauthVerifier);
				}
			}
		}

	}
    
    public void onOAuthDone(OAuth result) {
		if (result == null) {
			Toast.makeText(this,
					"Authorization failed", //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			User user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(this,
						"Authorization failed", //$NON-NLS-1$
						Toast.LENGTH_LONG).show();
				return;
			}
			String message = String.format(Locale.US, "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
					user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			Toast.makeText(this,
					message,
					Toast.LENGTH_LONG).show();
			saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			load(result);
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
	    	SharedPreferences sp = getSharedPreferences(PREFS_NAME,
					Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString(KEY_OAUTH_TOKEN, token);
			editor.putString(KEY_TOKEN_SECRET, tokenSecret);
			editor.putString(KEY_USER_NAME, userName);
			editor.putString(KEY_USER_ID, userId);
			editor.commit();
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

}
