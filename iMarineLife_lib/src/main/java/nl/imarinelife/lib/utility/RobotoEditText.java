package nl.imarinelife.lib.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class RobotoEditText extends EditText
{
	    public RobotoEditText(Context context) {
	        super(context);
	        setFont();
	    }
	    public RobotoEditText(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        setFont();
	    }
	    public RobotoEditText(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	        setFont();
	    }
	 
	    private void setFont() {
			setTypeface(RobotoFont.bold,
				Typeface.BOLD);

			setTypeface(RobotoFont.italic,
				Typeface.BOLD_ITALIC);

			setTypeface(RobotoFont.regular,
				Typeface.NORMAL);

			setTypeface(RobotoFont.lightitalic,
				Typeface.ITALIC);
	    }
	    
	    
	
}
