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
import cc.dot.engine.listener.DotEngineListener;
import cc.dot.engine.type.DotEngineErrorType;
import cc.dot.engine.type.DotRTCEngineVideoProfileType;


/**
 * Created by haizhu on 16/5/21.
 */

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
                }

                v.setTag(v.getTag() == null ? true : null);
            }
        });

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
        DotEngine instance = DotEngine.getInstance();
        instance.sharedInstanceWithListener(this,
                new DotEngineListener() {
                    @Override
                    public void onJoined(final String user) {
                        showToast(user + " join room  可以拖动视频看看效果");
                        if (user.equals(DotEngine.getInstance().currentUser())) {
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
                    public void onInitPrepared() {
                        DotEngine.getInstance().startPreview();
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
                    public void onAddVideoView(String user, SurfaceView view) {


                        //九宫格布局
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

                        int size = videoLayout.getChildCount();
                        int pw = (int) (displayMetrics.widthPixels * 0.5f);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(pw, pw);

                        layoutParams.leftMargin = (size % 2) * pw;
                        layoutParams.topMargin = (size / 2) * pw;

                        videoLayout.addView(view, layoutParams);

                    }

                    @Override
                    public void onRemoveVideoView(String user, SurfaceView view) {
                        videoLayout.removeView(view);

                        updateFrameLayout();

                    }


                    @Override
                    public void onAddStream(String user) {
                        super.onAddStream(user);

                        // 有一个stream 添加进来了
                    }


                    @Override
                    public void onRemoveStream(String user) {
                        super.onRemoveStream(user);

                        // 有一个stream 被删除掉了


                    }
                });


        instance.setupVideoProfile(DotRTCEngineVideoProfileType.DotEngine_VideoProfile_360P);


        instance.startInit();
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
        DotEngine.getInstance().stopPreview();//暂停预览

    }

    @Override
    public void onResume() {
        super.onResume();
        DotEngine.getInstance().onResume();//中途恢复
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoLayout.removeAllViews();
        DotEngine.getInstance().leaveRoom();//立刻房间,回收资源
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
}
