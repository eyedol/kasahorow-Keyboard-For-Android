/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.anysoftkeyboard.buydictionary.BuyDictionaryFragment;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.EdgeEffectHacker;
import net.evendanan.pushingpixels.FragmentChauffeurActivity;

public class MainSettingsActivity extends FragmentChauffeurActivity {

    private static final String TAG = "ASK_MAIN";

	private DrawerLayout mDrawerRootLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private SharedPreferences.OnSharedPreferenceChangeListener menuExtraUpdaterOnConfigChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateMenuExtraData();
        }
    };

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_ui);

        mTitle = mDrawerTitle = getTitle();

        mDrawerRootLayout = (DrawerLayout) findViewById(R.id.main_root_layout);
        mDrawerRootLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerRootLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                ActivityCompat.invalidateOptionsMenu(MainSettingsActivity.this);// creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                ActivityCompat.invalidateOptionsMenu(MainSettingsActivity.this);// creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerRootLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AnyApplication.getConfig().addChangedListener(menuExtraUpdaterOnConfigChange);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
	    //applying my very own Edge-Effect color
	    EdgeEffectHacker.brandGlowEffect(getApplicationContext(), getResources().getColor(R.color.menu_divider));
    }

    @Override
    protected Fragment createRootFragmentInstance() {
        return new MainFragment();
    }

    @Override
    protected int getFragmentRootUiElementId() {
        return R.id.main_ui_content;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updating menu's data
        updateMenuExtraData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AnyApplication.getConfig().removeChangedListener(menuExtraUpdaterOnConfigChange);
    }

    private void updateMenuExtraData() {
        TextView keyboardsData = (TextView)findViewById(R.id.keyboards_group_extra_data);
        final int all = KeyboardFactory.getAllAvailableKeyboards(getApplicationContext()).size();
        final int enabled = KeyboardFactory.getEnabledKeyboards(getApplicationContext()).size();
        keyboardsData.setText(getString(R.string.keyboards_group_extra_template, enabled, all));

        TextView themeData = (TextView)findViewById(R.id.theme_extra_data);
        KeyboardTheme theme = KeyboardThemeFactory.getCurrentKeyboardTheme(getApplicationContext());
        if (theme == null)
            theme = KeyboardThemeFactory.getFallbackTheme(getApplicationContext());
        themeData.setText(getString(R.string.selected_add_on_summary, theme.getName()));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
	        return true;
        }

        return super.onOptionsItemSelected(item);
    }

	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    //side menu navigation methods

    public void onNavigateToRootClicked(View v) {
        mDrawerRootLayout.closeDrawers();
        returnToRootFragment();
    }

    public void onNavigateToKeyboardAddonSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new KeyboardAddOnSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToDictionarySettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new DictionariesFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToLanguageSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AdditionalLanguageSettingsFragment(), FragmentUiContext.RootFragment);

    }

    public void onNavigateToKeyboardThemeSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new KeyboardThemeSelectorFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToEffectsSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new EffectsSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToGestureSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new GesturesSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToUserInterfaceSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AdditionalUiSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToAboutClicked(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AboutAnySoftKeyboardFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToBuyDictionary(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new BuyDictionaryFragment(),FragmentUiContext.RootFragment);
    }


    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            getSupportActionBar().hide();
            mDrawerRootLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            getSupportActionBar().show();
            mDrawerRootLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void openDrawer() {
        mDrawerRootLayout.openDrawer(Gravity.LEFT);
    }
}
