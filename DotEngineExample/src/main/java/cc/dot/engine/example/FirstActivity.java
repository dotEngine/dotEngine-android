package cc.dot.engine.example;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;


import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;



import org.json.JSONException;
import org.json.JSONObject;


import cc.dot.engine.DotEngine;
import cc.dot.engine.listener.TokenCallback;
import cc.dot.engine.utils.PermissionsUtils;






public class FirstActivity extends Activity {

    private Handler handler;

    private TextView mRooText;

    private String  mRoom;

    private String  mUserid;

    private PermissionsUtils permissionsUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        mRooText = (TextView)findViewById(R.id.room);

        permissionsUtils = new PermissionsUtils();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void check() {

        if (handler == null) {
            return;
        }

        findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);


        if (!permissionsUtils.hasPermissions(FirstActivity.this)) {
            permissionsUtils.requestPermissions(FirstActivity.this, new PermissionsUtils.Callback() {
                @Override
                public void onSuccess() {

                    Toast.makeText(FirstActivity.this, "got permission", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String msg) {
                    Toast.makeText(FirstActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

            // we just disable this

        findViewById(R.id.toCropActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRoom = mRooText.getText().toString();

                mUserid = "android-" + Build.DEVICE + new Random().nextInt(10000);




                DotEngine.generateTestToken(DotEngineConfig.TOKEN_URL, DotEngineConfig.APP_SECRET, mRoom, mUserid, new TokenCallback() {
                    @Override
                    public void onSuccess(String token) {

                        Intent intent = new Intent(FirstActivity.this, CropVideoActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("userid",mUserid);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(FirstActivity.this, "获取token失败", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });





    }


    @Override
    protected void onPause() {
        super.onPause();
        handler = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler = new Handler();
        check();

    }


}
