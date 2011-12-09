package edu.ualr.swe.flickomatic.images;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.photos.Photo;
import com.gmail.yuyang226.flickr.photos.PhotoList;
import com.gmail.yuyang226.flickr.photos.Size;

import edu.ualr.swe.flickomatic.tasks.ImageDownloadTask;
import edu.ualr.swe.flickomatic.images.ImageUtils.DownloadedDrawable;

/**
     * Adapter for our image files. 
     * 
     * @author Mihai Fonoage
     *
     */
    public class ImageAdapter extends BaseAdapter {

        private Context mContext; 
        private PhotoList photos = new PhotoList();

        public ImageAdapter(Context context) { 
            mContext = context; 
        } 

        public void addPhoto(Photo photo) { 
            photos.add(photo); 
			notifyDataSetChanged();
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
            //imageView.setImageBitmap(BitmapFactory.decodeStream(f.getPhotosInterface().getImageAsStream(photos.get(position)), Size.THUMB));
            Photo photo = photos.get(position);
            ImageDownloadTask task = new ImageDownloadTask(imageView);
        	Drawable drawable = new DownloadedDrawable(task);
        	imageView.setImageDrawable(drawable);
        	task.execute(photo.getSmallSquareUrl());
            return imageView; 
        }

		public void addPhoto(edu.ualr.swe.flickomatic.Photo value) {
			// TODO Auto-generated method stub
			
		}

    }