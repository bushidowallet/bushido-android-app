package com.bitcoin.blockchain.api.android.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitcoin.blockchain.api.android.R;

/**
 * Created by Jesion on 2015-03-13.
 */
public class HomeSectionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_section_home, container, false);
        final TextView balanceCtrl = (TextView) view.findViewById(R.id.balance);
        final Bundle args = getArguments();
        balanceCtrl.setText(args.getString("balance"));
        return view;
    }

    public void update(String balance, String plnBalance) {
        final TextView balanceCtrl = (TextView) this.getView().findViewById(R.id.balance);
        final TextView plnBalanceCtrl = (TextView) this.getView().findViewById(R.id.plnBalance);
        balanceCtrl.setText(balance);
        plnBalanceCtrl.setText(plnBalance);
    }
}
