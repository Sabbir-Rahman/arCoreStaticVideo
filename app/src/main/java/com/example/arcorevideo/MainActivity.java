package com.example.arcorevideo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    /*
        create an object of external texture
        create an object of media player
        set mediaplayer on external textures surface
        build the 3d model and set it's texture to External texture that has media player on its surface
        filter out chroma key color from the video

         */
    private ModelRenderable videoRenderable;
    private float HEIGHT = 1.25f;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExternalTexture texture = new ExternalTexture();

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.video);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);


        ModelRenderable
                .builder()
                .setSource(this, R.raw.video_screen)
                .build()
                .thenAccept(modelRenderable -> {
                    videoRenderable = modelRenderable;
                    videoRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    videoRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1.0f, 0.098f));
                });

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());

            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();

                texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
                    anchorNode.setRenderable(videoRenderable);
                    texture.getSurfaceTexture().setOnFrameAvailableListener(null);
                });
            } else
                anchorNode.setRenderable(videoRenderable);

            float width = mediaPlayer.getVideoWidth()-30;
            float height = mediaPlayer.getVideoHeight()-30;

            anchorNode.setLocalScale(new Vector3(
                HEIGHT * (width/height), HEIGHT, 1.0f
            ));

            arFragment.getArSceneView().getScene().addChild(anchorNode);

        }));






    }
}