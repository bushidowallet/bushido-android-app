package com.bitcoin.blockchain.api.android.app.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.android.app.FragmentActionListener;
import com.bitcoin.blockchain.api.android.app.util.QRCodeUtil;

/**
 * Created by Jesion on 2015-03-13.
 */
public class ReceiveSectionFragment extends Fragment {

    FragmentActionListener listener;

    public ReceiveSectionFragment() {
        super();
    }

    public static ReceiveSectionFragment newInstance(FragmentActionListener listener) {
        ReceiveSectionFragment fragment = new ReceiveSectionFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_section_receive, container, false);
        final Bundle args = getArguments();
        if (args != null);
        {
            final String addr = args.getString("currentReceiveAddress");
            update( QRCodeUtil.getQRCodeBitmap(addr, getActivity()), addr, (TextView) view.findViewById(R.id.address), (ImageView) view.findViewById(R.id.qr_code));
        }
        /* Request Amount Button is disabled for now - until the Fragment work is ready
        Button receiveAmountBtn = (Button) view.findViewById(R.id.receiveAmount);
        receiveAmountBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onButtonClick(ReceivePageState.RECEIVE_AMOUNT, null);
                        } else {
                            Log.e("", "WTF there is no listener, how is this possible...");
                        }
                    }
                }
        );
        */
        return view;
    }

    public String getAddress() {
        final TextView addressCtrl = (TextView) this.getView().findViewById(R.id.address);
        return addressCtrl.getText().toString();
    }

    public void update(Bitmap qrCodeBitmap, String address) {
        final TextView addressCtrl = (TextView) this.getView().findViewById(R.id.address);
        if (addressCtrl.getText() == null || addressCtrl.getText().toString().equals(address) == false) {
            final ImageView qrCodeCtrl = (ImageView) this.getView().findViewById(R.id.qr_code);
            qrCodeCtrl.setImageBitmap(qrCodeBitmap);
            addressCtrl.setText(address);
            Log.i("", "Updated address and QR code.");
        } else {
            Log.i("", "Address " + address + " already presented");
        }
    }

    private void update(Bitmap qrCodeBitmap, String address, TextView addressCtrl, ImageView qrCodeCtrl) {
        qrCodeCtrl.setImageBitmap(qrCodeBitmap);
        addressCtrl.setText(address);
    }
}
