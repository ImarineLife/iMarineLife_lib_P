package nl.imarinelife.lib.utility.wheel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.R;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * @author igor.kostromin 16.05.13 9:03
 * 
 *         Custom date picker based on wheel control.
 */
public class DateWheel extends LinearLayout {

	private static final String	TAG						= "DateWheel";
	public static final int		DEFAULT_VISIBLE_ITEMS	= 3;

	public static final int		DEFAULT_MIN_YEAR		= 1900;
	public static final int		DEFAULT_MAX_YEAR		= 2050;

	public static final int		DEFAULT_YEAR			= 2000;
	public static final int		DEFAULT_DAY				= 15;
	public static final int		DEFAULT_MONTH			= 7;
	
	private static Resources	resources				= null;
	// state
	private int					minYear;
	private int					maxYear;

	// last selected manually day, remembered for correct restore selected day
	// while user scrolls months list
	private int					lastSelectedDay;

	// flag to disable selected day change yesListener
	private boolean				dayChangeListenerDisabled;

	// wheel controls to select date
	private WheelView			wheelYear;
	private WheelView			wheelMonth;
	private WheelView			wheelDay;
	private WheelView			wheelDayName;

	public static interface IDateChangedListener {
		void onChanged(DateWheel sender, int oldDay, int oldMonth, int oldYear, int day, int month, int year);
	}

	private List<IDateChangedListener>	dateChangedListeners	= new ArrayList<IDateChangedListener>();

	public void addDateChangedListener(IDateChangedListener listener) {
		if (null == listener)
			throw new IllegalArgumentException("yesListener is null");
		dateChangedListeners.add(listener);
	}

	public void removeDateChangedListener(IDateChangedListener listener) {
		if (null == listener)
			throw new IllegalArgumentException("yesListener is null");
		dateChangedListeners.remove(listener);
	}

	private void raiseDateChangedEvent(int oldDay, int oldMonth, int oldYear, int day, int month, int year) {
		if (!dateChangedListeners.isEmpty()) {
			List<IDateChangedListener> copy = new ArrayList<IDateChangedListener>(dateChangedListeners);
			for (IDateChangedListener listener : copy) {
				listener.onChanged(this,
					oldDay,
					oldMonth,
					oldYear,
					day,
					month,
					year);
			}

		}
	}

	public DateWheel(Context context) {
		super(context);
		init(context);
	}

	public DateWheel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/*
	 * public DateWheel(Context context, AttributeSet attrs, int defStyle) {
	 * super(context, attrs, defStyle); init(context); }
	 */

	public int getMinYear() {
		return minYear;
	}

	/**
	 * Use this method to set years interval that will be awailable.
	 * 
	 * @param minYear
	 * @param maxYear
	 */
	public void setMinMaxYears(int minYear, int maxYear) {
		if (minYear < 0)
			throw new IllegalArgumentException("minYear");
		if (maxYear < 0)
			throw new IllegalArgumentException("maxYear");
		if (minYear > maxYear)
			throw new IllegalArgumentException("minYear should be <= maxYear");
		//
		if (this.minYear != minYear || this.maxYear != maxYear) {
			this.minYear = minYear;
			this.maxYear = maxYear;

			// reinit wheelYear
			int year = getYear();
			NumericWheelAdapter adapter = new NumericWheelAdapter(this.getContext(), minYear, maxYear);
			wheelYear.setViewAdapter(adapter);
			if (year >= minYear && year <= maxYear) {
				setYear(year);
			} else {
				setYear(minYear);
			}
		}
	}

	public int getMaxYear() {
		return maxYear;
	}

	/**
	 * Returns selected day.
	 * 
	 * @return
	 */
	public int getDay() {
		return wheelDay.getCurrentItem() + 1;
	}

	/**
	 * Returns selected month.
	 * 
	 * @return
	 */
	public int getMonth() {
		return wheelMonth.getCurrentItem() + 1;
	}

	/**
	 * Returns selected year.
	 * 
	 * @return
	 */
	public int getYear() {
		return wheelYear.getCurrentItem() + minYear;
	}

	public void setDay(int day) {
		if (day < 1 || day > 31)
			throw new IllegalArgumentException("day should be between 1 and 31");
		int month = getMonth();
		int year = getYear();
		int daysInMonth = getDaysCountInMonth(year,
			month);
		int dayToSet = Math.min(day,
			daysInMonth);
		wheelDay.setCurrentItem(dayToSet - 1);
	}

	public void setMonth(int month) {
		if (month < 1 || month > 12)
			throw new IllegalArgumentException("month should be between 1 and 12");
		wheelMonth.setCurrentItem(month - 1);
	}

	public void setYear(int year) {
		if (year < minYear || year > maxYear)
			throw new IllegalArgumentException("year should be between minYear and maxYear");
		wheelYear.setCurrentItem(year - minYear);
	}

	/**
	 * Gets count of visible items
	 * 
	 * @return
	 */
	public int getVisibleItems() {
		return wheelDay.getVisibleItems();
	}

	/**
	 * Sets the desired count of visible items.
	 * 
	 * @param count
	 */
	public void setVisibleItems(int count) {
		wheelDay.setVisibleItems(count);
		wheelMonth.setVisibleItems(count);
		wheelYear.setVisibleItems(count);
		wheelDayName.setVisibleItems(count);

		// trigger remeasuring
		this.requestLayout();
	}

	private void init(final Context ctx) {
		resources = LibApp.getCurrentResources();
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.datewheels,
			this,
			true);

		minYear = DEFAULT_MIN_YEAR;
		maxYear = DEFAULT_MAX_YEAR;

		dayChangeListenerDisabled = false;
		lastSelectedDay = DEFAULT_DAY;

		wheelYear = (WheelView) findViewById(R.id.wheelYear);
		wheelYear.setTag("wheelYear"); // to debug inside wheel control code,
										// can be safely removed
		NumericWheelAdapter adapter = new NumericWheelAdapter(ctx, minYear, maxYear);
		wheelYear.setViewAdapter(adapter);
		wheelYear.setCurrentItem(DEFAULT_YEAR - minYear);
		wheelYear.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
		wheelYear.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int oldDay = getDay();
				// special logic for February
				int oldYear = oldValue + minYear;
				int newYear = newValue + minYear;
				GregorianCalendar gregorianCalendar = new GregorianCalendar();

				boolean leapOldYear = gregorianCalendar.isLeapYear(oldYear);
				boolean leapNewYear = gregorianCalendar.isLeapYear(newYear);
				if (leapNewYear != leapOldYear) {
					int month = wheelMonth.getCurrentItem() + 1;
					if (2 == month) {
						int daysCount = getDaysCountInMonth(newYear,
							2);
						int daysCountPrev = getDaysCountInMonth(oldYear,
							2);
						int currentDay = wheelDay.getCurrentItem() + 1;
						NumericWheelAdapter adapter = new NumericWheelAdapter(ctx, 1, daysCount);
						wheelDay.setViewAdapter(adapter);
						updateWheelDay(daysCount,
							daysCountPrev,
							currentDay,
							lastSelectedDay);
					}
				}
				int newDay = getDay();
				int month = getMonth();
				raiseDateChangedEvent(oldDay,
					month,
					oldYear,
					newDay,
					month,
					newYear);
				setDay();
			}
		});

		wheelMonth = (WheelView) findViewById(R.id.wheelMonth);
		wheelMonth.setTag("wheelMonth");
		MonthsAdapter m_adapter = new MonthsAdapter(ctx);
		wheelMonth.setViewAdapter(m_adapter);
		wheelMonth.setCurrentItem(DEFAULT_MONTH - 1);
		wheelMonth.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
		wheelMonth.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int oldDay = getDay();
				//
				int year = wheelYear.getCurrentItem() + minYear;
				int oldMonth = oldValue + 1;
				int newMonth = newValue + 1;
				int daysCount = getDaysCountInMonth(year,
					newMonth);
				int daysCountPrev = getDaysCountInMonth(year,
					oldMonth);
				if (daysCountPrev != daysCount) {
					int currentDay = wheelDay.getCurrentItem() + 1;
					NumericWheelAdapter adapter = new NumericWheelAdapter(ctx, 1, daysCount);
					wheelDay.setViewAdapter(adapter);
					updateWheelDay(daysCount,
						daysCountPrev,
						currentDay,
						lastSelectedDay);
				}
				//
				int newDay = getDay();
				raiseDateChangedEvent(oldDay,
					oldMonth,
					year,
					newDay,
					newMonth,
					year);
				setDay();

			}

		});

		wheelDay = (WheelView) findViewById(R.id.wheelDay);
		wheelDay.setTag("wheelDay");
		NumericWheelAdapter d_adapter = new NumericWheelAdapter(ctx, 1, getDaysCountInMonth(DEFAULT_YEAR,
			DEFAULT_MONTH));
		wheelDay.setViewAdapter(d_adapter);
		wheelDay.setCurrentItem(DEFAULT_DAY - 1);
		wheelDay.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
		wheelDay.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!dayChangeListenerDisabled) {
					lastSelectedDay = newValue + 1;
					//
					int oldDay = oldValue + 1;
					int newDay = newValue + 1;
					int year = getYear();
					int month = getMonth();
					raiseDateChangedEvent(oldDay,
						month,
						year,
						newDay,
						month,
						year);

				}
				setDay();
			}
		});

		wheelDayName = (WheelView) findViewById(R.id.wheelDayName);
		wheelDayName.setTag("wheelDayName");
		String[] items = { resources.getString(R.string.sunday), resources.getString(R.string.monday),
				resources.getString(R.string.tuesday), resources.getString(R.string.wednesday),
				resources.getString(R.string.thursday), resources.getString(R.string.friday),
				resources.getString(R.string.saturday) };
		ArrayWheelAdapter<String> s_adapter = new ArrayWheelAdapter<String>(ctx, items);
		wheelDayName.setViewAdapter(s_adapter);

		wheelDayName.setCurrentItem(0);
		wheelDayName.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
		wheelDayName.setCyclic(true);
		wheelDayName.setEnabled(false);
	}

	protected void setDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(getYear(),
			getMonth()+1,
			getDay());
		int day = cal.get(Calendar.DAY_OF_WEEK);
		Log.d(TAG,"set day ["+day+"] for ["+getDay()+"-"+getMonth()+1+"-"+getYear()+"]" );
		wheelDayName.setCurrentItem(day);		
	}

	/**
	 * 
	 * @param daysCount
	 * @param daysCountPrev
	 * @param currentDay
	 * @param lastSelectedDay
	 */
	private void updateWheelDay(int daysCount, int daysCountPrev, int currentDay, int lastSelectedDay) {
		if (daysCountPrev > daysCount) {
			if (currentDay > daysCount) {
				dayChangeListenerDisabled = true;
				wheelDay.setCurrentItem(daysCount - 1);
				dayChangeListenerDisabled = false;
			}
		} else if (daysCountPrev < daysCount) {
			if (currentDay != lastSelectedDay) {
				dayChangeListenerDisabled = true;
				if (lastSelectedDay <= daysCount)
					wheelDay.setCurrentItem(lastSelectedDay - 1);
				else {
					// if switch from month with 28 days to month with 29 (or
					// 30),
					// but lastSelectedDay is 31, we have to set current item =
					// 29 (or 30), not 31
					wheelDay.setCurrentItem(daysCount - 1);
				}
				dayChangeListenerDisabled = false;
			}
		}
	}

	/**
	 * 
	 * @param year
	 * @param month
	 *            1 - January, 2 - February, ..
	 * @return
	 */
	private static int getDaysCountInMonth(int year, int month) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		switch (month) {
			case 4:
			case 6:
			case 9:
			case 11:
				return 30;
			case 2:
				return gregorianCalendar.isLeapYear(year)
						? 29
						: 28;
			default:
				return 31;
		}
	}

	public static class MonthsAdapter extends NumericWheelAdapter {

		public MonthsAdapter(Context context) {
			super(context, 1, 12);
			setItemResource(R.layout.wheel_text_items);
		}

		@Override
		public CharSequence getItemText(int index) {
			if (index >= 0 && index < getItemsCount()) {
				int value = 1 + index;
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.MONTH,
					index);

				switch (value) {
					case 1:
						return resources.getString(R.string.jan);
					case 2:
						return resources.getString(R.string.feb);
					case 3:
						return resources.getString(R.string.mrt);
					case 4:
						return resources.getString(R.string.apr);
					case 5:
						return resources.getString(R.string.mei);
					case 6:
						return resources.getString(R.string.jun);
					case 7:
						return resources.getString(R.string.jul);
					case 8:
						return resources.getString(R.string.aug);
					case 9:
						return resources.getString(R.string.sep);
					case 10:
						return resources.getString(R.string.okt);
					case 11:
						return resources.getString(R.string.nov);
					case 12:
						return resources.getString(R.string.dec);
				}
			}
			return null;
		}
	}
}
