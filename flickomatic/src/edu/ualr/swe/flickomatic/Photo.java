package edu.ualr.swe.flickomatic;

import android.graphics.Bitmap;

/**
     * A Photo contains the Bitmap loaded for the image.
     */
    public class Photo {
        Bitmap mBitmap;

        public Photo(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }