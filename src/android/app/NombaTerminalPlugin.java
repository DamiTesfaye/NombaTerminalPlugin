package com.nomba.terminal.plugin;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * This class bridges the gap between the Cordova JavaScript and the native Android code.
 */
public class NombaTerminalPlugin extends CordovaPlugin {

    private static final int ARGS_PRINT_RECEIPT_EVENT = 1944;
    private static final String AMOUNT_DATA = "amount";
    private static final String MERCHANT_TX_REF = "merchantTxRef";
    private static final String RECEIPT_OPTIONS = "receiptOptions";
    private static final String TXN_RESULT = "txnResultData";
    private static final String PRINT_RESULT = "PRINT_RESULT";
    private static final String PAY_BY_TRANSFER_INTENT = "com.nomba.pro.feature.pay_by_transfer.ACTION_VIEW";
    private static final String CARD_AND_PBT_INTENT = "com.nomba.pro.feature.payment_option.ACTION_VIEW";
    private static final String PRINT_CUSTOM_RECEIPT_INTENT = "com.nomba.pro.core.print_receipt.ACTION_VIEW";
    private static final String CARD_PAYMENT = "com.nomba.pro.feature.payment_option.ACTION_VIEW";
    private static final String ARGS_PAYMENT_OPTION_STATE = "ARGS_PAYMENT_OPTION_STATE";
    private static final String SDK_PAYMENT_OPTIONS = "SDK_PAYMENT_OPTIONS";
    private static final String ARGS_PRINT_DATA = "ARGS_PRINT_DATA";
    private static final String ARGS_PRINT_BITMAP_DATA = "ARGS_PRINT_BITMAP_DATA";

    private CallbackContext callbackContext;

    @Override
    public boolean execute(final String action,
                           final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        Log.d("NombaProPlugin", "execute: " + action + " " + args.toString());

        switch (action) {
            case "terminalRequest":
                return handleTerminalRequest(args);
            default:
                return false;
        }
    }

    private boolean handleTerminalRequest(JSONArray args) throws JSONException {
        final String actionKey = args.getString(0);
        final boolean isPayment = !Objects.equals(actionKey,
                "triggerPrintCustomReceipt") && !Objects.equals(
                actionKey,
                "getDeviceInfo");
        final String amount = isPayment ? args.getString(1) : "";
        final String transactionReference = isPayment ? args.getString(2) : "";
        final String receiptData = isPayment ? args.getString(3) : "";


        switch (actionKey) {
            case "triggerCardPayment":
                triggerPayment(CARD_PAYMENT, amount, transactionReference, receiptData);
                return true;
            case "triggerPayByTransfer":
                triggerPayment(PAY_BY_TRANSFER_INTENT, amount, transactionReference, receiptData);
                return true;
            case "triggerCardAndPBT":
                triggerCardAndPBT(amount, transactionReference, receiptData);
                return true;
            case "triggerPrintCustomReceipt":
                triggerPrintCustomReceipt(args.getString(1));
                return true;
            default:
                return false;
        }
    }

    private void triggerPayment(String intentAction,
                                String amount,
                                String transactionReference,
                                String receiptData) {
        try {
            Intent intent = new Intent(intentAction);
            intent.putExtra(AMOUNT_DATA, amount);
            intent.putExtra(MERCHANT_TX_REF, transactionReference);

            intent.putExtra(RECEIPT_OPTIONS, getReceiptOptions(receiptData));

            cordova.startActivityForResult(this, intent, RESULT_OK);
        } catch (Exception e) {
            handleError("Failed to complete payment action", e);
        }
    }

    private void triggerCardAndPBT(String amount, String transactionReference, String receiptData) {
        try {
            Intent intent = new Intent(CARD_AND_PBT_INTENT);
            intent.putExtra(AMOUNT_DATA, amount);
            intent.putExtra(MERCHANT_TX_REF, transactionReference);
            intent.putExtra(RECEIPT_OPTIONS, getReceiptOptions(receiptData));
            intent.putExtra(ARGS_PAYMENT_OPTION_STATE, SDK_PAYMENT_OPTIONS);

            cordova.startActivityForResult(this, intent, RESULT_OK);
        } catch (Exception e) {
            handleError("Failed to complete transfer action", e);
        }
    }

    private void triggerPrintCustomReceipt(String receiptData) {
        try {
            Intent intent = new Intent(PRINT_CUSTOM_RECEIPT_INTENT);
            intent.putExtras(createPrintReceiptBundle(receiptData));

            cordova.startActivityForResult(this, intent, ARGS_PRINT_RECEIPT_EVENT);
        } catch (Exception e) {
            handleError("Failed to print custom receipt", e);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RESULT_OK) {
            if (intent != null) {
                callbackContext.success(intent.getStringExtra(TXN_RESULT));
            } else {
                handleError("Intent result is null", null);
            }
        } else if (requestCode == ARGS_PRINT_RECEIPT_EVENT) {
            if (intent != null) {
                callbackContext.success(intent.getStringExtra(PRINT_RESULT));
            } else {
                handleError("Intent result is null", null);
            }
        } else {
            handleError("Unexpected requestCode", null);
        }
    }

    private void handleError(String errorMessage, Exception exception) {
        Log.e("NombaProPlugin", errorMessage, exception);
        callbackContext.error(errorMessage + (exception != null ? " :: " + exception.getMessage() : ""));
    }

    private String getReceiptOptions(String receiptData) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        HashMap<String, Object> hashMap = gson.fromJson(receiptData, type);
        return gson.toJson(hashMap);
    }

    private Bundle createPrintReceiptBundle(String receiptData) {
        Log.d("NombaProPlugin", "createPrintReceiptBundle: " + receiptData);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<HashMap<String, Object>>>() {
        }.getType();
        ArrayList<HashMap<String, Object>> arrayList = gson.fromJson(receiptData, type);

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGS_PRINT_DATA, arrayList);

//        Bitmap receiptLogo = drawableToBitmap(AppCompatResources.getDrawable(cordova.getContext(),
//                R.drawable.nombalogo));
//
//        bundle.putParcelable(ARGS_PRINT_BITMAP_DATA, receiptLogo);

        return bundle;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
