package com.bitcoin.blockchain.api.android;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.bitcoin.blockchain.api.android.app.PaymentSender;

import junit.framework.Assert;

/**
 * Created by Jesion on 2015-03-25.
 */
public class PaymentSenderTest extends ApplicationTestCase<Application> {

    public PaymentSenderTest() {
        super(Application.class);
    }

    public void testToSatoshi() {
        PaymentSender sender = new PaymentSender();
        long out1 = sender.toSatoshi("0.0005");
        Assert.assertEquals(out1, 50000);
        long out2 = sender.toSatoshi("2");
        Assert.assertEquals(out2, 100000000 * 2);
    }
}
