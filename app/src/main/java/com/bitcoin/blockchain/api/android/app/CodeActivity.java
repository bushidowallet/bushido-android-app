package com.bitcoin.blockchain.api.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bitcoin.blockchain.api.android.R;
import com.bitcoin.blockchain.api.domain.Response;
import com.bitcoin.blockchain.api.domain.Token2FARequest;
import com.bitcoin.blockchain.api.domain.UserLoginRequest;
import com.bitcoin.blockchain.api.domain.UserLoginResponse;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesion on 2015-07-06.
 */
public class CodeActivity extends Activity {

    private BushidoController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        controller = (BushidoController) getApplicationContext();
    }

    public void login(View view) {
        String authUrl = null;
        if (this.controller.env.env.equals(Env.PROD)) {
            authUrl = this.controller.env.getHTTP() + this.controller.env.restHost +"/walletapi/api/v2/user/auth/code";
        } else if(this.controller.env.env.equals(Env.DEV)) {
            authUrl = this.controller.env.getHTTP() + this.controller.env.restHost + ":" + this.controller.env.restPort + "/walletapi/api/v2/user/auth/code";
        }
        Log.i("", "Connecting to: " + authUrl);
        new ProceedTask().execute(authUrl);
    }

    public void deliverToken(View view) {
        String authUrl = null;
        if (this.controller.env.env.equals(Env.PROD)) {
            authUrl = this.controller.env.getHTTP() + this.controller.env.restHost +"/walletapi/api/v2/user/auth/code/token";
        } else if(this.controller.env.env.equals(Env.DEV)) {
            authUrl = this.controller.env.getHTTP() + this.controller.env.restHost + ":" + this.controller.env.restPort + "/walletapi/api/v2/user/auth/code/token";
        }
        Log.i("", "Connecting to: " + authUrl);
        new RequestTokenTask().execute(authUrl);
    }

    private void showView(UserLoginResponse result) {
        Intent intent = null;
        if (result.getErrors() == null || result.getErrors().size() == 0) {
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
            intent.putExtra(LoginActivity.EXTRA_LOGIN_RESPONSE, result);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private class ProceedTask extends AsyncTask<String, Void, UserLoginResponse> {
        @Override
        protected UserLoginResponse doInBackground(String... params) {
            EditText authCodeCtrl = (EditText) findViewById(R.id.authcode);
            String authCode = authCodeCtrl.getText().toString();
            List<String> cred = new ArrayList<String>();
            cred.add(authCode);
            UserLoginRequest request = new UserLoginRequest(controller.username, cred);
            HttpAuthentication authHeader = new HttpBasicAuthentication(controller.username, controller.password);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAuthorization(authHeader);
            requestHeaders.setContentType(new MediaType("application", "json"));
            HttpEntity<Object> requestEntity = new HttpEntity<Object>(request, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            try {
                ResponseEntity<UserLoginResponse> response = restTemplate.exchange(params[0], HttpMethod.POST, requestEntity, UserLoginResponse.class);
                return response.getBody();
            } catch (Exception e) {
                Log.i("", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserLoginResponse result) {
            if (result != null && (result.getErrors() == null || result.getErrors().size() == 0)) {
                Toast.makeText(getBaseContext(), "Logged u in...", Toast.LENGTH_LONG).show();
                showView(result);
            }
        }
    }

    private class RequestTokenTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Token2FARequest request = new Token2FARequest(controller.username, true);
            HttpAuthentication authHeader = new HttpBasicAuthentication(controller.username, controller.password);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAuthorization(authHeader);
            requestHeaders.setContentType(new MediaType("application", "json"));
            HttpEntity<Object> requestEntity = new HttpEntity<Object>(request, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            try {
                restTemplate.exchange(params[0], HttpMethod.POST, requestEntity, Response.class);
            } catch (Exception e) {
            }
            return null;
        }
    }
}
