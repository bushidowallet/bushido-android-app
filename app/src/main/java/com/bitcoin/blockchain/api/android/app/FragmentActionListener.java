package com.bitcoin.blockchain.api.android.app;

/**
 * Created by Jesion on 2015-03-06.
 */
public interface FragmentActionListener {

    public void onButtonClick(String button, String payload);

    public void onPayAmountChanged(String amount);
}
