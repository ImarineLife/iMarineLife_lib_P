package nl.imarinelife.lib.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class RobotoAutoCompleteEditText extends AutoCompleteTextView {

	public RobotoAutoCompleteEditText(Context context) {
		super(context);
		setFont();
	}

	public RobotoAutoCompleteEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont();
	}

	public RobotoAutoCompleteEditText(Context context, AttributeSet attrs, int defStyle) {
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
