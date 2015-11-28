package com.bitcoin.blockchain.api.android.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.android.app.BushidoController;
import com.bitcoin.blockchain.api.android.app.FragmentActionListener;
import com.bitcoin.blockchain.api.android.app.ReceivePageState;

/**
 * Created by Jesion on 2015-10-19.
 */
public class ReceiveAmountSectionFragment extends Fragment {

    FragmentActionListener listener;
    BushidoController controller;

    public static ReceiveAmountSectionFragment newInstance(FragmentActionListener listener, BushidoController controller) {
        final ReceiveAmountSectionFragment fragment = new ReceiveAmountSectionFragment();
        fragment.listener = listener;
        fragment.controller = controller;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_section_receive_amount, container, false);
        final Button cancelBtn = (Button) view.findViewById(R.id.closeReceiveAmount);
        final Button hit0Btn = (Button) view.findViewById(R.id.hit0);
        final Button hit1Btn = (Button) view.findViewById(R.id.hit1);
        final Button hit2Btn = (Button) view.findViewById(R.id.hit2);
        final Button hit3Btn = (Button) view.findViewById(R.id.hit3);
        final Button hit4Btn = (Button) view.findViewById(R.id.hit4);
        final Button hit5Btn = (Button) view.findViewById(R.id.hit5);
        final Button hit6Btn = (Button) view.findViewById(R.id.hit6);
        final Button hit7Btn = (Button) view.findViewById(R.id.hit7);
        final Button hit8Btn = (Button) view.findViewById(R.id.hit8);
        final Button hit9Btn = (Button) view.findViewById(R.id.hit9);
        final Button hitCommaBtn = (Button) view.findViewById(R.id.hitComma);
        final Button hitDelBtn = (Button) view.findViewById(R.id.hitDel);
        final Button hitEnterBtn = (Button) view.findViewById(R.id.hitEnter);
        cancelBtn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onButtonClick(ReceivePageState.RECEIVE_SIMPLE, null);
                        controller.clearRequestAmount();
                    }
                }
            }
        );
        hit0Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("0");
                    update();
                }
            }
        );
        hit1Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("1");
                    update();
                }
            }
        );
        hit2Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("2");
                    update();
                }
            }
        );
        hit3Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("3");
                    update();
                }
            }
        );
        hit4Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("4");
                    update();
                }
            }
        );
        hit5Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("5");
                    update();
                }
            }
        );
        hit6Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("6");
                    update();
                }
            }
        );
        hit7Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("7");
                    update();
                }
            }
        );
        hit8Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("8");
                    update();
                }
            }
        );
        hit9Btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount("9");
                    update();
                }
            }
        );
        hitCommaBtn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.addToRequestAmount(",");
                    update();
                }
            }
        );
        hitDelBtn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.deleteFromRequestAmount();
                    update();
                }
            }
        );
        hitEnterBtn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onButtonClick(ReceivePageState.ENTER, null);
                    }
                }
            }
        );
        return view;
    }

    private void update() {
        final TextView amountCtrl = (TextView) this.getView().findViewById(R.id.amountRequested);
        amountCtrl.setText( controller.requestAmountStr + " PLN" );
    }
}
