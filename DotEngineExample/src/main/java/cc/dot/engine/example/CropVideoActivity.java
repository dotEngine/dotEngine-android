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
import cc.dot.engine.Engine;
import cc.dot.engine.listener.DotEngineListener;
import cc.dot.engine.type.DotEngineErrorType;
import cc.dot.engine.type.DotEngineVideoProfileType;
import cc.dot.engine.type.DotEngineWarnType;


public class CropVideoActivity extends Activity {

    private static String TAG = CropVideoActivity.class.getSimpleName();

    private TextView textView;
    private Button button;
    private LinearLayout linearLayout;
    private String token;
    private FrameLayout videoLayout;


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
                } else {
                    DotEngine.getInstance().leaveRoom();
                    linearLayout.setVisibility(View.GONE);
                    linearLayout.removeAllViews();
                    videoLayout.removeAllViews();

                    DotEngine.getInstance().stopLocalMedia();
                }

                v.setTag(v.getTag() == null ? true : null);
            }
        });

        //DotEngine.getInstance().setCaptureMode(DotEngine.DotEngineCaptureMode.DotEngine_Capture_Default);

    }

    private void initView() {

        initAndStartPreview();
        linearLayout.setVisibility(View.VISIBLE);

        textView = (TextView) findViewById(R.id.textView);

        button = (Button) findViewById(R.id.btnSwitchCamera);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DotEngine.getInstance().switchCamera();

            }
        });

        findViewById(R.id.btnSwitchEnableVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DotEngine.getInstance().enableVideo(v.getTag() != null);
                ((Button) v).setText(v.getTag() == null ? "enable video" : "disable video");
                v.setTag(v.getTag() == null ? true : null);

            }
        });


        findViewById(R.id.btnSwitchEnableAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DotEngine.getInstance().enableAudio(v.getTag() != null);
                ((Button) v).setText(v.getTag() == null ? "enable audio" : "disable audio");
                v.setTag(v.getTag() == null ? true : null);

            }
        });


        findViewById(R.id.speakerphoneOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DotEngine.getInstance().enableSpeakerphone(v.getTag() != null);
                ((Button) v).setText(v.getTag() == null ? "enable speakerphone" : "disable speakerphone");
                v.setTag(v.getTag() == null ? true : null);
            }
        });


    }

    private void initAndStartPreview() {
        DotEngine.getInstance().sharedInstanceWithListener(this,
                new DotEngineListener() {
                    @Override
                    public void onJoined(final String user) {
                        showToast(user + " join room  可以拖动视频看看效果");
                        if (user.equals(DotEngine.getInstance().getCurrentUser())) {
                            return;
                        }


                    }

                    @Override
                    public void onLeave(String user) {
                        showToast(user + " leave room");

                    }

                    @Override
                    public void onOccurError(DotEngineErrorType errorType) {
                        showToast("" + errorType);
                    }

                    @Override
                    public void onWarning(DotEngineWarnType dotEngineWarnType) {

                    }


                    @Override
                    public void onInitPrepared() {
                        DotEngine.getInstance().startLocalMedia();
                        DotEngine.getInstance().joinRoom(token);
                    }


                    @Override
                    public void onEnableAudio(boolean enable, String user) {
                        showToast(user + " audio  " + (enable ? " enable :" : " disable "));
                    }

                    @Override
                    public void onEnableVideo(boolean enable, String user) {
                        showToast(user + " video " + (enable ? " enable :" : " disable "));


                    }

                    @Override
                    public void onAddLocalView(SurfaceView surfaceView) {

                        addVideo(null,surfaceView);
                    }

                    @Override
                    public void onAddRemoteView(String s, SurfaceView surfaceView) {

                        addVideo(s, surfaceView);
                    }

                    @Override
                    public void onRemoveLocalView(SurfaceView surfaceView) {

                        videoLayout.removeView(surfaceView);

                        updateFrameLayout();
                    }

                    @Override
                    public void onRemoveRemoteView(String s, SurfaceView surfaceView) {

                        videoLayout.removeView(surfaceView);

                        updateFrameLayout();
                    }


                });


        DotEngine.getInstance().setupVideoProfile(DotEngineVideoProfileType.DotEngine_VideoProfile_360P);

        DotEngine.getInstance().startInit();

    }


    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CropVideoActivity.this, msg, Toast.LENGTH_SHORT).show();
                textView.setText(msg + "\n");
                for (String use : DotEngine.getInstance().getUsers()) {
                    textView.append(use + "\n");
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        DotEngine.getInstance().onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        DotEngine.getInstance().onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoLayout.removeAllViews();
        DotEngine.getInstance().onDestroy();//立刻房间,回收资源
    }


    private void addVideo(String user, SurfaceView view) {
        //九宫格布局
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int size = videoLayout.getChildCount();
        int pw = (int) (displayMetrics.widthPixels * 0.5f);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pw, pw);

        layoutParams.leftMargin = (size % 2) * pw;
        layoutParams.topMargin = (size / 2) * pw;

        videoLayout.addView(view,layoutParams);


        view.setZOrderOnTop(true);

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


}
