package nl.imarinelife.lib.utility.mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.mail.internet.InternetAddress;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.db.dive.DiveProfilePart;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.divinglog.sightings.Sighting;
import nl.imarinelife.lib.divinglog.sightings.SightingsSender;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.SerializableSparseArray;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MailSightingsSender extends SightingsSender {

	public static final String TAG = "MailSightingsSender";

	HSSFCellStyle turned;
	HSSFCellStyle bold;
	HSSFCellStyle simple;

	@Override
	protected boolean sendSightings(Dive dive, Context context)
			throws Exception {
		Log.d(TAG, "sendSightings[" + dive.getDiveNr() + "]");
		try {
			String fileName = createExcelFromDive(dive, context);

			String content = LibApp.getMailBody();
			String mailTo = LibApp.getMailTo();
			String mailFrom = LibApp.getMailFrom();
			String mailFromPwd = LibApp.getMailFromPassword();
			
			GMailSender sender = new GMailSender(mailFrom, mailFromPwd);
			String subject = "waarnemingen "
					+ Preferences.getString(Preferences.USER_NAME, "") + " ("
					+ dive.getFormattedDate() + " " + dive.getFormattedTime()
					+ ")";
		
			boolean sendToMeInCC = Preferences
					.getBooleanFromDefaultSharedPreferences(Preferences.SEND_ME);
			if (sendToMeInCC) {
				String cc = null;
				cc = Preferences.getString(Preferences.USER_EMAIL, null);
				sender.sendMail(subject, content, mailFrom, mailTo, cc,
						fileName);
			} else {
				sender.sendMail(subject, content, mailFrom, mailTo, fileName);
			}

			File file = new File(fileName);
			if (file.exists())
				file.delete();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "failed creating excel or sending mail", e);
			throw e;
		}
	}

	private String createExcelFromDive(Dive dive, Context context) {
		FieldGuideAndSightingsEntryDbHelper dbHelper = FieldGuideAndSightingsEntryDbHelper
				.getInstance(context);
		Cursor cursor = dbHelper.queryFieldGuideFilledForDive(dive.getDiveNr());

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(LibApp.getCurrentCatalogName());
		turned = workbook.createCellStyle();
		bold = workbook.createCellStyle();
		simple = workbook.createCellStyle();
		Font normalFont = simple.getFont(workbook);

		Font boldFont = bold.getFont(workbook);
		boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		turned.setRotation((short) 90);
		turned.setAlignment(HSSFCellStyle.VERTICAL_TOP);
		turned.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		turned.setFont(boldFont);
		turned.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		bold.setFont(boldFont);
		bold.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		simple.setFont(normalFont);
		simple.setFillForegroundColor(HSSFColor.WHITE.index);

		int rownr = fillAlgemeneGegevens(sheet, dive);
		rownr = fillSightings(sheet, dive, cursor, rownr);
		cursor.close();
		return saveWorkBook(workbook, dive, context);
	}

	private String saveWorkBook(HSSFWorkbook workbook, Dive dive,
			Context context) {
		String prefix = LibApp.getCurrentResources().getString(R.string.sightings);
		String fileName = prefix + "_"
				+ Preferences.getString(Preferences.USER_NAME, "") + "_"
				+ dive.getFormattedDate().replace("-", "")
				+ dive.getFormattedTime().replace(":", "") + ".xls";
		File file = new File(context.getFilesDir(), fileName);
		if (file.exists())
			file.delete();

		try {
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();
			System.out.println("Excel written successfully..");

		} catch (FileNotFoundException e) {
			Log.e(TAG, "saveWorkBook", e);
		} catch (IOException e) {
			Log.e(TAG, "saveWorkBook", e);
		}

		return file.getAbsolutePath();
	}

	private int fillSightings(HSSFSheet sheet, Dive dive, Cursor cursor,
			int rownr) {
		final int COL_FIRST = 0;
		final int COL_NUMBER = 1;
		final int COL_NED = 2;
		final int COL_SCIENTIFIC = 3;
		final int COL_START = 4;

		int firstStartingRow = rownr;
		int groupStartingRow = 0;
		String lastGroup = null;
		int colnr = 4;
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				Sighting sighting = FieldGuideAndSightingsEntryDbHelper
						.getSightingFromCursor(cursor);

				Row row = sheet.createRow(rownr);
				if (lastGroup == null
						|| !sighting.fg_entry.getShowGroupName().equals(
								lastGroup)) {
					lastGroup = sighting.fg_entry.getShowGroupName();
					if (firstStartingRow != rownr) {
						sheet.addMergedRegion(new CellRangeAddress(
								groupStartingRow, rownr - 1, 0, 0));
					}
					groupStartingRow = rownr;
					Cell cell = row.createCell(COL_FIRST);
					cell.setCellValue(sighting.fg_entry.getGroupName());
					cell.setCellStyle(turned);
					// header
					cell = row.createCell(COL_NUMBER);
					cell.setCellValue("Code");
					cell.setCellStyle(bold);
					cell = row.createCell(COL_NED);
					cell.setCellValue("Nederlandse naam");
					cell.setCellStyle(bold);
					cell = row.createCell(COL_SCIENTIFIC);
					cell.setCellValue("Wetenschappelijke naam");
					cell.setCellStyle(bold);
					colnr = COL_START;
					for (String choice : LibApp
							.getCurrentCatalogSightingChoices()) {
						cell = row.createCell(colnr);
						cell.setCellValue(choice);
						cell.setCellStyle(bold);
						colnr++;
					}
					for (String choice : LibApp
							.getCurrentCatalogCheckBoxChoices()) {
						cell = row.createCell(colnr);
						cell.setCellValue(choice);
						cell.setCellStyle(bold);
						colnr++;
					}
					cell = row.createCell(colnr);
					cell.setCellValue("Opmerkingen");
					cell.setCellStyle(bold);
					rownr++;
				}
				row = sheet.createRow(rownr);
				Cell cell = row.createCell(COL_NUMBER);
				cell.setCellValue(sighting.fg_entry.id);
				cell.setCellStyle(simple);
				cell = row.createCell(COL_NED);
				cell.setCellValue(sighting.fg_entry.getShowCommonName());
				cell.setCellStyle(simple);
				cell = row.createCell(COL_SCIENTIFIC);
				cell.setCellValue(sighting.fg_entry.latinName);
				cell.setCellStyle(simple);

				String defaultValue = null;
				if (dive.getDiveDefaultChoice() != null) {
					defaultValue = dive.getDiveDefaultChoice();
				} else {
					defaultValue = Preferences.getString(
							Preferences.PERSONAL_DEFAULT_CHOICE,
							LibApp.getCurrentCatalogDefaultChoice());
				}
				String value = null;
				if (sighting.data == null
						|| sighting.data.sightingValue == null) {
					value = defaultValue;
				} else {
					value = sighting.data.sightingValue;
				}
				colnr = COL_START;
				for (String choice : LibApp.getCurrentCatalogSightingChoices()) {
					if (choice.equals(value)) {
						cell = row.createCell(colnr);
						cell.setCellValue("x");
						cell.setCellStyle(simple);
					}
					colnr++;
				}

				List<String> checked = sighting.data == null ? null
						: sighting.data.csCheckedValues;
				for (String choice : LibApp.getCurrentCatalogCheckBoxChoices()) {

					if (checked != null && checked.contains(choice)) {
						cell = row.createCell(colnr);
						cell.setCellValue("x");
						cell.setCellStyle(simple);
					}
					colnr++;
				}

				cell = row.createCell(colnr);
				cell.setCellValue(((sighting.data != null ? sighting.data.remarks
						: "")));
				cell.setCellStyle(simple);

				rownr++;
			} while (cursor.moveToNext());
			if (firstStartingRow != rownr) {
				sheet.addMergedRegion(new CellRangeAddress(groupStartingRow,
						rownr - 1, 0, 0));
			}

		}

		return rownr;
	}

	private int fillAlgemeneGegevens(HSSFSheet sheet, Dive dive) {
		final int COL_FIRST = 0;
		final int COL_NAME = 2;
		final int COL_VALUE = 3;

		int rownr = 0;
		Row row = sheet.createRow(rownr);
		Cell cell = row.createCell(COL_FIRST);
		cell.setCellValue("Algemene informatie");
		cell.setCellStyle(turned);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Project");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(LibApp.getProject());
		cell.setCellStyle(bold);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Formulier");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(LibApp.getCurrentCatalogName() + " "
				+ LibApp.getCurrentCatalogVersion());
		cell.setCellStyle(simple);
		rownr++;
		rownr++;

		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Naam Waarnemer");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(Preferences.getString(Preferences.USER_NAME, ""));
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Code Waarnemer");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(Preferences.getString(Preferences.USER_CODE, ""));
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Email Waarnemer");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(Preferences.getString(Preferences.USER_EMAIL, ""));
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Naam Buddy");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getBuddyName());
		rownr++;
		cell.setCellStyle(simple);
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Code Buddy");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getBuddyCode());
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Email Buddy");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getBuddyEmail());
		cell.setCellStyle(simple);
		rownr++;
		rownr++;

		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Datum en tijd te water");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getFormattedDate() + " "
				+ dive.getFormattedTime());
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Locatiecode");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getLocationCode());
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Locatienaam");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getLocationName());
		cell.setCellStyle(simple);
		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Zicht");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getVisibilityInMeters() + " meter");
		cell.setCellStyle(simple);
		rownr++;
		rownr++;

		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> profile = dive
				.getProfile();
		if (profile != null) {
			for (ProfilePartDbHelper.AddType type : profile.keySet()) {
				SerializableSparseArray<DiveProfilePart> array = profile
						.get(type);
				for (int i = 0; i < array.size(); i++) {
					DiveProfilePart ppart = array.get(array.keyAt(i));
					row = sheet.createRow(rownr);
					cell = row.createCell(COL_NAME);
					cell.setCellValue(ppart.profilePart
							.getShowName());
					cell.setCellStyle(bold);
					cell = row.createCell(COL_VALUE);
					cell.setCellValue(ppart.stayValueInMeters + " meter");
					rownr++;
					cell.setCellStyle(simple);
				}
			}
		}

		rownr++;
		row = sheet.createRow(rownr);
		cell = row.createCell(COL_NAME);
		cell.setCellValue("Opmerkingen");
		cell.setCellStyle(bold);
		cell = row.createCell(COL_VALUE);
		cell.setCellValue(dive.getRemarks());
		cell.setCellStyle(simple);

		sheet.addMergedRegion(new CellRangeAddress(0, rownr, 0, 0));

		rownr++;
		rownr++;
		return rownr;

	}

}
