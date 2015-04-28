package nl.imarinelife.lib.utility.wheel;

import java.util.ArrayList;
import java.util.List;

import nl.imarinelife.lib.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * @author igor.kostromin 16.05.13 9:03
 * 
 *         Custom time picker based on wheel control.
 */
public class TimeWheel extends LinearLayout {

	public static final int		DEFAULT_VISIBLE_ITEMS	= 3;

	
	// wheel controls to select birth date
	private WheelView			wheelHour;
	private WheelView			wheelMinute;

	public static interface ITimeChangedListener {
		void onChanged(TimeWheel sender, int oldMinute, int oldHour, int minute, int hour);
	}

	private List<ITimeChangedListener>	timeChangedListeners	= new ArrayList<ITimeChangedListener>();

	public void addTimeChangedListener(ITimeChangedListener listener) {
		if (null == listener)
			throw new IllegalArgumentException("yesListener is null");
		timeChangedListeners.add(listener);
	}

	public void removetimeChangedListener(ITimeChangedListener listener) {
		if (null == listener)
			throw new IllegalArgumentException("yesListener is null");
		timeChangedListeners.remove(listener);
	}

	private void raisetimeChangedEvent(int oldMinute, int oldHour, int minute, int hour) {
		if (!timeChangedListeners.isEmpty()) {
			List<ITimeChangedListener> copy = new ArrayList<ITimeChangedListener>(timeChangedListeners);
			for (ITimeChangedListener listener : copy) {
				listener.onChanged(this,
					oldMinute,
					oldHour,
					minute,
					hour);
			}

		}
	}

	public TimeWheel(Context context) {
		super(context);
		init(context);
	}

	public TimeWheel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/*
	 * public DateWheel(Context context, AttributeSet attrs, int defStyle) {
	 * super(context, attrs, defStyle); init(context); }
	 */

	/**
	 * Returns selected day.
	 * 
	 * @return
	 */
	public int getMinute() {
		return wheelMinute.getCurrentItem();
	}

	/**
	 * Returns selected month.
	 * 
	 * @return
	 */
	public int getHour() {
		return wheelHour.getCurrentItem();
	}

	public void setMinute(int minute) {
		if (minute < 0 || minute > 59)
			throw new IllegalArgumentException("minute should be in the range of 0-59");
		wheelMinute.setCurrentItem(minute);
	}

	public void setHour(int hour) {
		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException("hour should be in the range of 0-23");
		wheelHour.setCurrentItem(hour);
	}

	/**
	 * Gets count of visible items
	 * 
	 * @return
	 */
	public int getVisibleItems() {
		return wheelHour.getVisibleItems();
	}

	/**
	 * Sets the desired count of visible items.
	 * 
	 * @param count
	 */
	public void setVisibleItems(int count) {
		wheelMinute.setVisibleItems(count);
		wheelHour.setVisibleItems(count);

		// trigger re-measuring
		this.requestLayout();
	}

	private void init(final Context ctx) {
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.timewheels,
			this,
			true);

		wheelHour = (WheelView) findViewById(R.id.wheelHour);
		wheelHour.setTag("wheelHour"); // to debug inside wheel control code,
										// can be safely removed

		wheelHour.setViewAdapter(new NumericWheelAdapter(ctx, 0, 23, "%02d"));
		wheelHour.setCyclic(true);
		wheelHour.setMinimumWidth(25);
		wheelHour.setCurrentItem(0);
		wheelHour.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
		wheelHour.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldHour, int newHour) {
				raisetimeChangedEvent(oldHour,
					getMinute(),
					newHour,
					getMinute());
			}
		});

		wheelMinute = (WheelView) findViewById(R.id.wheelMinute);
		wheelMinute.setTag("wheelMinute");
		wheelMinute.setViewAdapter(new NumericWheelAdapter(ctx, 0, 59, "%02d"));
		wheelMinute.setCyclic(true);
		wheelHour.setMinimumWidth(25);
		wheelMinute.setCurrentItem(0);
		wheelMinute.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
		wheelMinute.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldMinute, int newMinute) {
				raisetimeChangedEvent(getHour(),
					oldMinute,
					getHour(),
					newMinute);
			}
		});

	}


}
