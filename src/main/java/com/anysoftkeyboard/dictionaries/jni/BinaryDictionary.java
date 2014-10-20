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

package com.anysoftkeyboard.dictionaries.jni;

import android.content.res.AssetFileDescriptor;
import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.utils.Log;

import java.io.FileDescriptor;
import java.util.Arrays;

/**
 * Implements a static, compacted, binary dictionary of standard words.
 */
public class BinaryDictionary extends Dictionary {
    public static final int MAX_WORD_LENGTH = 20;
    private static final String TAG = "ASK_BinaryDictionary";
    private static final int MAX_ALTERNATIVES = 16;
    private static final int MAX_WORDS = 16;
    private static final boolean ENABLE_MISSED_CHARACTERS = true;
    private final AssetFileDescriptor mAfd;
    private volatile int mNativeDict;
    private int[] mInputCodes = new int[MAX_WORD_LENGTH * MAX_ALTERNATIVES];
    private char[] mOutputChars = new char[MAX_WORD_LENGTH * MAX_WORDS];
    private int[] mFrequencies = new int[MAX_WORDS];

    static {
        try {
            System.loadLibrary("anysoftkey_jni");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "******** Could not load native library nativeim ********");
            Log.e(TAG, "******** Could not load native library nativeim ********", ule);
            Log.e(TAG, "******** Could not load native library nativeim ********");
        } catch (Throwable t) {
            Log.e(TAG, "******** Failed to load native dictionary library ********");
            Log.e(TAG, "******** Failed to load native dictionary library *******", t);
            Log.e(TAG, "******** Failed to load native dictionary library ********");
        }
    }

    public BinaryDictionary(String dictionaryName, AssetFileDescriptor afd) {
        super(dictionaryName);
        mAfd = afd;
    }

    @Override
    protected final void loadAllResources() {
        //The try-catch is for issue 878: http://code.google.com/p/softkeyboard/issues/detail?id=878
        try {
            mNativeDict = 0;
            long startTime = System.currentTimeMillis();
            mNativeDict = openNative(mAfd.getFileDescriptor(), mAfd.getStartOffset(), mAfd.getLength(), TYPED_LETTER_MULTIPLIER, FULL_WORD_FREQ_MULTIPLIER);
            Log.d(TAG, "Loaded dictionary in " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (UnsatisfiedLinkError ex) {
            Log.w(TAG, "Failed to load binary JNI connection! Error: " + ex.getMessage());
        }
    }

    private native int openNative(FileDescriptor fd, long offset, long length, int typedLetterMultiplier, int fullWordMultiplier);

    private native void closeNative(int dict);

    private native boolean isValidWordNative(int nativeData, char[] word, int wordLength);

    private native int getSuggestionsNative(int dict, int[] inputCodes, int codesSize, char[] outputChars, int[] frequencies, int maxWordLength, int maxWords, int maxAlternatives, int skipPos);

    @Override
    public void getWords(final WordComposer codes, final WordCallback callback) {
        if (mNativeDict == 0 || isClosed()) return;
        final int codesSize = codes.length();
        // Wont deal with really long words.
        if (codesSize > MAX_WORD_LENGTH - 1) return;

        Arrays.fill(mInputCodes, -1);
        for (int i = 0; i < codesSize; i++) {
            int[] alternatives = codes.getCodesAt(i);
            System.arraycopy(alternatives, 0, mInputCodes, i * MAX_ALTERNATIVES, Math.min(alternatives.length, MAX_ALTERNATIVES));
        }
        Arrays.fill(mOutputChars, (char) 0);
        Arrays.fill(mFrequencies, 0);

        int count = getSuggestionsNative(mNativeDict, mInputCodes, codesSize, mOutputChars, mFrequencies, MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES, -1);

        // If there aren't sufficient suggestions, search for words by allowing wild cards at
        // the different character positions. This feature is not ready for prime-time as we need
        // to figure out the best ranking for such words compared to proximity corrections and
        // completions.
        if (ENABLE_MISSED_CHARACTERS && count < 5) {
            for (int skip = 0; skip < codesSize; skip++) {
                int tempCount = getSuggestionsNative(mNativeDict, mInputCodes, codesSize, mOutputChars, mFrequencies, MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES, skip);
                count = Math.max(count, tempCount);
                if (tempCount > 0) break;
            }
        }

        boolean requestContinue = true;
        for (int j = 0; j < count && requestContinue; j++) {
            if (mFrequencies[j] < 1) break;
            final int start = j * MAX_WORD_LENGTH;

            int position = start;
            while ((mOutputChars.length > position) && (mOutputChars[position] != 0)) {
                position++;
            }
            final int len = (position - start);
            if (len > 0) {
                requestContinue = callback.addWord(mOutputChars, start, len, mFrequencies[j], this);
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        if (word == null || mNativeDict == 0 || isClosed()) return false;
        char[] chars = word.toString().toCharArray();
        return isValidWordNative(mNativeDict, chars, chars.length);
    }

    protected final void closeAllResources() {
        if (mNativeDict != 0) {
            closeNative(mNativeDict);
            mNativeDict = 0;
        }
    }
}
