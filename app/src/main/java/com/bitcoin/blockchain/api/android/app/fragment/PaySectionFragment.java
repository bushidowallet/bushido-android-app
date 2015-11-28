package com.bitcoin.blockchain.api.android.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.android.app.FragmentActionListener;
import com.bitcoin.blockchain.api.android.app.PayPageState;
import com.bitcoin.blockchain.api.android.app.controls.EditTextWithLabel;

/**
 * Created by Jesion on 2015-03-13.
 */
public class PaySectionFragment extends Fragment {

    private FragmentActionListener listener;

    public PaySectionFragment() {
        super();
    }

    public static PaySectionFragment newInstance(FragmentActionListener listener) {
        final PaySectionFragment fragment = new PaySectionFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_section_pay, container, false);
        final EditTextWithLabel recipientAddressCtrl = (EditTextWithLabel) view.findViewById(R.id.recipientAddress);
        final EditTextWithLabel amountCtrl = (EditTextWithLabel) view.findViewById(R.id.amount);
        final Bundle args = getArguments();
        Log.i("", "Creating view with amount: " + args.getString("payAmount"));
        amountCtrl.setText(args.getString("payAmount"));
        amountCtrl.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (listener != null) {
                            listener.onPayAmountChanged(amountCtrl.getText().toString());
                        }
                    }
                }
        );
        amountCtrl.setLabel("Pay : ");
        recipientAddressCtrl.setLabel("To : ");
        recipientAddressCtrl.setText(args.getString("recipientAddress"));
        final Button scanButton = (Button) view.findViewById(R.id.scan_code);
        scanButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftInput(amountCtrl, (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                        if (listener != null) {
                            listener.onButtonClick(PayPageState.QR_SCANNER, null);
                        } else {
                            Log.e("", "WTF there is no listener, how is this possible...");
                        }
                    }
                }
        );
        final Button spendAllButton = (Button) view.findViewById(R.id.spend_all);
        final Button spendButton = (Button) view.findViewById(R.id.spend);
        spendAllButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftInput(amountCtrl, (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                        if (listener != null) {
                            listener.onButtonClick(PayPageState.SPEND_ALL, null);
                        }
                    }
                }
        );
        spendButton.setOnClickListener (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftInput(amountCtrl, (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                        if (listener != null) {
                            listener.onButtonClick(PayPageState.SPEND, null);
                        }
                    }
                }
        );
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            final EditText amountCtrl = (EditText) getView().findViewById(R.id.amount);
            if (isVisibleToUser) {
                amountCtrl.requestFocus();
                imm.showSoftInput(amountCtrl, InputMethodManager.SHOW_IMPLICIT);
            } else {
                hideSoftInput(amountCtrl, imm);
            }
        }
    }

    public void update(String recipientAddress, String payAmount) {
        Log.i("", "Fragment update: " + recipientAddress + " amount " + payAmount);
        final TextView addressCtrl = (TextView) this.getView().findViewById(R.id.recipientAddress);
        final EditText amountCtrl = (EditText) this.getView().findViewById(R.id.amount);
        addressCtrl.setText(recipientAddress);
        amountCtrl.setText(payAmount);
    }

    private void hideSoftInput(EditText amountCtrl, InputMethodManager imm) {
        amountCtrl.clearFocus();
        imm.restartInput(amountCtrl);
        imm.hideSoftInputFromWindow(amountCtrl.getWindowToken(), 0);
    }
}
