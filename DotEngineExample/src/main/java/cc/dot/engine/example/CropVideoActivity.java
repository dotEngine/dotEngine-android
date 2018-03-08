package cc.dot.engine.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cc.dot.engine.DotEngine;
import cc.dot.engine.DotStream;
import cc.dot.engine.DotView;
import cc.dot.engine.audio.DotAudioManager;
import cc.dot.engine.listener.DotEngineListener;
import cc.dot.engine.listener.DotStreamListener;
import cc.dot.engine.type.DotEngineErrorType;
import cc.dot.engine.type.DotEngineStatus;
import cc.dot.engine.type.DotEngineVideoProfileType;
import cc.dot.engine.type.DotEngineWarnType;


public class CropVideoActivity extends Activity {

    private static String TAG = CropVideoActivity.class.getSimpleName();

    private TextView textView;
    private Button button;
    private LinearLayout linearLayout;
    private String token;
    private FrameLayout videoLayout;

    private DotEngine  mDotEngine;
    private DotStream localStream;
    private String  userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        setContentView(R.layout.main_crop_items);
        videoLayout = (FrameLayout) findViewById(R.id.viewLayout);


        this.token = getIntent().getStringExtra("token");


        linearLayout = (LinearLayout) findViewById(R.id.parentLayout);

        findViewById(R.id.joinRoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Button) v).setText(v.getTag() != null ? "join room" : "leave room");
                if (v.getTag() == null) {
                    initView();
                    mDotEngine.joinRoom(token);
                } else {
                    mDotEngine.leaveRoom();
                    linearLayout.setVisibility(View.GONE);
                    linearLayout.removeAllViews();
                }

                v.setTag(v.getTag() == null ? true : null);
            }
        });


        mDotEngine = DotEngine.builder().setContext(this.getApplicationContext())
                    .setDotEngineListener(dotEngineListener).build();


        localStream = DotStream.builder(this.getApplicationContext())
                    .setAudio(true)
                    .setVideo(true)
                    .setVideoProfile(DotEngineVideoProfileType.DotEngine_VideoProfile_240P_3)
                    .build();

        //localStream.enableFaceBeauty(true);

        // beauty level  (0.0f - 1.0f)  default 0.5f
        //localStream.setBeautyLevel(0.7f);

        // bright level (0.0f - 1.0f )  default 0.5f
        //localStream.setBrightLevel(0.6f);

        // 开启视频预览  也可以通过onAddLocalStream 来预览

        addVideo(localStream.getStreamId(),localStream.getView());

    }

    private void initView() {

        textView = (TextView) findViewById(R.id.textView);

        linearLayout.setVisibility(View.VISIBLE);


        findViewById(R.id.speakerphoneOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDotEngine.enableSpeakerphone(v.getTag() != null);
                ((Button) v).setText(v.getTag() == null ? "enable speakerphone" : "disable speakerphone");
                v.setTag(v.getTag() == null ? true : null);
            }
        });


    }



    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CropVideoActivity.this, msg, Toast.LENGTH_SHORT).show();
                textView.setText(msg + "\n");

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mDotEngine != null){
            mDotEngine.pause();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDotEngine != null){
            mDotEngine.resume();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        videoLayout.removeAllViews();


        // 重要  离开的时候需要 destroy
        if (mDotEngine!=null){
            mDotEngine.destroy();
        }

        // need destroy after dotEngine
        if (localStream != null){
            localStream.destroy();
        }

    }


    private void addVideo(String user, View view) {
        //九宫格布局
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int size = videoLayout.getChildCount();
        int pw = (int) (displayMetrics.widthPixels * 0.5f);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pw, pw);

        layoutParams.leftMargin = (size % 2) * pw;
        layoutParams.topMargin = (size / 2) * pw;

        videoLayout.addView(view,layoutParams);

    }


    private void updateFrameLayout(){


        //九宫格布局
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int size = videoLayout.getChildCount();
        int pw = (int) (displayMetrics.widthPixels * 0.5f);



        for (int i=0;i<size;i++){
            View view = videoLayout.getChildAt(i);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pw, pw);

            layoutParams.leftMargin = (i % 2) * pw;
            layoutParams.topMargin = (i / 2) * pw;

            videoLayout.updateViewLayout(view,layoutParams);

        }
        Log.d(TAG,"updateFrameLayout");

    }


    private void showTip(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CropVideoActivity.this, msg, Toast.LENGTH_SHORT).show();
                textView.setText(msg + "\n");
            }
        });
    }



    private DotEngineListener dotEngineListener =  new DotEngineListener() {

        @Override
        public void onJoined(String s) {

        }

        @Override
        public void onLeave(String s) {

            showTip(s + " leave room");
        }

        @Override
        public void onOccurError(DotEngineErrorType dotEngineErrorType) {

            showTip("" + dotEngineErrorType);
        }


        @Override
        public void onWarning(DotEngineWarnType dotEngineWarnType) {

        }

        @Override
        public void onAddLocalStream(DotStream dotStream) {

            // todo nothing
        }

        @Override
        public void onRemoveLocalStream(DotStream dotStream) {

            // todo  remove the view or keep the view 
//            DotView view = dotStream.getView();
//            videoLayout.removeView(view);
//            updateFrameLayout();
        }

        @Override
        public void onAddRemoteStream(DotStream dotStream) {

            addVideo(dotStream.getPeerId(), dotStream.getView());
        }

        @Override
        public void onRemoveRemoteStream(DotStream dotStream) {

            DotView view = dotStream.getView();

            videoLayout.removeView(view);

            updateFrameLayout();

        }

        @Override
        public void onStateChange(DotEngineStatus dotEngineStatus) {

            showTip("当前的dotEngine 的状态是  -->  " + dotEngineStatus);

            if (dotEngineStatus == DotEngineStatus.DotEngineStatusConnected){

                mDotEngine.addStream(localStream);

            }
        }

        @Override
        public void onAudioDeviceChanged(DotAudioManager.AudioDevice device) {

        }
    };







}
