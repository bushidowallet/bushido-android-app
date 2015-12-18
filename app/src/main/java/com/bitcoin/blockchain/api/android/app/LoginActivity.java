package com.bitcoin.blockchain.api.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.domain.UserLoginRequest;
import com.bitcoin.blockchain.api.domain.UserLoginResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public final static String EXTRA_LOGIN_RESPONSE = "com.bitcoin.blockchain.api.android.LOGIN_RESPONSE";

    private BushidoController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        controller = (BushidoController) getApplicationContext();
        controller.initEnv(Env.PROD);
        Log.i("", "initialized env: " + controller.env.env);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.envs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        controller.initEnv((String) parent.getItemAtPosition(pos));
        Log.i("", "initialized env: " + controller.env.env);
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void login(View view) {
        String authURL = null;
        if (this.controller.env.env.equals(Env.PROD)) {
            authURL = this.controller.env.getHTTP() + this.controller.env.restHost +"/walletapi/api/v2/user/auth";
        } else if(this.controller.env.env.equals(Env.DEV)) {
            authURL = this.controller.env.getHTTP() + this.controller.env.restHost + ":" + this.controller.env.restPort + "/walletapi/api/v2/user/auth";
        }
        Log.i("", "Connecting to: " + authURL);
        new HttpAsyncTask().execute(authURL);
    }

    private void showView(UserLoginResponse result) {
        Intent intent = null;
        if (result.getErrors() == null || result.getErrors().size() == 0) {
            EditText passwordCtrl = (EditText) findViewById(R.id.password);
            BushidoController controller = (BushidoController) getApplicationContext();
            String username = result.user.username;
            String password = passwordCtrl.getText().toString();
            controller.username = username;
            controller.password = password;
            if (result.user.has2FAEnabled == false) {
                controller.user = result.user;
                controller.wallets = result.getWallets();
                if (result.getWallets().size() == 1) {
                    controller.wallet = result.getWallets().get(0);
                    intent = new Intent(this, MainScreenActivity.class);
                } else if (result.getWallets().size() == 0) {
                    intent = new Intent(this, PanelActivity.class);
                } else if (result.getWallets().size() > 1) {
                    intent = new Intent(this, PanelActivity.class);
                }
                Toast.makeText(getBaseContext(), "Logged u in...", Toast.LENGTH_LONG).show();
            } else {
                intent = new Intent(this, CodeActivity.class);
            }
            intent.putExtra(EXTRA_LOGIN_RESPONSE, result);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, UserLoginResponse> {

        @Override
        protected UserLoginResponse doInBackground(String... params) {
            EditText usernameCtrl = (EditText) findViewById(R.id.username);
            EditText passwordCtrl = (EditText) findViewById(R.id.password);
            EditText pinCtrl = (EditText) findViewById(R.id.pin);
            String username = usernameCtrl.getText().toString();
            String password = passwordCtrl.getText().toString();
            String pin = pinCtrl.getText().toString();
            List<String> cred = new ArrayList<String>();
            cred.add(password);
            cred.add(pin);
            UserLoginRequest request = new UserLoginRequest(username, cred);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(new MediaType("application", "json"));
            HttpEntity<Object> requestEntity = new HttpEntity<Object>(request, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            try {
                ResponseEntity<UserLoginResponse> response = restTemplate.exchange(params[0], HttpMethod.POST, requestEntity, UserLoginResponse.class);
                return response.getBody();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(UserLoginResponse result) {
            if (result != null && (result.getErrors() == null || result.getErrors().size() == 0)) {
                showView(result);
            }
        }
    }
}
