package com.bitcoin.blockchain.api.android.app;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.bitcoin.blockchain.api.FiatCurrency;
import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.bitcoin.BitcoinMetric;
import com.bitcoin.blockchain.api.domain.User;
import com.bitcoin.blockchain.api.domain.UserLoginResponse;
import com.bitcoin.blockchain.api.domain.V2WalletDescriptor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Jesion on 2015-02-25.
 */
public class BushidoController extends Application {

    public UserLoginResponse login;
    public String currentReceiveAddress;
    public String username;
    public String password;
    public String recipientAddress = "";
    public String payAmount;
    public double requestAmount = 0.0;
    public String requestAmountStr = "";
    public PaymentSender paymentSender;
    public Env env;
    public User user;
    public List<V2WalletDescriptor> wallets;
    public V2WalletDescriptor wallet;

    public BushidoController() {

    }

    public String getBalance(long confirmed, long unconfirmed) {
        final BigDecimal c = new BigDecimal(confirmed);
        final BigDecimal u = new BigDecimal(unconfirmed);
        final BigDecimal s = new BigDecimal(BitcoinMetric.SATOSHI);
        final BigDecimal btcc = c.divide(s);
        final BigDecimal btcu = u.divide(s);
        return btcc.toString() + " BTC / (" + btcu.toString() + " BTC)";
    }

    public String getPLNBalance() {
        final String empty = "0.00 PLN";
        if (wallet.info != null && wallet.info.fcBalances != null) {
            for (int i = 0; i < wallet.info.fcBalances.size(); i++) {
                if (wallet.info.fcBalances.get(i).currency.equals(FiatCurrency.PLN)) {
                    if (wallet.info.fcBalances.get(i).balance.equals(BigDecimal.ZERO)) {
                        return empty;
                    }
                    return wallet.info.fcBalances.get(i).balance.toString() + " PLN";
                }
            }
        }
        return empty;
    }

    public String getBalance() {
        if (wallet.info != null) {
            return getBalance(wallet.info.balance.confirmed, wallet.info.balance.unconfirmed);
        } else {
            return getBalance(0, 0);
        }
    }

    public void setRecipientAddress(String scannedAddress) {
        if (scannedAddress == null) {
            return;
        }
        final String[] parts = scannedAddress.split(":");
        if (parts.length == 2) {
            this.recipientAddress = parts[1];
        } else {
            this.recipientAddress = scannedAddress;
        }
        Log.i("", "Setting recipient address to: " + recipientAddress);
    }

    public void addToRequestAmount(String sign) {
        if (sign.equals(",")) {

        }
        if (requestAmountStr.equals("0") && sign.equals(",") == false) {
            requestAmountStr = "";
        }
        requestAmountStr = requestAmountStr + sign;
    }

    public void deleteFromRequestAmount() {
        final String remaining = requestAmountStr.substring(0, requestAmountStr.length() - 1);
        if (remaining.length() > 0) {
            requestAmountStr = remaining;
        } else {
            requestAmountStr = "0";
        }
    }

    public void clearRequestAmount() {
        requestAmount = 0.0;
        requestAmountStr = "";
    }

    public void initEnv(String envStr) {
        final Resources resources = getResources();
        final Env env = new Env();
        env.env = envStr;
        if (envStr.equals(Env.PROD)) {
            env.useSSL = true;
            env.restHost = resources.getString(R.string.rest_host_prod);
            env.restPort = Integer.parseInt(resources.getString(R.string.rest_port_prod));
            env.socketHost = resources.getString(R.string.socket_host_prod);
            env.socketPort = Integer.parseInt(resources.getString(R.string.socket_port_prod));
        } else if (envStr.equals(Env.DEV)) {
            env.useSSL = false;
            env.restHost = resources.getString(R.string.rest_host_dev);
            env.restPort = Integer.parseInt(resources.getString(R.string.rest_port_dev));
            env.socketHost = resources.getString(R.string.socket_host_dev);
            env.socketPort = Integer.parseInt(resources.getString(R.string.socket_port_dev));
        }
        this.env = env;
    }
}
