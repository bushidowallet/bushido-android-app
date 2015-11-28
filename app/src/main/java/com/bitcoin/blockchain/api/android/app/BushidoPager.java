package com.bitcoin.blockchain.api.android.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.android.app.fragment.HomeSectionFragment;
import com.bitcoin.blockchain.api.android.app.fragment.PaySectionFragment;
import com.bitcoin.blockchain.api.android.app.fragment.ReceiveAmountSectionFragment;
import com.bitcoin.blockchain.api.android.app.fragment.ReceiveSectionFragment;
import com.bitcoin.blockchain.api.android.app.util.QRCodeUtil;
import com.bitcoin.blockchain.api.android.barcode.BarcodeFragment;

/**
 * Created by Jesion on 2015-03-13.
 */
public class BushidoPager extends FragmentStatePagerAdapter implements FragmentActionListener {

    private Fragment currentPayFragment;
    private Fragment currentReceiveFragment;
    private FragmentManager fm;
    private BushidoController controller;
    private MainScreenActivity activity;

    public BushidoPager(FragmentManager fm, BushidoController controller, MainScreenActivity activity) {
        super(fm);
        this.fm = fm;
        this.controller = controller;
        this.activity = activity;
    }

    public void onButtonClick(String button, String payload) {
        Fragment f;
        if (button.equals(PayPageState.QR_SCANNER)) {
            currentPayFragment = BarcodeFragment.newInstance(this);
        } else if (button.equals(PayPageState.PAY_FORM)) {
            controller.setRecipientAddress(payload);
            f = PaySectionFragment.newInstance(this);
            Bundle args = new Bundle();
            args.putString("recipientAddress", controller.recipientAddress);
            args.putString("payAmount", controller.payAmount);
            f.setArguments(args);
            currentPayFragment = f;
        } else if (button.equals(PayPageState.SPEND)) {
            controller.paymentSender.spend(controller.recipientAddress, controller.payAmount, this.activity.getResources());
        } else if (button.equals(PayPageState.SPEND_ALL)) {
            controller.paymentSender.spendAll(controller.recipientAddress);
        } else if (button.equals(ReceivePageState.RECEIVE_AMOUNT)) {
            f = ReceiveAmountSectionFragment.newInstance(this, controller);
            currentReceiveFragment = f;
        } else if (button.equals(ReceivePageState.RECEIVE_SIMPLE)) {
            f = ReceiveSectionFragment.newInstance(this);
            Bundle args = new Bundle();
            args.putString("currentReceiveAddress", controller.currentReceiveAddress);
            f.setArguments(args);
            currentReceiveFragment = f;
        } else if (button.equals(ReceivePageState.ENTER)) {

        }
        notifyDataSetChanged();
    }

    public void onPayAmountChanged(String amount) {
        controller.payAmount = amount;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                HomeSectionFragment fragment = new HomeSectionFragment();
                Bundle args = new Bundle();
                args.putString("balance", controller.getBalance());
                args.putString("plnBalance", controller.getPLNBalance());
                fragment.setArguments(args);
                return fragment;
            case 1:
                if (currentPayFragment == null) {
                    Fragment payf = PaySectionFragment.newInstance(this);
                    Bundle argsSpend = new Bundle();
                    argsSpend.putString("recipientAddress", controller.recipientAddress);
                    argsSpend.putString("payAmount", controller.payAmount);
                    payf.setArguments(argsSpend);
                    currentPayFragment = payf;
                }
                return currentPayFragment;
            case 2:
                if (currentReceiveFragment == null) {
                    Fragment recf = ReceiveSectionFragment.newInstance(this);
                    Bundle argsReceive = new Bundle();
                    argsReceive.putString("currentReceiveAddress", controller.currentReceiveAddress);
                    recf.setArguments(argsReceive);
                    currentReceiveFragment = recf;
                }
                return currentReceiveFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final Resources resources = this.activity.getResources();
        switch (position) {
            case 0:
                return resources.getString(R.string.home);
            case 1:
                return resources.getString(R.string.pay);
            case 2:
                return resources.getString(R.string.receive);
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof HomeSectionFragment) {
            ((HomeSectionFragment) object).update(controller.getBalance(), controller.getPLNBalance());
        } else if (object instanceof ReceiveSectionFragment) {
            String renderedAddress = ((ReceiveSectionFragment) object).getAddress();
            if (renderedAddress.equals(controller.currentReceiveAddress) == false) {
                Bitmap qrCode = QRCodeUtil.getQRCodeBitmap(controller.currentReceiveAddress, activity);
                ((ReceiveSectionFragment) object).update(qrCode, controller.currentReceiveAddress);
            }
        }
        if (object instanceof ReceiveSectionFragment && currentReceiveFragment instanceof ReceiveAmountSectionFragment) {
            return POSITION_NONE;
        }
        if (object instanceof ReceiveAmountSectionFragment && currentReceiveFragment instanceof ReceiveSectionFragment) {
            return POSITION_NONE;
        }
        if (object instanceof PaySectionFragment) {
            ((PaySectionFragment) object).update(controller.recipientAddress, controller.payAmount);
        }
        if (object instanceof PaySectionFragment && currentPayFragment instanceof BarcodeFragment) {
            return POSITION_NONE;
        }
        if (object instanceof BarcodeFragment && currentPayFragment instanceof PaySectionFragment) {
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }
}
