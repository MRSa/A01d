package net.osdn.gokigen.a01d.camera.sony.wrapper;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ISonyCameraApi
{
    JSONObject getAvailableApiList();
    JSONObject getApplicationInfo();

    JSONObject getShootMode();
    JSONObject setShootMode(@NonNull String shootMode);
    JSONObject getAvailableShootMode();
    JSONObject getSupportedShootMode();

    JSONObject startLiveview();
    JSONObject stopLiveview();

    JSONObject startRecMode();
    JSONObject actTakePicture();

    JSONObject startMovieRec();
    JSONObject stopMovieRec();

    JSONObject actZoom(@NonNull String direction, @NonNull String movement);

    JSONObject getEvent(@NonNull String version, boolean longPollingFlag);

    JSONObject setCameraFunction(@NonNull String cameraFunction);

    JSONObject getCameraMethodTypes();

    JSONObject getAvcontentMethodTypes();

    JSONObject getSchemeList();
    JSONObject getSourceList(String scheme);

    JSONObject getContentList(JSONArray params);

    JSONObject setStreamingContent(String uri);

    JSONObject startStreaming();
    JSONObject stopStreaming();
}
