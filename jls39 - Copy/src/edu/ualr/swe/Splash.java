///////////////////////////////////////////////////////////////////////////////////////////
//                                                   41 Post                             //
// Android: loading images from a remote location, SD card and from the Resources folder //
// Created by DimasTheDriver in 25/Jan/2011                                      		 //
// Availiable at:       http://www.41post.com/?p=2744                           		 //
///////////////////////////////////////////////////////////////////////////////////////////

package edu.ualr.swe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Loads images from SD card. 
 * 
 * @author Mihai Fonoage
 *
 */
public class Splash extends Activity {
	protected boolean _active = true;
	protected int _splashTime = 300; // time to display the splash screen in ms

/** Called when the activity is first created. */
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash);
 
    // thread for displaying the SplashScreen
    Thread splashTread = new Thread() {
        @Override
        public void run() {
            try {
                int waited = 0;
                while(_active && (waited < _splashTime)) {
                    sleep(100);
                    if(_active) {
                        waited += 100;
                    }
                }
            } catch(InterruptedException e) {
                // do nothing
            } finally {
                finish();
                Intent i = new Intent(Splash.this, LoadImagesFromSDCardActivity.class);
                startActivity(i);
                stop();
            }
        }
    };
    splashTread.start();
}
}