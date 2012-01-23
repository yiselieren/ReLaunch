package com.harasoft.relaunch;

import java.io.DataOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.widget.Toast;

public class PowerFunctions {

	public static boolean actionLock(Activity act) {
		File isrooted = new File("/system/bin", "su");
		if (isrooted.exists()) {
			try {
				Process p = Runtime.getRuntime().exec(
						act.getResources().getString(R.string.shell));
				try {
					// nook only
					DataOutputStream os = new DataOutputStream(
							p.getOutputStream());
					os.writeChars("su\n");
					SystemClock.sleep(100);
					os.writeChars("sendevent /dev/input/event1 1 116 1\n");
					SystemClock.sleep(100);
					os.writeChars("sendevent /dev/input/event1 1 116 0\n");
				} catch (Exception e) {
				} finally {
					p.destroy();
				}
			} catch (Exception e) {
			}
			return true;
		} else {
			Toast.makeText(
					act,
					act.getResources()
							.getString(R.string.jv_advanced_root_only),
					Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public static void actionReboot(Activity act) {
		File isrooted = new File("/system/bin", "su");
		if (isrooted.exists()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			// builder.setTitle("Reboot confirmation");
			builder.setTitle(act.getResources().getString(
					R.string.jv_advanced_reboot_confirm_title));
			// builder.setMessage("Are you sure to reboot your device ? ");
			builder.setMessage(act.getResources().getString(
					R.string.jv_advanced_reboot_confirm_text));
			// builder.setPositiveButton("YES", new
			// DialogInterface.OnClickListener() {
			final Activity fact = act;
			builder.setPositiveButton(
					act.getResources().getString(R.string.jv_advanced_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							fact.setContentView(R.layout.reboot);
							Timer timer = new Timer();
							timer.schedule(new TimerTask() {
								public void run() {
									try {
										Process p = Runtime.getRuntime().exec(
												fact.getResources().getString(
														R.string.shell));
										try {
											DataOutputStream os = new DataOutputStream(
													p.getOutputStream());
											os.writeChars("su\n");
											SystemClock.sleep(100);
											os.writeChars("reboot\n");
										} catch (Exception e) {
										} finally {
											p.destroy();
										}
									} catch (Exception e) {
									}
								}
							}, 500);
						}
					});
			// builder.setNegativeButton("NO", new
			// DialogInterface.OnClickListener() {
			builder.setNegativeButton(
					act.getResources().getString(R.string.jv_advanced_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			builder.show();
		} else {
			Toast.makeText(
					act,
					act.getResources()
							.getString(R.string.jv_advanced_root_only),
					Toast.LENGTH_LONG).show();
		}
	}

	public static void actionPowerOff(Activity act) {
		File isrooted = new File("/system/bin", "su");
		if (isrooted.exists()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			// builder.setTitle("Reboot confirmation");
			builder.setTitle(act.getResources().getString(
					R.string.jv_advanced_poweroff_confirm_title));
			// builder.setMessage("Are you sure to reboot your device ? ");
			builder.setMessage(act.getResources().getString(
					R.string.jv_advanced_poweroff_confirm_text));
			// builder.setPositiveButton("YES", new
			// DialogInterface.OnClickListener() {
			final Activity fact = act;
			builder.setPositiveButton(
					act.getResources().getString(R.string.jv_advanced_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							fact.setContentView(R.layout.poweroff);
							Timer timer = new Timer();
							timer.schedule(new TimerTask() {
								public void run() {
									try {
										Process p = Runtime.getRuntime().exec(
												fact.getResources().getString(
														R.string.shell));
										try {
											DataOutputStream os = new DataOutputStream(
													p.getOutputStream());
											os.writeChars("su\n");
											SystemClock.sleep(100);
											os.writeChars("reboot -p\n");
										} catch (Exception e) {
										} finally {
											p.destroy();
										}
									} catch (Exception e) {
									}
								}
							}, 500);
						}
					});
			// builder.setNegativeButton("NO", new
			// DialogInterface.OnClickListener() {
			builder.setNegativeButton(
					act.getResources().getString(R.string.jv_advanced_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			builder.show();
		} else {
			Toast.makeText(
					act,
					act.getResources()
							.getString(R.string.jv_advanced_root_only),
					Toast.LENGTH_LONG).show();
		}
	}

}
