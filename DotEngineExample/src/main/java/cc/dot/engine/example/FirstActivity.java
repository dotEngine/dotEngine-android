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


import java.util.concurrent.ThreadLocalRandom;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;




import cc.dot.engine.DotEngine;
import cc.dot.engine.mode.DotEngineStatus;
import cc.dot.engine.mode.RtcResponse;
import cc.dot.engine.utils.DotEngineUtils;
import cc.dot.engine.utils.NetUtils;
import cc.dot.engine.utils.ResponseUtils;



public class FirstActivity extends Activity {

    private Handler handler;

    private TextView mRooText;

    private String  mRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String host = "182.92.152.61:8000";


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

                    getToken(new CallBack() {
                        @Override
                        public void onSuccess(String token) {
                            Intent intent = new Intent(FirstActivity.this, CropVideoActivity.class);
                            intent.putExtra("token", token);
                            startActivity(intent);
                            finish();
                        }
                    });


                }
            });




        }
    }

    private void getToken(final CallBack callBack) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                String value = NetUtils.getValue(String.format("http://182.92.152.61:5001/getToken?room=%s&user_id=%s", mRoom, Build.DEVICE.toString() + ThreadLocalRandom.current().nextInt(1,1000)));
                if (TextUtils.isEmpty(value)) {
                    return null;
                }
                RtcResponse<String> rtcResponse = JSON.parseObject(value, RtcResponse.type(String.class));

                JSONObject response = JSON.parseObject(value);



                if (ResponseUtils.isOkAndHasData(rtcResponse)) {

                    String d = rtcResponse.getD();
                    JSONObject jsonObject = JSON.parseObject(d);

                    return jsonObject.getString("token");
                }
                return null;
            }

            @Override
            protected void onPostExecute(final String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(FirstActivity.this, "token 获取失败", Toast.LENGTH_SHORT).show();

                    return;
                }
                callBack.onSuccess(s);


            }
        }.execute();
    }

    interface CallBack {
        void onSuccess(String token);
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
