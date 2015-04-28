package nl.imarinelife.lib;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class IMarineLife extends Application {
	
	
	public void onCreate() {
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
           .build();
        ImageLoader.getInstance().init(config);
    }


}
