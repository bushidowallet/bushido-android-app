package com.bitcoin.blockchain.api.android.app;

import android.content.res.Resources;
import android.util.Log;

import com.bitcoin.blockchain.api.Command;
import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.android.messaging.WalletAPI;
import com.bitcoin.blockchain.api.bitcoin.BitcoinMetric;
import com.bitcoin.blockchain.api.domain.SpendDescriptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesion on 2015-03-13.
 */
public class PaymentSender {

    private WalletAPI walletAPI;

    public PaymentSender(WalletAPI walletAPI) {
        this.walletAPI = walletAPI;
    }

    public PaymentSender() {

    }

    public void spend(String receivingAddress, String payAmount, Resources resources) {
        final String sendStr = resources.getString(R.string.send);
        final String amountOfStr = resources.getString(R.string.amount_of);
        Log.i("", sendStr + " " + receivingAddress + " " + amountOfStr + " " + payAmount);
        final List<SpendDescriptor> spendings = new ArrayList<SpendDescriptor>();
        spendings.add(new SpendDescriptor(receivingAddress, toSatoshi(payAmount)));
        walletAPI.invoke(Command.SPEND, spendings, null);
    }

    public void spendAll(String receivingAddress) {
        walletAPI.invoke(Command.SPEND_ALL_UTXO, receivingAddress, null);
    }

    public long toSatoshi(String btc) {
        final BigDecimal pay = new BigDecimal(btc);
        final BigDecimal s = new BigDecimal(BitcoinMetric.SATOSHI);
        final BigDecimal r = pay.multiply(s);
        return r.longValue();
    }
}
