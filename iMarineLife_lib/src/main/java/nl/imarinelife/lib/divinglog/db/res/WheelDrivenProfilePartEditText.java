package nl.imarinelife.lib.divinglog.db.res;

import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.DivingLogStayEntryFragment;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.db.dive.DiveProfilePart;
import nl.imarinelife.lib.utility.dialogs.NumberWheelDialogFragment;
import nl.imarinelife.lib.utility.dialogs.NumberWheelDialogFragment.OnCompleteListener;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class WheelDrivenProfilePartEditText {
	private static String						TAG			= "WheelDrivenProfielPartEditText";
	public EditText								numberEditText;
	public DiveProfilePart						currentPart;
	private Dive								currentDive;
	private OnCompleteListener					listener	= null;
	private final DivingLogStayEntryFragment	stayFragment;

	@SuppressWarnings("serial")
	public WheelDrivenProfilePartEditText(EditText editText, DiveProfilePart part, Dive dive,
			DivingLogStayEntryFragment fragment) {
		this.numberEditText = editText;
		this.currentPart = part;
		this.currentDive = dive;
		this.stayFragment = fragment;

		this.listener = new OnCompleteListener() {

			@Override
			public void onCompleteNumberWheel(int value) {
				Log.d(TAG,
					"onCompleteNumberWheel: setting value ["+value+"]");
				numberEditText.setText(value + "");
				currentPart.stayValueInMeters = value;
				Log.d(TAG, currentDive+"");
				currentDive.insertProfilePart(currentPart);
				stayFragment.resetTotalSummed();
				MainActivity.me.currentDive.setChanged(true);
			}
		};

		numberEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NumberWheelDialogFragment frag = getNumberWheelDialogFragmentForProfilePartEditText();
				stayFragment.showDialog(frag);
			}
		});

	}

	protected NumberWheelDialogFragment getNumberWheelDialogFragmentForProfilePartEditText() {

		final int layoutId;
		final int numberWheelId;
		final int choosebuttonId;
		final int cancelbuttonId;
		layoutId = R.layout.divinglog_identification_divenr_dialog;
		numberWheelId = R.id.dl_id_divenrdialog_wheel_id;
		choosebuttonId = R.id.dl_id_divenrdialog_choose_button_id;
		cancelbuttonId = R.id.dl_id_divenrdialog_cancel_button_id;

		int currentNumber = currentPart.stayValueInMeters;
		NumberWheelDialogFragment frag = NumberWheelDialogFragment.newInstance(currentNumber,
			0,
			500,
			5,
			null,
			layoutId,
			numberWheelId,
			choosebuttonId,
			cancelbuttonId,
			DialogFragment.STYLE_NO_TITLE,
			R.style.iMarineLifeDialogTheme);
		frag.setOnCompleteListener(listener);
		return frag;

	}

}
