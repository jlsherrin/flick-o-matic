package edu.ualr.swe.flickomatic.tasks;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.GridView;
import edu.ualr.swe.flickomatic.FlickOMaticActivity;
import edu.ualr.swe.flickomatic.images.ImageAdapter;


import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import com.gmail.yuyang226.flickr.photos.Size;


import edu.ualr.swe.flickomatic.FlickrHelper;
import edu.ualr.swe.flickomatic.images.LazyAdapter;

public class LoadPhotostreamTask extends AsyncTask<OAuth, Void, PhotoList>{

	private GridView grid;
	private Activity activity;

	public LoadPhotostreamTask(Activity activity,
			GridView gridView) {
		this.activity = activity;
		this.grid = gridView;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected PhotoList doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(), 
				token.getOauthTokenSecret());
		Set<String> extras = new HashSet<String>();
		extras.add("url_sq"); //$NON-NLS-1$
		extras.add("url_l"); //$NON-NLS-1$
		extras.add("views"); //$NON-NLS-1$
		User user = arg0[0].getUser();
		try {
			PhotoList photoList = f.getPeopleInterface().getPhotos(user.getId(), extras, 20, 1);
			for(Photo photo : photoList){
				
				Bitmap bitmap = BitmapFactory.decodeStream(f.getPhotosInterface().getImageAsStream(photo, Size.THUMB));
				if (bitmap != null) {
					Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
					bitmap.recycle();
					if (newBitmap != null) {
						publishProgress();
					}
				}
			}
			return f.getPeopleInterface().getPhotos(user.getId(), extras, 20, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	protected void onPostExecute(PhotoList result) {
		if (result != null) {
			//LazyAdapter adapter = new LazyAdapter(this.activity, result);
			//ImageAdapter adapter =  new ImageAdapter(getApplicationContext());
			//this.grid.setAdapter(adapter);
		}
	}
	
}