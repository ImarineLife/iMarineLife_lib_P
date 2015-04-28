package nl.imarinelife.lib.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Messenger;
import android.util.Log;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;

public class ExpansionFileAccessHelper extends DownloaderService implements
		IDownloaderClient {

	public static final String TAG = "ExpansionFileAccessHlpr";
	private static ExpansionFileAccessHelper me = null;

	public static boolean delivered = false;
	
	/*
	 * private ProgressBar mPB;
	 * 
	 * private TextView mStatusText; private TextView mProgressFraction; private
	 * TextView mProgressPercent; private TextView mAverageSpeed; private
	 * TextView mTimeRemaining;
	 * 
	 * private View mDashboard; private View mCellMessage;
	 * 
	 * private Button mPauseButton; private Button mWiFiSettingsButton;
	 * 
	 * private boolean mStatePaused; private int mState;
	 * 
	 * private IDownloaderService mRemoteService;
	 */
	private IStub mDownloaderClientStub;

	private static int MAIN_VERSION = 0;
	private static int PATCH_VERSION = 0;
	private static final boolean IS_MAIN = true;

	/*
	 * private static int PATCHVERSION = 1; private static long PATCH_FILE_SIZE
	 * = 10l; private static final boolean IS_PATCH = false;
	 */
	private static final String PATH = "/Android/obb/nl.imarinelife.";

	static {
		MAIN_VERSION = LibApp.getExpansionFileMainVersion();
		PATCH_VERSION = LibApp.getExpansionFilePatchVersion();
	}

	public ExpansionFileAccessHelper() {

	}

	public static ExpansionFileAccessHelper getInstance() {
		if (me == null) {
			me = new ExpansionFileAccessHelper();
		}
		return me;
	}

	/**
	 * If the download isn't present, we initialize the download UI. This ties
	 * all of the controls into the remote service calls.
	 */
	public void initializeDownloadUI() {
		Log.d(TAG, "trying download");

		mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this,
				ExpansionFileAccessHelper.class);
		Log.d(TAG, "stub created");
		mDownloaderClientStub.disconnect(MainActivity.me); // just to be sure
		mDownloaderClientStub.connect(MainActivity.me); // in onStart of
														// MainActivity?

		// mDownloaderClientStub.disconnect(this); // in onStop of MainActivity?
		// you can do this (maybe in MainActivity) to show progress
		/*
		 * setContentView(R.layout.main);
		 * 
		 * mPB = (ProgressBar) findViewById(R.id.progressBar); mStatusText =
		 * (TextView) findViewById(R.id.statusText); mProgressFraction =
		 * (TextView) findViewById(R.id.progressAsFraction); mProgressPercent =
		 * (TextView) findViewById(R.id.progressAsPercentage); mAverageSpeed =
		 * (TextView) findViewById(R.id.progressAverageSpeed); mTimeRemaining =
		 * (TextView) findViewById(R.id.progressTimeRemaining); mDashboard =
		 * findViewById(R.id.downloaderDashboard); mCellMessage =
		 * findViewById(R.id.approveCellular); mPauseButton = (Button)
		 * findViewById(R.id.pauseButton); mWiFiSettingsButton = (Button)
		 * findViewById(R.id.wifiSettingsButton);
		 * 
		 * mPauseButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View view) { if (mStatePaused) {
		 * mRemoteService.requestContinueDownload(); } else {
		 * mRemoteService.requestPauseDownload(); }
		 * setButtonPausedState(!mStatePaused); } });
		 * 
		 * mWiFiSettingsButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { startActivity(new
		 * Intent(Settings.ACTION_WIFI_SETTINGS)); } });
		 * 
		 * Button resumeOnCell = (Button) findViewById(R.id.resumeOverCellular);
		 * resumeOnCell.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View view) {
		 * mRemoteService.setDownloadFlags
		 * (IDownloaderService.FLAGS_DOWNLOAD_OVER_CELLULAR);
		 * mRemoteService.requestContinueDownload();
		 * mCellMessage.setVisibility(View.GONE); } });
		 */

	}

	public void connect() {
		if (me != null && me.mDownloaderClientStub != null) {
			mDownloaderClientStub.connect(MainActivity.me);
		}
	}

	public void disconnect() {
		if (MainActivity.me != null && me != null
				&& me.mDownloaderClientStub != null) {
			mDownloaderClientStub.disconnect(MainActivity.me);
		}
	}

	/**
	 * This is a little helper class that demonstrates simple testing of an
	 * Expansion APK file delivered by Market. You may not wish to hard-code
	 * things such as file lengths into your executable... and you may wish to
	 * turn this code off during application development.
	 */
	private static class XAPKFile {
		public final boolean mIsMain;
		public final int mFileVersion;
		/*public final long mFileSize;*/
		public boolean delivered;
		boolean deliveryInitiatedThisSession;

		XAPKFile(boolean isMain, int fileVersion) {
			mIsMain = isMain;
			mFileVersion = fileVersion;
			/*mFileSize = fileSize;*/
			delivered = false;
			deliveryInitiatedThisSession = false;
		}
	}

	/**
	 * Here is where you place the data that the validator will use to determine
	 * if the file was delivered correctly. This is encoded in the source code
	 * so the application can easily determine whether the file has been
	 * properly delivered without having to talk to the server. If the
	 * application is using LVL for licensing, it may make sense to eliminate
	 * these checks and to just rely on the server.
	 */
	private static final XAPKFile[] xAPKS = {
			new XAPKFile(IS_MAIN, MAIN_VERSION),
			new XAPKFile(!IS_MAIN, PATCH_VERSION) };

	/**
	 * Go through each of the APK Expansion files defined in the structure above
	 * and determine if the files are present and match the required size. Free
	 * applications should definitely consider doing this, as this allows the
	 * application to be launched for the first time without having a network
	 * connection present. Paid applications that use LVL should probably do at
	 * least one LVL check that requires the network to be present, so this is
	 * not as necessary.
	 * 
	 * @return true if they are present.
	 */
	public static boolean expansionFilesDeliveredCorrectly() {
		if (delivered)
			return delivered;
		boolean deliveryOfAnyFileUncertain = false;
		for (XAPKFile xf : xAPKS) {
			if(xf.delivered) continue;
			
			String fileName = Helpers.getExpansionAPKFileName(MainActivity.me,
					xf.mIsMain, xf.mFileVersion);
			Log.d(TAG, "checking for " + fileName);
			File toCheck = new File(Helpers.generateSaveFileName(
					MainActivity.me, fileName));
			// if the file does not exist in exactly the right version, you
			// should try to get it. I am not checking for filesize, just for
			// existence of a file with the right name
			if (xf.mFileVersion > 0 && !toCheck.exists()) {
				Log.d(TAG, fileName + " does not exist");

				xf.delivered = false;
				deliveryOfAnyFileUncertain = true;
				if (!xf.deliveryInitiatedThisSession) {
					Log.d(TAG, fileName
							+ " delivery has not yet been initiated");
					xf.deliveryInitiatedThisSession = true;
					ExpansionFileAccessHelper me = ExpansionFileAccessHelper
							.getInstance();
					if (LibApp.getSalt() != null
							&& LibApp.getBase64PublicKey() != null) {
						me.initializeDownloadUI(); // initiate the download
					}
				}
			} else {
				xf.delivered = true;
				
			}
		}
		delivered = !deliveryOfAnyFileUncertain;
		return delivered;
	}

	public static boolean expansionFileAvailable() {
		String fileName = Helpers.getExpansionAPKFileName(MainActivity.me,
				IS_MAIN, (IS_MAIN ? MAIN_VERSION : PATCH_VERSION));
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();
		if (!isSDPresent)
			return false;

		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ PATH
				+ LibApp.getCurrentCatalogName().toLowerCase(), fileName);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static Bitmap getBitMap(String pathToFileInsideZip) {
		Bitmap bitmap = null;
		Log.d(TAG, "expansionFileAvailable[" + expansionFileAvailable() + "]");
		try {
			if (!expansionFileAvailable())
				return null;

			// Get a ZipResourceFile representing a merger of both the main and
			// patch files
			ZipResourceFile expansionFile = APKExpansionSupport
					.getAPKExpansionZipFile(MainActivity.me, MAIN_VERSION,
							PATCH_VERSION);

			// Get an input stream for a known file inside the expansion file
			// ZIPs
			InputStream fileStream = expansionFile
					.getInputStream(pathToFileInsideZip);
			if (fileStream == null) {
				Log.d(TAG, "pathToFileInsideZip not available["
						+ pathToFileInsideZip + "]");
				return null;
			}

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bitmap = BitmapFactory.decodeStream(fileStream);
			Log.d(TAG, "Bitmap decoded " + bitmap);
		} catch (IOException e) {
			Log.d(TAG, "getBitMap failed ", e);
		}
		return bitmap;

	}

	@Override
	public String getPublicKey() {
		return LibApp.getBase64PublicKey();
	}

	@Override
	public byte[] getSALT() {
		return LibApp.getSalt();
	}

	@Override
	public String getAlarmReceiverClassName() {
		return ExpansionFileDownloadBroadcastReceiver.class.getName();
	}

	@Override
	public void onServiceConnected(Messenger m) {
		Log.d(TAG, "onServiceConnected");
		IDownloaderService remoteService = DownloaderServiceMarshaller
				.CreateProxy(m);
		remoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
	}

	@Override
	public void onDownloadStateChanged(int newState) {
		Log.d(TAG, "onDownloadStateChanged");

	}

	@Override
	public void onDownloadProgress(DownloadProgressInfo progress) {
		Log.d(TAG, "oDownloadProgress " + progress.mOverallProgress + "/"
				+ progress.mOverallTotal);
	}

}
