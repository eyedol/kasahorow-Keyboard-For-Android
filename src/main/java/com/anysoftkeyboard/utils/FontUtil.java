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
