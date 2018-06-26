package com.example.dengchu.eglrtmpsender;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.example.dengchu.eglrtmpsender.Filter.ChooseFilter;
import com.example.dengchu.eglrtmpsender.Filter.GroupFilter;
import com.example.dengchu.eglrtmpsender.MediaManager.MediaConfig;
import com.example.dengchu.eglrtmpsender.MediaManager.SimpleStreamer;
import com.example.dengchu.eglrtmpsender.WheelView.WheelView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    private TextureView mVideoPreview;
    private SimpleStreamer Streamer;
    private int filterIndex;
    private GroupFilter mFilter;
    private MediaConfig mConfig = new MediaConfig();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFilter = new GroupFilter(getResources());
        Streamer = new SimpleStreamer(mConfig);
        Streamer.setRender(mFilter);
        mVideoPreview = (TextureView)findViewById(R.id.video_preview);
        initWheel();
        mVideoPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                Streamer.setPreviewSurface(new Surface(surfaceTexture));
                Streamer.setPreviewSize(i, i1);
                Streamer.startPreview();
                Streamer.startRecord();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
    }

    /**
     * 设置滤镜选择控件
     */
    private void initWheel() {
        final List<FilterChoose> filterChooses = new ArrayList<>();
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.NORMAL, "默认"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.COOL, "寒冷"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.WARM, "温暖"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.GRAY, "灰度"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.CAMEO, "浮雕"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.INVERT, "底片"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.SEPIA, "旧照"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.TOON, "动画"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.CONVOLUTION, "卷积"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.SOBEL, "边缘"));
        filterChooses.add(new FilterChoose(ChooseFilter.FilterType.SKETCH, "素描"));
        WheelView wheelView = (WheelView) findViewById(R.id.change_fliter);
        wheelView.setAdapter(new FilterAdapter(filterChooses));
        wheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {

                changeFilter(filterChooses.get(index).getIndex());
                Toast.makeText(MainActivity.this, "选择滤镜:" + filterChooses.get(index).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 切换滤镜
     */
    private void changeFilter(int chooseIndex) {
        if (chooseIndex != filterIndex) {
            filterIndex = chooseIndex;
            mFilter.getChooseFilter().setChangeType(chooseIndex);
        }
    }

}
