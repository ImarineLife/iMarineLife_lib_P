package nl.imarinelife.lib.utility;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class DivingLogGestureListener extends GestureDetector.SimpleOnGestureListener {

	private static final int	SWIPE_MIN_DISTANCE			= 20;
	private static final int	SWIPE_MAX_OFF_PATH			= 100;
	private static final int	SWIPE_THRESHOLD_VELOCITY	= 100;
	
	private static final String TAG ="DivingLogGestureLstener";
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,

	float velocityY) {
		Log.d(TAG,"onFling- Swipe");

		if(e2 != null && e1 != null) {
			float dX = e2.getX() - e1.getX();

			float dY = e1.getY() - e2.getY();

			if (Math.abs(dY) < SWIPE_MAX_OFF_PATH &&

					Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY &&

					Math.abs(dX) >= SWIPE_MIN_DISTANCE) {

				if (dX > 0) {
					onLeftSwipe();
				} else {
					onRightSwipe();
				}

				return true;

			} else if (Math.abs(dX) < SWIPE_MAX_OFF_PATH &&

					Math.abs(velocityY) >= SWIPE_THRESHOLD_VELOCITY &&

					Math.abs(dY) >= SWIPE_MIN_DISTANCE) {

				if (dY > 0) {
					onUpSwipe();
				} else {
					onDownSwipe();
				}

				return true;

			}

		}
		return false;
	}

	protected void onLeftSwipe(){};
	protected void onRightSwipe(){};
	protected void onUpSwipe(){};
	protected void onDownSwipe(){};
}
