package pl.synth.pinry;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AccountAuthenticatorActivity {
    EditText urlText;
    EditText usernameText;
    EditText passwordText;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        urlText = (EditText) findViewById(R.id.url);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = urlText.getText().toString().trim();
                String username = usernameText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();

                if (username.length() > 0 && password.length() > 0) {
                    try {
                        new URI(url);
                    } catch (URISyntaxException e) {
                        return;
                    }

                    LoginTask task = new LoginTask(LoginActivity.this);
                    task.execute(url, username, password);
                }
            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        ProgressDialog progressDialog;

        LoginTask(Context context) {
            this.context = context;
            loginButton.setEnabled(false);
            progressDialog = ProgressDialog.show(context, "", "Authenticating", true, false);
            progressDialog.setCancelable(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            loginButton.setEnabled(true);
            progressDialog.dismiss();
            if (result) {
                finish();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient client;
            String url = params[0];
            String user = params[1];
            String password = params[2];

            String api_url = url + "/api/pin/";

            String responseString;
            try {
                List<NameValuePair> urlParams = new ArrayList<NameValuePair>();
                urlParams.add(new BasicNameValuePair("format", "json"));
                String paramString = URLEncodedUtils.format(urlParams, "utf-8");
                api_url += "?" + paramString;

                HttpGet request = new HttpGet(api_url);
                client = new DefaultHttpClient();
                HttpResponse response = client.execute(request);

                final int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode != HttpStatus.SC_OK) {
                    return false;
                }

                responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            } catch (IOException e) {
                return false;
            }

            if (!responseString.startsWith("{")) {
                return false;
            }

            Bundle data = new Bundle();
            data.putString("url", url);

            Account account = new Account(user, "pl.synth.pinry.account");
            AccountManager manager = AccountManager.get(context);
            if (manager.addAccountExplicitly(account, password, data)) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                setAccountAuthenticatorResult(result);
                return true;
            } else {
                return false;
            }
        }

    }
}
