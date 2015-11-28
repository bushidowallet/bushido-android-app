package com.bitcoin.blockchain.api.android.app;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.bitcoin.blockchain.api.Command;
import com.bitcoin.blockchain.api.android.messaging.WalletAPI;
import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.domain.Balance;
import com.bitcoin.blockchain.api.domain.BalanceChange;
import com.bitcoin.blockchain.api.domain.FCBalance;
import com.bitcoin.blockchain.api.domain.Transaction;
import com.bitcoin.blockchain.api.domain.UserLoginResponse;
import com.bitcoin.blockchain.api.domain.WalletChange;
import com.bitcoin.blockchain.api.domain.WalletInfo;
import com.bitcoin.blockchain.api.domain.message.BalanceChangeMessage;
import com.bitcoin.blockchain.api.domain.message.GetAddressMessage;
import com.bitcoin.blockchain.api.domain.message.WalletChangeMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreenActivity extends FragmentActivity implements ActionBar.TabListener {

    private BushidoController controller;

    BushidoPager pager;

    ViewPager mViewPager;

    WalletAPI walletAPI;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        controller = (BushidoController) getApplicationContext();
        pager = new BushidoPager(getSupportFragmentManager(), controller, this);
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(pager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < pager.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pager.getPageTitle(i))
                            .setTabListener(this));
        }
        setup();
    }

    private void setup() {
        Intent intent = getIntent();
        controller.login = (UserLoginResponse) intent.getSerializableExtra(LoginActivity.EXTRA_LOGIN_RESPONSE);
        walletAPI = new WalletAPI(controller.env.socketHost,
                controller.env.socketPort,
                controller.username,
                controller.password,
                controller.wallet.getKey(),
                this);
        walletAPI.addListener(Command.BALANCE_CHANGE_RECEIVED, "balanceChangeReceivedHandler");
        walletAPI.addListener(Command.FC_BALANCE_CHANGE_RECEIVED, "fcBalanceChangeReceivedHandler");
        walletAPI.addListener(Command.BALANCE_CHANGE_SPENT, "balanceChangeSpentHandler");
        walletAPI.addListener(Command.BALANCE_CHANGE_STATUS, "balanceChangeStatusHandler");
        controller.paymentSender = new PaymentSender(walletAPI);
        requestAddress();
    }

    public void fcBalanceChangeReceivedHandler(BalanceChangeMessage message) {
        controller.wallet.info.fcBalances = message.getPayload().getFCBalances();
    }

    public void balanceChangeReceivedHandler(WalletChangeMessage message) {
        WalletChange change = message.getPayload();
        updateBalance(change.getBalance());
        Transaction newTx = change.getTx();
        for (int i = 0; i < newTx.outputs.size(); i++) {
            if (newTx.outputs.get(i).getToAddress().equals(controller.currentReceiveAddress)) {
                controller.currentReceiveAddress = change.getCurrentAddress();
            }
        }
        updatePages();
    }

    public void balanceChangeSpentHandler(WalletChangeMessage message) {
        WalletChange change = message.getPayload();
        updateBalance(change.getBalance());
        updatePages();
    }

    public void balanceChangeStatusHandler(BalanceChangeMessage message) {
        BalanceChange change = message.getPayload();
        updateBalance(change.getBalance());
        updatePages();
    }

    private void updateBalance(Balance balance) {
        if (controller.wallet.info == null) {
            controller.wallet.info = new WalletInfo();
            controller.wallet.info.balance = new Balance(0, 0);
        }
        controller.wallet.info.balance.confirmed = balance.confirmed;
        controller.wallet.info.balance.unconfirmed = balance.unconfirmed;
    }

    private void updateCurrencyBalance(List<FCBalance> balances) {
        controller.wallet.info.fcBalances = balances;
    }

    private void requestAddress() {
        Map<String, Object> payload = new HashMap<String,Object>();
        payload.put("account", 0);
        walletAPI.invoke(Command.GET_ADDRESS, payload, "getAddressHandler");
    }

    public void getAddressHandler(GetAddressMessage message) {
        WalletInfo payload = (WalletInfo) message.getPayload();
        Log.i("", "Reveiced address message: " + payload);
        controller.currentReceiveAddress = payload.currentAddress;
        updateBalance(payload.balance);
        updateCurrencyBalance(payload.fcBalances);
        updatePages();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        int p = tab.getPosition();
        Log.i("", "Setting index to " + p);
        mViewPager.setCurrentItem(p);
        if (tab.getPosition() == 2) {
            if (controller.currentReceiveAddress == null) {
                requestAddress();
            } else {
                updatePages();
            }
        } else if (tab.getPosition() == 1) {
            updatePages();
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {


    }

    private void updatePages() {
        pager.notifyDataSetChanged();
        mViewPager.invalidate();
    }
}
