package nl.imarinelife.lib.utility;

import nl.imarinelife.lib.MainActivity;
import android.graphics.Typeface;

public class RobotoFont {
	
        public static Typeface bold = Typeface.createFromAsset(MainActivity.me.getAssets(),
                "fonts/Roboto-Bold.ttf");
        
        public static Typeface italic = Typeface.createFromAsset(MainActivity.me.getAssets(),
                "fonts/Roboto-BoldItalic.ttf");
        
        public static Typeface regular = Typeface.createFromAsset(MainActivity.me.getAssets(),
                "fonts/Roboto-Regular.ttf");
  
        public static Typeface lightitalic = Typeface.createFromAsset(MainActivity.me.getAssets(),
                "fonts/Roboto-Italic.ttf");

}
