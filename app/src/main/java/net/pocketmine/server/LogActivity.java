package net.pocketmine.server;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.nao20010128nao.PM_Metroid.R;
import net.pocketmine.server.Utils.Utils;
import net.pocketmine.server.log.buttons.Ban;
import net.pocketmine.server.log.buttons.BanIp;
import net.pocketmine.server.log.buttons.Banlist;
import net.pocketmine.server.log.buttons.Clear;
import net.pocketmine.server.log.buttons.DefaultGamemode;
import net.pocketmine.server.log.buttons.Deop;
import net.pocketmine.server.log.buttons.Gamemode;
import net.pocketmine.server.log.buttons.Give;
import net.pocketmine.server.log.buttons.Kick;
import net.pocketmine.server.log.buttons.Kill;
import net.pocketmine.server.log.buttons.Me;
import net.pocketmine.server.log.buttons.Op;
import net.pocketmine.server.log.buttons.Pardon;
import net.pocketmine.server.log.buttons.PardonIp;
import net.pocketmine.server.log.buttons.Save_All;
import net.pocketmine.server.log.buttons.Save_Off;
import net.pocketmine.server.log.buttons.Save_On;
import net.pocketmine.server.log.buttons.Stop;
import net.pocketmine.server.log.buttons.Tell;
import net.pocketmine.server.log.buttons.Time_Set;
import net.pocketmine.server.log.buttons.Tp;
import net.pocketmine.server.log.buttons.Weather;
import net.pocketmine.server.log.buttons.Xp;

@SuppressWarnings("deprecation")
@android.annotation.TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LogActivity extends AppCompatActivity {

	public static LogActivity logActivity;
	public static ScrollView sv;
	public static SpannableStringBuilder currentLog = new SpannableStringBuilder();

	DrawerLayout drawer;
	boolean drawerOpened;
	LinearLayout commandButtons;
	boolean isRunning=ServerUtils.isRunning();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_main_new);
		logActivity = this;
		TextView logTV = (TextView) findViewById(R.id.logTextView);
		logTV.setText(currentLog);
		logTV.setTypeface(Typeface.MONOSPACE);

		sv = (ScrollView) findViewById(R.id.logScrollView);
		drawer=(DrawerLayout)findViewById(R.id.mainDrawer);
		commandButtons=(LinearLayout)findViewById(R.id.commandButtons);
		drawer.setDrawerListener(new DrawerLayout.DrawerListener(){
				public void onDrawerSlide(View v, float slide) {
					ViewCompat.setAlpha(commandButtons,slide);
				}
				public void onDrawerStateChanged(int state) {

				}
				public void onDrawerClosed(View v) {
					drawerOpened = false;
				}
				public void onDrawerOpened(View v) {
					drawerOpened = true;
				}
		});

		Button btnCmd = (Button) findViewById(R.id.runCommand);
		btnCmd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				EditText et = (EditText) findViewById(R.id.textCmd);
				performSend(et.getText().toString());
				et.setText("");
			}
		});

		updateRunningState(isRunning);
		applyHandlers();
	}

	final static int CLEAR_CODE = 143;
	final static int COPY_CODE = CLEAR_CODE + 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == CLEAR_CODE) {
			currentLog = new SpannableStringBuilder();
			TextView logTV = (TextView) logActivity
					.findViewById(R.id.logTextView);
			logTV.setText(currentLog);

			return true;
		} else if (item.getItemId() == COPY_CODE) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(currentLog);
			Toast.makeText(this,
					R.string.copied,
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuItem copy=menu.add(0, COPY_CODE, 0, android.R.string.copy).setIcon(R.drawable.content_copy);
		MenuItemCompat.setShowAsAction(copy,MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem clear=menu.add(0, CLEAR_CODE, 0, R.string.clear).setIcon(R.drawable.content_discard);
		MenuItemCompat.setShowAsAction(clear,MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public void onBackPressed() {
		// TODO: Implement this method
		if(drawerOpened){
			drawer.closeDrawers();
			drawerOpened=false;
		}else{
			finish();
		}
	}

	public static void log(final String whatToLog) {
		StringBuilder formatted = new StringBuilder();

		int currentColor = -1;
		Boolean bold = false;

		System.out.println(whatToLog);
		String[] parts = whatToLog.split("\u001B\\[");
		Boolean firstPart = true;
		for (String part : parts) {
			if (!firstPart) {

				int end = part.indexOf("m");
				if (end != -1) {
					String[] flags = part.substring(0, end).split(";");
					for (String flag : flags) {
						if (flag.startsWith(";"))
							flag = flag.substring(1);
						try {
							int n = Integer.parseInt(flag);
							if (n >= 30 && n < 40) {
								// text colour
								if (currentColor != -1) {
									formatted.append("</font>");
									n = -1;
								}
								// get color
								if (!bold) {
									// standard

									if (n == 30)
										currentColor = Color.argb(255, 0, 0, 0);
									else if (n == 31)
										currentColor = Color.argb(255, 128, 0,
												0);
									else if (n == 32)
										currentColor = Color.argb(255, 0, 128,
												0);
									else if (n == 33)
										currentColor = Color.argb(255, 128,
												128, 0);
									else if (n == 34)
										currentColor = Color.argb(255, 0, 0,
												128);
									else if (n == 35)
										currentColor = Color.argb(255, 128, 0,
												128);
									else if (n == 36)
										currentColor = Color.argb(255, 0, 128,
												128);
									else if (n == 37)
										currentColor = Color.argb(255, 128,
												128, 128);
								} else {
									// lighter

									if (n == 30)
										currentColor = Color.argb(255, 85, 85,
												85);
									else if (n == 31)
										currentColor = Color.argb(255, 255, 0,
												0);
									else if (n == 32)
										currentColor = Color.argb(255, 0, 255,
												0);
									else if (n == 33)
										currentColor = Color.argb(255, 255,
												255, 0);
									else if (n == 34)
										currentColor = Color.argb(255, 0, 0,
												255);
									else if (n == 35)
										currentColor = Color.argb(255, 255, 0,
												255);
									else if (n == 36)
										currentColor = Color.argb(255, 0, 255,
												255);
									else if (n == 37)
										currentColor = Color.argb(255, 255,
												255, 255);
								}

								if (currentColor != -1) {
									formatted.append("<font color=\""
											+ (String.format("#%06X",
													(0xFFFFFF & currentColor)))
											+ "\">");
								}
							} else if (n == 0) {
								if (currentColor != -1) {
									formatted.append("</font>");
									n = -1;
								}
								if (bold) {
									bold = false;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					part = part.substring(end + 1);
				}
				formatted.append(part);
			} else {
				formatted.append(part);
				firstPart = false;
			}
		}
		formatted.append("<br/>");
		final Spanned result = Html.fromHtml(formatted.toString());
		currentLog.append(result);

		if (logActivity != null) {
			logActivity.runOnUiThread(new Runnable() {
				public void run() {
					TextView logTV = (TextView) logActivity
							.findViewById(R.id.logTextView);
					logTV.append(result);
					sv.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}

	public void performSend(String s){
		log(">" + s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
		ServerUtils.executeCMD(s);
	}
	
	public static void updateRunningState(final boolean newState){
		if(logActivity==null)return;
		logActivity.runOnUiThread(new Runnable(){
			public void run(){
				logActivity.isRunning=newState;
				for(View v:Utils.getChildren((ViewGroup)logActivity.findViewById(R.id.commandButtons)))
					v.setEnabled(newState);
			}
		});
	}
	
	private void applyHandlers() {
		new Stop(this);
		new Op(this);
		new Deop(this);
		new Kick(this);
		new Ban(this);
		new BanIp(this);
		new Pardon(this);
		new PardonIp(this);
		new Time_Set(this);
		new Gamemode(this);
		new Save_All(this);
		new Save_On(this);
		new Save_Off(this);
		new Give(this);
		new Clear(this);
		new Kill(this);
		new Tell(this);
		new Tp(this);
		new Xp(this);
		new DefaultGamemode(this);
		new Weather(this);
		new Me(this);
		new Banlist(this);
	}
}
