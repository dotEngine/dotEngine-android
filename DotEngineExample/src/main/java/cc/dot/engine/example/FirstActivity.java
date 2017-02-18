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
import cc.dot.engine.mode.DotEngineStatus;
import cc.dot.engine.mode.RtcResponse;
import cc.dot.engine.utils.DotEngineUtils;
import cc.dot.engine.utils.NetUtils;
import cc.dot.engine.utils.ResponseUtils;






public class FirstActivity extends Activity {

    private Handler handler;

    private TextView mRooText;

    private String  mRoom;

    private String  mUserid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        mRooText = (TextView)findViewById(R.id.room);

        if (!DotEngineUtils.checkCurrentEnvirorment(this)) {
            Toast.makeText(this, "该设备不支持", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void check() {

        if (handler == null) {
            return;
        }
        if (DotEngine.getInstance().getStatus() != DotEngineStatus.Prepare) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    check();
                }
            }, 1000);
            Toast.makeText(this, "正在开启,请稍后", Toast.LENGTH_SHORT).show();
        } else {

            findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);


            // we just disable this

            findViewById(R.id.toCropActivity).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mRoom = mRooText.getText().toString();

                    mUserid = "android-" + Build.DEVICE + new Random().nextInt(10000);




                    DotEngine.getInstance().generateTestToken(DotEngineConfig.APP_KEY, DotEngineConfig.APP_SECRET, mRoom, mUserid, new TokenCallback() {
                        @Override
                        public void onSuccess(String token) {

                            Intent intent = new Intent(FirstActivity.this, CropVideoActivity.class);
                            intent.putExtra("token", token);
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
