package example.packt.com.dynamicregisterapp.presenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import example.packt.com.dynamicregisterapp.R;
import example.packt.com.dynamicregisterapp.client.ClientAPI;
import example.packt.com.dynamicregisterapp.client.oauth2.AccessToken;
import example.packt.com.dynamicregisterapp.client.oauth2.AccessTokenRequest;
import example.packt.com.dynamicregisterapp.client.oauth2.OAuth2StateManager;
import example.packt.com.dynamicregisterapp.client.oauth2.TokenStore;
import example.packt.com.dynamicregisterapp.client.registration.ClientCredentials;
import example.packt.com.dynamicregisterapp.client.registration.ClientCredentialsStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorizationCodeActivity  extends AppCompatActivity {

    private String code;
    private String state;
    private TokenStore tokenStore;
    private OAuth2StateManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenStore = new TokenStore(this);
        manager = new OAuth2StateManager(this);

        Uri callbackUri = Uri.parse(getIntent().getDataString());

        code = callbackUri.getQueryParameter("code");
        state = callbackUri.getQueryParameter("state");

        // validates state
        if (!manager.isValidState(state)) {
            Toast.makeText(this, "CSRF Attack detected", Toast.LENGTH_SHORT).show();
            return;
        }

        ClientCredentials credentials = ClientCredentialsStore.getInstance().get();
        Call<AccessToken> accessTokenCall = ClientAPI.oauth2(credentials)
            .requestToken(AccessTokenRequest.fromCode(code));

        accessTokenCall.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                AccessToken token = response.body();
                tokenStore.save(token);

                Intent intent = new Intent(AuthorizationCodeActivity.this,
                    ProfileActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.e("AuthorizationCode", "Error retrieving access token", t);
            }
        });
    }

}
