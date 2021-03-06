/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package net.pocketmine.server;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nao20010128nao.PM_Metroid.R;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.pocketmine.forum.PluginsActivity;
import org.apache.http.conn.util.InetAddressUtils;

/**
 * Activity to Home Screen
 */

@android.annotation.TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class HomeActivity extends AppCompatActivity {

	final static int PROJECT_CODE = 143;
	final static int VERSION_MANAGER_CODE = 144;
	final static int FILE_MANAGER_CODE = 145;
	final static int PROPERTIES_EDITOR_CODE = 146;
	final static int PLUGINS_CODE = 147;
	final static int FORCE_CLOSE_CODE = 148;
	final static int ABOUT_US_CODE = 149;
	final static int CONSOLE_CODE = 150;
	final static int REINSTALL_PHP_CODE = 151;
	final static int CHOOSE_INSTALLATION_DIRECTORY=152;
	public static HashMap<String, String> server;
	public static SharedPreferences prefs;

	private final Context mContext = HomeActivity.this;
	public static HomeActivity ha = null;

	public static Boolean statsShown = false;
	public static String online = "Unknown";
	public static String ram = "Unknown";
	public static String download = "Unknown";
	public static String upload = "Unknown";
	public static String tps = "Unknown";
	public static String[] players = null,banlist,bannedIps;

	/**
	 * Buttons for managing server state
	 */
	public static Button btn_runServer;
	public static Button btn_stopServer;
	public static Intent servInt;
	public static Boolean isStarted = false;

	public static LayoutInflater inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
		} catch (Exception e) {
			return;
		}

		inflater = getLayoutInflater();

		ha = this;
		setContentView(R.layout.home);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		ServerUtils.setContext(mContext);

		btn_runServer = (Button) findViewById(R.id.RunTime_Http);
		btn_stopServer = (Button) findViewById(R.id.RunTime_Http_Kill);

		btn_runServer.setEnabled(!isStarted);
		btn_runServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btn_runServer.setEnabled(false);
				btn_stopServer.setEnabled(true);
				btn_runServer.setText(R.string.server_online);
				String msg = getResources().getString(R.string.start_unable);
				if (ServerUtils.isRunning()) {
					msg = getResources().getString(R.string.start_success);
				}
				android.widget.Toast.makeText(mContext, msg,
						android.widget.Toast.LENGTH_LONG).show();

				servInt = new Intent(mContext, ServerService.class);
				startService(servInt);
				isStarted = true;
				showStats(true);

				ServerUtils.runServer();
			}
		});

		btn_stopServer.setEnabled(isStarted);
		btn_stopServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (ServerUtils.isRunning()) {
					LogActivity.log(getResources().getString(R.string.log_stopping));
					ServerUtils.executeCMD("stop");
				}

			}
		});

		Button btn_op = (Button) findViewById(R.id.action_op);
		btn_op.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				actionPlayer("op");
			}
		});
		Button btn_deop = (Button) findViewById(R.id.action_deop);
		btn_deop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				actionPlayer("deop");
			}
		});

		if (isStarted) {
			showStats(false);
		}
	}

	// http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase(Locale.US);
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%');
								return delim < 0 ? sAddr : sAddr.substring(0,
										delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		}
		return ha.getResources().getString(R.string.unknown);
	}

	private void actionPlayer(final String cmd) {
		if (players == null)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
		builder.setTitle(getResources().getString(R.string.op_player));
		final CharSequence[] list = new CharSequence[players.length + 1];
		list[0] = getResources().getString(android.R.string.cancel);
		for (int i = 0; i < players.length; i++) {
			list[i + 1] = players[i];
		}
		builder.setItems(list, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface di, int pos) {
				if (pos == 0) {
					// nothing
				} else {
					ServerUtils.executeCMD(cmd + " " + list[pos]);
				}
			}
		});
		builder.show();
	}

	public static void showStats(Boolean reset) {
		if (reset) {
			online = ha.getResources().getString(R.string.unknown);
			ram = ha.getResources().getString(R.string.unknown);
			download = ha.getResources().getString(R.string.unknown);
			upload = ha.getResources().getString(R.string.unknown);
			tps = ha.getResources().getString(R.string.unknown);
			players = null;
		}
		if (ha != null) {
			ha.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LinearLayout layout = (LinearLayout) ha
							.findViewById(R.id.stats);
					layout.setVisibility(View.VISIBLE);
					statsShown = true;
					TextView ip = (TextView) ha.findViewById(R.id.stat_ip);
					ip.setText(ha.getResources().getString(R.string.ip_address) + ": " + getIPAddress(true));
					setStats(online, ram, download, upload, tps);
					updatePlayerList(players);
				}
			});
		}
	}

	public static void setStats(final String nOnline, final String nRAM,
			final String nUpload, final String nDownload, final String nTPS) {

		online = nOnline;
		ram = nRAM;
		upload = nUpload;
		download = nDownload;
		tps = nTPS;

		if (ha != null) {
			ha.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!statsShown) {
						showStats(true);
					}

					TextView online = (TextView) ha
							.findViewById(R.id.stat_online);
					online.setText(ha.getResources().getString(R.string.online)+": " + nOnline);
					TextView ram = (TextView) ha.findViewById(R.id.stat_ram);
					ram.setText(ha.getResources().getString(R.string.ram_usage)+": " + nRAM);
					TextView download = (TextView) ha
							.findViewById(R.id.stat_download);
					download.setText(ha.getResources().getString(R.string.download)+": " + nDownload + " kB/s");
					TextView upload = (TextView) ha
							.findViewById(R.id.stat_upload);
					upload.setText(ha.getResources().getString(R.string.upload)+": " + nUpload + " kB/s");
					TextView tps = (TextView) ha.findViewById(R.id.stat_tps);
					tps.setText(ha.getResources().getString(R.string.tps)+": " + nTPS);
				}
			});
		}
	}

	public static void hideStats() {
		if (ha != null) {
			ha.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LinearLayout layout = (LinearLayout) ha
							.findViewById(R.id.stats);
					layout.setVisibility(View.GONE);
					statsShown = false;
				}
			});
		}
	}

	public static int dip2px(float dips) {
		return (int) (dips * ha.getResources().getDisplayMetrics().density + 0.5f);
	}

	public static void updatePlayerList(final String[] nPlayers) {
		players = nPlayers;

		if (ha != null && inflater != null) {
			ha.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					LinearLayout layout = (LinearLayout) ha
							.findViewById(R.id.players);
					layout.removeAllViews();

					if (players != null && players.length > 0) {
						for (final String player : players) {
							View v = inflater.inflate(R.layout.player, layout,
									false);
							TextView playerName = (TextView) v
									.findViewById(R.id.player_name);
							playerName.setText(player);
							final Button kickBtn = (Button) v
									.findViewById(R.id.player_kick);
							kickBtn.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									LinearLayout ll = new LinearLayout(kickBtn
											.getContext());
									final EditText input = new EditText(kickBtn
											.getContext());
									LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
											LinearLayout.LayoutParams.MATCH_PARENT,
											LinearLayout.LayoutParams.WRAP_CONTENT);
									layoutParams.setMargins(dip2px(8), 0,
											dip2px(8), 0);
									input.setLayoutParams(layoutParams);
									ll.addView(input);
									new AlertDialog.Builder(kickBtn
											.getContext())
											.setTitle(R.string.player_kick)
											.setMessage(R.string.kick_reason)
											.setView(ll)
											.setPositiveButton(
													R.string.player_kick,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int whichButton) {
															ServerUtils
																	.executeCMD("kick "
																			+ player
																			+ " "
																			+ input.getText()
																					.toString());
														}
													})
											.setNegativeButton(android.R.string.cancel, null)
											.show();
								}
							});
							final Button banBtn = (Button) v
									.findViewById(R.id.player_ban);
							banBtn.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									AlertDialog.Builder builder = new AlertDialog.Builder(
											banBtn.getContext());
									builder.setTitle(R.string.ban_player_title);
									builder.setItems(
											ha.getResources().getStringArray(R.array.ban_player_modes),
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													if (which == 0) {
														ServerUtils
																.executeCMD("ban "
																		+ player);
													} else if (which == 1) {
														ServerUtils
																.executeCMD("ban-ip "
																		+ player);
													}
												}
											});
									builder.show();
								}
							});
							layout.addView(v);
						}
					}
				}
			});
		}
	}

	public static void updateBanList(String... list){
		banlist=list;
	}
	
	public static void updateBanIpsList(String... list){
		bannedIps=list;
	}
	
	public static void stopNotifyService() {

		if (ha != null && servInt != null) {
			ha.runOnUiThread(new Runnable() {
				public void run() {
					isStarted = false;
					ha.stopService(servInt);
					btn_runServer.setEnabled(true);
					btn_stopServer.setEnabled(false);
				}
			});
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!ServerUtils.checkPhpInstalled()) {
			startActivity(new Intent(mContext, PhpVersionSelectorActivity.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem log=menu.add(0, CONSOLE_CODE, 0, R.string.title_activity_log).setIcon(R.drawable.hardware_dock);
		MenuItemCompat.setShowAsAction(log,MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu sub = menu.addSubMenu(getString(R.string.abs_settings));
		/**
		 * Set Icon for Submenu
		 */
		sub.setIcon(R.drawable.action_settings);
		/**
		 * Build navigation for submenu
		 */
		sub.add(0, VERSION_MANAGER_CODE, 0,
				getString(R.string.abs_version_manager));
		sub.add(0, PROPERTIES_EDITOR_CODE, 0,
				getString(R.string.abs_properties_editor));
		sub.add(0, PLUGINS_CODE, 0, getString(R.string.abs_plugins));
		sub.add(0, FORCE_CLOSE_CODE, 0, getString(R.string.abs_force_close));
		sub.add(0, ABOUT_US_CODE, 0, getString(R.string.abs_about));
		sub.add(0, REINSTALL_PHP_CODE, 0, getString(R.string.php_reinstall));
		MenuItem subgrp=sub.getItem();
		MenuItemCompat.setShowAsAction(subgrp,MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
			return false;
		}
		if (item.getItemId() == FILE_MANAGER_CODE) {
			startActivity(new Intent(mContext, FileManagerActivity.class));
		} else if (item.getItemId() == VERSION_MANAGER_CODE) {
			startActivity(new Intent(mContext, VersionManagerActivity.class));
		} else if (item.getItemId() == PROPERTIES_EDITOR_CODE) {
			startActivity(new Intent(mContext, ConfigActivity.class));
		} else if (item.getItemId() == PLUGINS_CODE) {
			startActivity(new Intent(mContext, PluginsActivity.class));
		} else if (item.getItemId() == FORCE_CLOSE_CODE) {
			btn_runServer.setEnabled(true);
			btn_stopServer.setEnabled(false);
			ServerUtils.stopServer();
			if (servInt != null)
				stopService(servInt);
			isStarted = false;
		} else if (item.getItemId() == ABOUT_US_CODE) {
			startActivity(new Intent(mContext, About.class));
		} else if (item.getItemId() == CONSOLE_CODE) {
			startActivity(new Intent(mContext, LogActivity.class));
		} else if (item.getItemId() == REINSTALL_PHP_CODE) {
			startActivity(new Intent(mContext, PhpVersionSelectorActivity.class).putExtra("reinst",true));
		}
		return true;
	}
}
