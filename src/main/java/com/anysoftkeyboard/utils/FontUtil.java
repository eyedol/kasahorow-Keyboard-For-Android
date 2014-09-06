/*****************************************************************************
 ** Copyright (c) 2010 - 2012 Ushahidi Inc
 ** All rights reserved
 ** Contact: team@ushahidi.com
 ** Website: http://www.ushahidi.com
 **
 ** GNU Lesser General Public License Usage
 ** This file may be used under the terms of the GNU Lesser
 ** General Public License version 3 as published by the Free Software
 ** Foundation and appearing in the file LICENSE.LGPL included in the
 ** packaging of this file. Please review the following information to
 ** ensure the GNU Lesser General Public License version 3 requirements
 ** will be met: http://www.gnu.org/licenses/lgpl.html.
 **
 **
 ** If you have questions regarding the use of this file, please contact
 ** Ushahidi developers at team@ushahidi.com.
 **
 *****************************************************************************/

package com.anysoftkeyboard.utils;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @author eyedol
 */
public class FontUtil {

    private static final Hashtable<String, SoftReference<Typeface>> fontCache = new Hashtable<String, SoftReference<Typeface>>();

    public static Typeface getFont(Context c, String name) {
        try {
            synchronized (fontCache) {
                if (fontCache.get(name) != null) {
                    SoftReference<Typeface> ref = fontCache.get(name);
                    if (ref.get() != null) {
                        return ref.get();
                    }
                }

                Typeface typeface = Typeface.createFromAsset(
                        c.getAssets(),"fonts/" + name);
                fontCache.put(name, new SoftReference<Typeface>(typeface));

                return typeface;

            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.d("FONTUTIL", "Could not find: " + name, e);
        }
        return null;
    }
}
