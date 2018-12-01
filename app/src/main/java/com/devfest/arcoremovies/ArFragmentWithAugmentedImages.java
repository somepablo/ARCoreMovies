package com.devfest.arcoremovies;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;

public class ArFragmentWithAugmentedImages extends ArFragment {

    private static final String TAG = ArFragmentWithAugmentedImages.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);

        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {

        Config config = new Config(session);

        try {
            AugmentedImageDatabase augmentedImageDatabase = AugmentedImageDatabase.deserialize(
                    session, getActivity().getAssets().open("movies.imgdb"));
            config.setAugmentedImageDatabase(augmentedImageDatabase);

        } catch (IOException e) {
            Log.e(TAG, "Cannot open movies.imgdb");
        }

        return config;
    }

}
