package com.devfest.arcoremovies;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArFragment arFragment;

    private final Map<AugmentedImage, Node> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onNewFrame);
    }

    private void onNewFrame(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage img : updatedAugmentedImages) {
            if (img.getTrackingState() == TrackingState.TRACKING) {
                if (!augmentedImageMap.containsKey(img)) {
                    showMovieData(img);
                }
            }
            else if (img.getTrackingState() == TrackingState.STOPPED) {
                augmentedImageMap.remove(img);
            }
        }
    }

    private void showMovieData(AugmentedImage img) {

        View movieView;
        LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        movieView = inflater.inflate(R.layout.movie_data, null);

        JsonObject movieJson = getJSONResource(img.getName().replace(".jpg", ".json"), this);

        TextView titleText = movieView.findViewById(R.id.title_txt);
        titleText.setText(movieJson.get("title").getAsString());
        TextView sinopsisText = movieView.findViewById(R.id.sinopsis_txt);
        sinopsisText.setText(movieJson.get("sinopsis").getAsString());
        RatingBar ratingBar = movieView.findViewById(R.id.rating_bar);
        ratingBar.setRating(movieJson.get("score").getAsFloat());

        Anchor anchor = img.createAnchor(img.getCenterPose());
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        Node movieNode = new Node();
        movieNode.setParent(anchorNode);
        movieNode.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));
        movieNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), 270));

        augmentedImageMap.put(img, movieNode);

        ViewRenderable.builder().setView(this, movieView)
                .build()
                .thenAccept(renderable -> {
                    movieNode.setRenderable(renderable);
                });

    }

    public JsonObject getJSONResource(String filename, Context context) {
        try (InputStream is = context.getAssets().open(filename)) {
            JsonParser parser = new JsonParser();
            return parser.parse(new InputStreamReader(is)).getAsJsonObject();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

}
