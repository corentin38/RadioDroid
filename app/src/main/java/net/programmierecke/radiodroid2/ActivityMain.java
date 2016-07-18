package net.programmierecke.radiodroid2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.programmierecke.radiodroid2.ActivityBase;
import net.programmierecke.radiodroid2.interfaces.IFragmentRefreshable;
import net.programmierecke.radiodroid2.interfaces.IFragmentSearchable;
import net.programmierecke.radiodroid2.interfaces.IMainView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ActivityMain extends ActivityBase implements SearchView.OnQueryTextListener, IMainView {

	private static final String TAG = "RD_MAIN";

	private DrawerLayout        mDrawerLayout;
	private NavigationView      mNavigationView;
	private SearchView          mSearchView;
	private Toolbar             mToolbar;
	private MenuItem            menuItemSearch;
	private MenuItem            menuItemRefresh;

	private IFragmentRefreshable fragRefreshable = null;
	private IFragmentSearchable  fragSearchable  = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		mToolbar = setUpToolbar(true);

		clearFilesDirectory();

		PlayerServiceUtil.bind(this);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		mNavigationView = (NavigationView) findViewById(R.id.my_navigation_view) ;

		initNavigationView();

		//myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_toolbar);
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, R.string.app_name,R.string.app_name);
		mDrawerLayout.addDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
	}

	private void initNavigationView() {
		mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				mDrawerLayout.closeDrawers();

				int itemId = menuItem.getItemId();
				selectFragment(itemId);

				return false;
			}
		});
	}

	private void selectFragment(int itemId) {
		Fragment f = null;

		switch (itemId) {

		case R.id.nav_item_player_status:
			Intent intent = new Intent(ActivityMain.this, ActivityPlayerInfo.class);
			startActivity(intent);
			return;

		case R.id.nav_item_stations:
			f = new FragmentTabs();
			break;

		case R.id.nav_item_starred:
			f = new FragmentStarred();
			break;

		case R.id.nav_item_history:
			f = new FragmentHistory();
			break;

		case R.id.nav_item_serverinfo:
			f = new FragmentServerInfo();
			break;

		case R.id.nav_item_recordings:
			f = new FragmentRecordings();
			break;

		case R.id.nav_item_alarm:
			f = new FragmentAlarm();
			menuItemSearch.setVisible(false);
			mToolbar.setTitle(R.string.nav_item_alarm);
			break;

		case R.id.nav_item_settings:
			f = new FragmentSettings();
			menuItemSearch.setVisible(false);
			mToolbar.setTitle(R.string.nav_item_settings);
			break;

		case R.id.nav_item_about:
			f = new FragmentAbout();
			menuItemSearch.setVisible(false);
			mToolbar.setTitle(R.string.nav_item_about);
			break;

		default:
			break;
		}

		FragmentTransaction xfragmentTransaction = getSupportFragmentManager().beginTransaction();
		xfragmentTransaction.replace(R.id.containerView, f).commit();
		fragRefreshable = null;
		fragSearchable = null;
		if (f instanceof IFragmentRefreshable) {
			fragRefreshable = (IFragmentRefreshable) f;
		}
		if (f instanceof IFragmentSearchable) {
			fragSearchable = (IFragmentSearchable) f;
		}
		menuItemRefresh.setVisible(fragRefreshable != null);

	}

	public void setRefreshableFragment(IFragmentRefreshable refreshable) {
		menuItemRefresh.setVisible(true);
		fragRefreshable = refreshable;
	}

	public void setSearchableFragment(IFragmentSearchable searchable) {
		menuItemSearch.setVisible(true);
		fragSearchable = searchable;
	}

	public void setToolbarTitle(int titleId) {
		mToolbar.setTitle(titleId);
	}

	private void clearFilesDirectory() {
		for (File file : getFilesDir().listFiles()) {
			Log.d(TAG, "Removing file:" + file.getName());
			file.delete();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		Log.d(TAG, "onRequestPermissionsResult - requestCode: " + requestCode);
		switch (requestCode) {
		case Utils.REQUEST_EXTERNAL_STORAGE:
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "Refreshing current fragment if refreshable");
				if (fragRefreshable != null){
					fragRefreshable.Refresh();
				}
			} else {
				Toast.makeText(this, getString(R.string.error_record_needs_write), Toast.LENGTH_LONG).show();
			}
			return;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PlayerServiceUtil.unBind(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);

		menuItemSearch = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) MenuItemCompat.getActionView(menuItemSearch);
		mSearchView.setOnQueryTextListener(this);

		menuItemRefresh = menu.findItem(R.id.action_refresh);

		if (fragSearchable == null) {
			menuItemSearch.setVisible(false);
		}

		if (fragRefreshable == null) {
			menuItemRefresh.setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
			case R.id.action_refresh:
				if (fragRefreshable != null){
					fragRefreshable.Refresh();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		Fragment first = null;
		if (sharedPref.getBoolean("starred_at_startup", false)) {
			FragmentStarred fragStarred = new FragmentStarred();
			getSupportActionBar().setTitle(R.string.nav_item_starred);
			fragSearchable = null;
			fragRefreshable = null;
			first = fragStarred;
		} else {
			FragmentTabs fragTabs = new FragmentTabs();
			getSupportActionBar().setTitle(R.string.nav_item_stations);
			fragRefreshable = fragTabs;
			fragSearchable = fragTabs;
			first = fragTabs;
		}

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.containerView, first).commit();
	}

	public void Search(String query){
		if (fragSearchable != null) {
			fragSearchable.Search(query);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		String queryEncoded = null;
		try {
			mSearchView.setQuery("", false);
			mSearchView.clearFocus();
			mSearchView.setIconified(true);
			queryEncoded = URLEncoder.encode(query, "utf-8");
			queryEncoded = queryEncoded.replace("+","%20");
			Search(getString(R.string.radiobrowser_url_stations_byname) + queryEncoded);
		} catch (UnsupportedEncodingException e) {
			Log.d(TAG, "Unable to submit request: <" + query + "> because of following error: " + e.getMessage());
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}
}
