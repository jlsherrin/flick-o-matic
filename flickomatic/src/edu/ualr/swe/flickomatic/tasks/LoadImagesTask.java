package edu.ualr.swe.flickomatic.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import com.gmail.yuyang226.flickr.photos.Size;

import edu.ualr.swe.flickomatic.FlickOMaticActivity;
import edu.ualr.swe.flickomatic.FlickrHelper;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
//import android.widget.GridView;


//FOR UPLOADER
import com.gmail.yuyang226.flickr.uploader.*;
import edu.ualr.swe.flickomatic.R;

/**
     * Async task for loading the images from the SD card. 
     * 
     * @author Mihai Fonoage
     *
     */
public class LoadImagesTask extends AsyncTask<Object, Photo, Object> {
       // private GridView grid;
		private FlickOMaticActivity activity;		
		private OAuth oauth;
		
		private static final Logger logger = LoggerFactory.getLogger(LoadImagesTask.class);

		public LoadImagesTask(OAuth o, FlickOMaticActivity act) {
			this.activity = act;
			this.oauth = o;
		}
		
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
            PhotoList photos = null;
            //MediaStore.Images.Media.
         
            // Set up an array of the Image ID column we want
            String[] projection = {
            		MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN};
            // Create the cursor pointing to the SDCard
            Cursor cursor = activity.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
               Long timestamp = 1321403384000l; //TODO: load timestamp dynamically
               Long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
               uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID);
               //MediaStore.Images.Media.query(getContentResolver(), uri, projection).;
               if (date > timestamp)
                	{               		
                		try {
                			bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri));
                			if (bitmap != null) {
                				logger.debug("LoadImages bitmap loaded");
                				newBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                				bitmap.recycle();
                				if (newBitmap != null) {
                					publishProgress(new edu.ualr.swe.flickomatic.Photo(newBitmap));
                					//Uploader uploader = new Uploader(R.string.);
                					//Bitmap bmp = intent.getExtras().get("data");
                					ByteArrayOutputStream stream = new ByteArrayOutputStream();
                					newBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                					byte[] byteArray = stream.toByteArray();
                					
                					//photos.add(new edu.ualr.swe.flickomatic.Photo(newBitmap));
                					logger.debug("LoadImages progress published");
                				}
                			}
                } catch (IOException e) {
                    //Error fetching image, try to recover
                }
                	}
            }
            cursor.close();            
            
            /*load flickr images*/
			OAuthToken token = oauth.getToken();
			Flickr f = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(),token.getOauthTokenSecret());
			logger.debug("1");
			Set<String> extras = new HashSet<String>();
			extras.add("url_sq"); //$NON-NLS-1$
			extras.add("url_l"); //$NON-NLS-1$
			extras.add("views"); //$NON-NLS-1$
			User user = oauth.getUser();
			
			try {				
				PhotoList photoList = f.getPeopleInterface().getPhotos(user.getId(), extras, 20, 1);
				for(Photo photo : photoList){					
					bitmap = BitmapFactory.decodeStream(f.getPhotosInterface().getImageAsStream(photo, Size.THUMB));
					if (bitmap != null) {
						newBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
						bitmap.recycle();
						logger.debug("2");
						if (newBitmap != null) {
							//publishProgress(photo);
							//photos.add(photo);
							logger.debug("3");
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//activity.addImage(photos);
            return null;
        }
        private void publishProgress(edu.ualr.swe.flickomatic.Photo photo) {
			// TODO Auto-generated method stub
			
		}

		/**
         * Add a new Photo in the images grid.
         *
         * @param value The image.
         */
        public void onProgressUpdate(PhotoList value) {
            //activity.addImage(value);
            logger.debug("LoadImages value sent to activity");
        }
        
        public void onProgressUpdate(edu.ualr.swe.flickomatic.Photo value){
        	
        }
        /**
         * Set the visibility of the progress bar to false.
         * 
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Object result) {
            activity.setProgressBarIndeterminateVisibility(false);
        }
    }