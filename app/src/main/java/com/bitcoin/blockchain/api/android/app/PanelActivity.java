package com.bitcoin.blockchain.api.android.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bitcoin.blockchain.api.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jesion on 2015-07-06.
 */
public class PanelActivity extends Activity {

    private BushidoController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = (BushidoController) getApplicationContext();
        setContentView(R.layout.activity_panel);
        final ListView listview = (ListView) findViewById(R.id.lst);
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < controller.wallets.size(); ++i) {
            list.add(controller.wallets.get(i).key);
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    final int position, long id) {
                controller.wallet = controller.wallets.get(position);
                Log.i("", "Set wallet to: " +controller.wallet.toString());
                startActivity( new Intent(getApplicationContext(), MainScreenActivity.class));
            }
        });
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}


