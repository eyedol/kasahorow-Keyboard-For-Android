package com.anysoftkeyboard.buydictionary;

import com.anysoftkeyboard.buydictionary.BuyDictionaryFragment.BuyDictionaryStatusListener.State;
import com.github.jberkel.pay.me.IabHelper;
import com.github.jberkel.pay.me.IabResult;
import com.github.jberkel.pay.me.listener.OnConsumeFinishedListener;
import com.github.jberkel.pay.me.listener.OnIabPurchaseFinishedListener;
import com.github.jberkel.pay.me.listener.OnIabSetupFinishedListener;
import com.github.jberkel.pay.me.listener.QueryInventoryFinishedListener;
import com.github.jberkel.pay.me.model.Inventory;
import com.github.jberkel.pay.me.model.ItemType;
import com.github.jberkel.pay.me.model.Purchase;
import com.github.jberkel.pay.me.model.SkuDetails;
import com.github.jberkel.pay.me.model.TestSkus;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.github.jberkel.pay.me.Response.BILLING_UNAVAILABLE;
import static com.anysoftkeyboard.buydictionary.BuyDictionaryFragment.Billing.ALL_SKUS;
import static com.anysoftkeyboard.buydictionary.BuyDictionaryFragment.Billing.PUBLIC_KEY;
import static com.anysoftkeyboard.buydictionary.BuyDictionaryFragment.Billing.GOOD_PREFIX;

/**
 * Activity for buying a premium dictionary
 */
public class BuyDictionaryFragment extends Fragment implements
        QueryInventoryFinishedListener,
        OnIabPurchaseFinishedListener {

    private static boolean DEBUG_IAB = BuildConfig.DEBUG;

    private static final int PURCHASE_REQUEST = 1;

    private IabHelper mIabHelper;

    private static final String TAG = BuyDictionaryFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buydictionary, container, false);
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIabHelper = new IabHelper(getActivity(), PUBLIC_KEY);
        mIabHelper.enableDebugLogging(DEBUG_IAB);

        mIabHelper.startSetup(new OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    String message;
                    switch (result.getResponse()) {
                        case BILLING_UNAVAILABLE:
                            message = getString(R.string.buy_dictionary_error_iab_unavailable);
                            break;
                        default:
                            message = result.getMessage();
                    }

                    toastLong(message);
                    log("Problem setting up in-app billing: " + result);

                    getActivity().finish();
                } else if (mIabHelper != null) {
                    List<String> moreSkus = new ArrayList<>();
                    Collections.addAll(moreSkus, ALL_SKUS);
                    mIabHelper
                            .queryInventoryAsync(true, moreSkus, null, BuyDictionaryFragment.this);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIabHelper != null) {
            mIabHelper.dispose();
            mIabHelper = null;
        }
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        log("onQueryInventoryFinished(" + result + ", " + inventory + ")");
        if (result.isFailure()) {
            log("failed to query inventory: " + result);
            return;
        }

        List<SkuDetails> skuDetailsList = new ArrayList<>();
        for (SkuDetails d : inventory.getSkuDetails()) {
            if (d.getSku().startsWith(GOOD_PREFIX)) {
                skuDetailsList.add(d);
            }
        }
        if (DEBUG_IAB) {
            Purchase testPurchase = inventory.getPurchase(TestSkus.PURCHASED.getSku());
            if (testPurchase != null) {
                mIabHelper.consumeAsync(testPurchase, new OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        log("onConsumeFinished:" + purchase + ", " + result);
                    }
                });
            }
        }

        if (!getActivity().isFinishing() && !userHasBoughtDictionary(inventory)) {
            showSelectDialog(skuDetailsList);
        } else {
            getActivity().finish();
        }
    }


    private void showSelectDialog(List<SkuDetails> skuDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final List<SkuDetails> skus = new ArrayList<>(skuDetails);
        Collections.sort(skus, SkuComparator.INSTANCE);
        //noinspection ConstantConditions
        if (DEBUG_IAB) {
            skus.add(TestSkus.PURCHASED);
            skus.add(TestSkus.CANCELED);
            skus.add(TestSkus.UNAVAILABLE);
            skus.add(TestSkus.REFUNDED);
        }
        String[] items = new String[skus.size()];
        for (int i = 0; i < skus.size(); i++) {
            final SkuDetails sku = skus.get(i);

            String item = sku.getTitle();
            if (!TextUtils.isEmpty(sku.getPrice())) {
                item += "  " + sku.getPrice();
            }
            items[i] = item;
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIabHelper.launchPurchaseFlow(getActivity(),
                        skus.get(which).getSku(),
                        ItemType.INAPP,
                        PURCHASE_REQUEST,
                        BuyDictionaryFragment.this,
                        null);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().finish();
            }
        });

        builder.setTitle(R.string.ui_dialog_donate_message)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger("onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (!mIabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            logger("onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        logger("onIabPurchaseFinished(" + result + ", " + info);
        if (result.isSuccess()) {
            toastLong(R.string.ui_buy_dictionary);

        } else {
            String message;
            switch (result.getResponse()) {
                case ITEM_UNAVAILABLE:
                    message = getString(R.string.buy_dictionary_error_unavailable);
                    break;
                case ITEM_ALREADY_OWNED:
                    message = getString(R.string.buy_dictionary_error_already_owned);
                    break;
                case USER_CANCELED:
                    message = getString(R.string.buy_dictionary_error_canceled);
                    break;

                default:
                    message = result.getMessage();
            }

            toastLong(getString(R.string.ui_buy_dictionary_failure_message, message));
        }
        getActivity().finish();
    }

    private static boolean userHasBoughtDictionary(Inventory inventory) {
        for (String sku : ALL_SKUS) {
            if (inventory.hasPurchase(sku)) {
                
                return true;
            }
        }
        return false;
    }

    private void logger(String s) {
        if (DEBUG_IAB) {
            log(s);
        }
    }

    public static interface BuyDictionaryStatusListener {

        public enum State {
            BOUGHT,
            NOT_BOUGHT,
            UNKNOWN,
            NOT_AVAILABLE
        }

        void userBoughtDictionaryState(State s);
    }

    public static void checkUserHasBoughtDictionary(Context c,
            final BuyDictionaryStatusListener l) {
        if (Build.VERSION.SDK_INT < 8) {
            l.userBoughtDictionaryState(State.NOT_AVAILABLE);
            return;
        }

        final IabHelper helper = new IabHelper(c, PUBLIC_KEY);
        helper.startSetup(new OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    helper.queryInventoryAsync(new QueryInventoryFinishedListener() {
                        @Override
                        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                            try {
                                if (result.isSuccess()) {
                                    final State s = userHasBoughtDictionary(inv) ? State.BOUGHT
                                            : State.NOT_BOUGHT;
                                    l.userBoughtDictionaryState(s);
                                } else {
                                    l.userBoughtDictionaryState(State.UNKNOWN);
                                }
                            } finally {
                                helper.dispose();
                            }
                        }
                    });
                } else {
                    l.userBoughtDictionaryState(
                            result.getResponse() == BILLING_UNAVAILABLE ? State.NOT_AVAILABLE
                                    : State.UNKNOWN);
                    helper.dispose();
                }
            }
        });
    }

    private static class SkuComparator implements Comparator<SkuDetails> {

        static final SkuComparator INSTANCE = new SkuComparator();

        @Override
        public int compare(SkuDetails lhs, SkuDetails rhs) {
            if (lhs.getPrice() != null && rhs.getPrice() != null) {
                return lhs.getPrice().compareTo(rhs.getPrice());
            } else if (lhs.getTitle() != null && rhs.getTitle() != null) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            } else if (lhs.getSku() != null && rhs.getSku() != null) {
                return lhs.getSku().compareTo(rhs.getSku());
            } else {
                return 0;
            }
        }
    }

    protected void toastLong(int message) {
        Toast.makeText(getActivity(), getText(message), Toast.LENGTH_LONG).show();
    }

    protected void toastLong(CharSequence message) {
        Toast.makeText(getActivity(), message.toString(), Toast.LENGTH_LONG).show();
    }

    protected void log(String message) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message);
        }

    }

    protected void log(String format, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, String.format(format, args));
        }
    }

    protected void log(String message, Exception ex) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message, ex);
        }
    }

    public static class Billing {

        public static final String PUBLIC_KEY = BuildConfig.PUBLIC_LICENSE_KEY;

        public static final String GOOD_PREFIX = "dictionary.";

        public static final String SKU_AKAN_DICTIONARY = "dictionary.aka";

        public static final String SKU_GA_DICTIONARY = "dictionary.gaa";

        public static final String SKU_EWE_DICTIONARY = "dictionary.ewe";

        public static final String[] ALL_SKUS = new String[]{
                SKU_AKAN_DICTIONARY,
                SKU_GA_DICTIONARY,
                SKU_EWE_DICTIONARY,
        };
    }
}
