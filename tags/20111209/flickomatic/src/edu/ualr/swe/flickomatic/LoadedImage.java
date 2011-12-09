package edu.ualr.swe.flickomatic;

import android.graphics.Bitmap;

/**
 * A LoadedImage contains the Bitmap loaded for the image.
 */
    public class LoadedImage {
        Bitmap mBitmap;

        LoadedImage(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }