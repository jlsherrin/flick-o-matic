package edu.ualr.swe;

import android.app.Activity;
import android.os.Bundle;
import com.loopj.android*;

public class PhotosActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SmartImageView myImage = (SmartImageView) this.findViewById(R.id.my_image);
    }
}