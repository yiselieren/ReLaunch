package com.harasoft.relaunch;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ebook.EBook;
import ebook.parser.InstantParser;
import ebook.parser.Parser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class BookInfo extends Activity {
	final String TAG = "BookInfo";
	final int COVER_MAX_W = 300;
	final int COVER_MAX_H = 450;

	ReLaunchApp app;
	SharedPreferences prefs;
	String fileName;
	EBook eBook;
	Bitmap eCover;

	private void setEinkController() {
		if (prefs != null) {
			Integer einkUpdateMode = 1;
			try {
				einkUpdateMode = Integer.parseInt(prefs.getString(
						"einkUpdateMode", "1"));
			} catch (Exception e) {
				einkUpdateMode = 1;
			}
			if (einkUpdateMode < -1 || einkUpdateMode > 2)
				einkUpdateMode = 1;
			if (einkUpdateMode >= 0) {
				EinkScreen.UpdateMode = einkUpdateMode;

				Integer einkUpdateInterval = 10;
				try {
					einkUpdateInterval = Integer.parseInt(prefs.getString(
							"einkUpdateInterval", "10"));
				} catch (Exception e) {
					einkUpdateInterval = 10;
				}
				if (einkUpdateInterval < 0 || einkUpdateInterval > 100)
					einkUpdateInterval = 10;
				EinkScreen.UpdateModeInterval = einkUpdateInterval;

				EinkScreen.PrepareController(null, false);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setEinkController();

		final Intent data = getIntent();
		if (data.getExtras() == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		
		app = ((ReLaunchApp) getApplicationContext());
		app.setFullScreenIfNecessary(this);
		setContentView(R.layout.bookinfo);
		fileName = data.getExtras().getString("file");
		eBook = getEBookInfoByFileName(fileName);
		ImageView imgCover = (ImageView) findViewById(R.id.cover);
		TextView tvAnnotation = (TextView) findViewById(R.id.tvAnnotation);
		TextView tvSeries = (TextView) findViewById(R.id.tvSeries);
		ListView lvAuthors = (ListView) findViewById(R.id.authors);
		lvAuthors.setDivider(null);
		if (eBook.isOk) {
			if (eCover != null)
				imgCover.setImageBitmap(eCover);
			else
				imgCover.setVisibility(View.GONE);
			if (eBook.annotation != null) {
				eBook.annotation = eBook.annotation.trim()
					.replace("<p>", "")
					.replace("</p>", "\n");
				tvAnnotation.setText(eBook.annotation);
			} else
				tvAnnotation.setVisibility(View.GONE);
			if (eBook.authors.size() > 0) {
				final String[] authors = new String[eBook.authors.size()];
				for (int i = 0; i < eBook.authors.size(); i++) {
					String author = "";
					if (eBook.authors.get(i).firstName != null)
						if (eBook.authors.get(i).firstName.length() > 0)
							author += eBook.authors.get(i).firstName.substring(0,1) + ".";
					if (eBook.authors.get(i).middleName != null)
						if (eBook.authors.get(i).middleName.length() > 0)
							author += eBook.authors.get(i).middleName.substring(0,1) + ".";
					if (eBook.authors.get(i).lastName != null)
						author += " " + eBook.authors.get(i).lastName;
					authors[i] = author;
				}
				final ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, authors);
				lvAuthors.setAdapter(lvAdapter);
			}
			if (eBook.sequenceName != null) {
				tvSeries.setText(eBook.sequenceName);
			}
			
			((TextView) findViewById(R.id.book_title)).setText(eBook.title);
		}
		
		

		
		((ImageButton) findViewById(R.id.btnExit))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		
		
//		ScreenOrientation.set(this, prefs);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		setEinkController();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setEinkController();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setEinkController();
		app.generalOnResume(TAG, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		return;
	}
	
	public EBook getEBookInfoByFileName(String fileName) {
		Parser parser = new InstantParser();
		EBook eBook = parser.parse(fileName, true);
		if (eBook.cover != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(eBook.cover, 0,
					eBook.cover.length);
			if (bitmap != null) {
				int width = Math.min(COVER_MAX_W, bitmap.getWidth());
				int height = (int) (width * bitmap.getHeight())/bitmap.getWidth();
				eCover = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}
		}
		return eBook;
	}


}
