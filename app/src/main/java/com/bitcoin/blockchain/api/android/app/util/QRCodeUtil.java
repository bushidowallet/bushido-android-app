package com.bitcoin.blockchain.api.android.app.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

/**
 * Created by Jesion on 2015-10-19.
 */
public class QRCodeUtil {

    public static Bitmap getQRCodeBitmap(String address, Activity activity) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int width = displaymetrics.widthPixels;
        final Intent intent = new Intent(Intents.Encode.ACTION);
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, address);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        try {
            Log.i("", "Initializing QR: " + width);
            final QRCodeEncoder encoder = new QRCodeEncoder(activity, intent, width, false);
            return encoder.encodeAsBitmap();
        } catch (Exception e) {

        }
        return null;
    }
}
