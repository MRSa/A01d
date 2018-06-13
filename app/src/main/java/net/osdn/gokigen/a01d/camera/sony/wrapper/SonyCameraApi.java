package net.osdn.gokigen.a01d.camera.sony.wrapper;

import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.sony.wrapper.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

class SonyCameraApi implements ISonyCameraApi
{
    private static final String TAG = SonyCameraApi.class.getSimpleName();
    private static final boolean FULL_LOG = true;

    // API server device you want to send requests.
    private final ISonyCamera sonyCamera;
    private int requestId;


    public static ISonyCameraApi newInstance(@NonNull ISonyCamera target)
    {
        return (new SonyCameraApi(target));
    }

    private SonyCameraApi(final @NonNull ISonyCamera target)
    {
        sonyCamera = target;
        requestId = 1;
    }

    private String findActionListUrl(String service)
    {
        List<SonyApiService> services = sonyCamera.getApiServices();
        for (SonyApiService apiService : services)
        {
            if (apiService.getName().equals(service))
            {
                return (apiService.getActionUrl());
            }
        }
        Log.v(TAG, "actionUrl not found. service : " + service);
        return (null);
    }

    private int id()
    {
        requestId++;
        if (requestId == 0)
        {
            requestId++;
        }
        return (requestId);
    }

    private void log(String msg)
    {
        if (FULL_LOG)
        {
            Log.d(TAG, msg);
        }
    }


    private JSONObject communicateJSON(@NonNull String service, @NonNull String method,  @NonNull JSONArray params, @NonNull String version, int timeoutMs)
    {
        try
        {
            JSONObject requestJson = new JSONObject().put("method", method)
                    .put("params", params)
                    .put("id", id())
                    .put("version", version);
            String url = findActionListUrl(service) + "/" + service;
            log("Request:  " + requestJson.toString());
            String responseJson = SimpleHttpClient.httpPost(url, requestJson.toString(), timeoutMs);
            log("Response: " + responseJson);
            return (new JSONObject(responseJson));
        }
        catch (Exception e)
        {
            log("Exception : " + method + " " + version);
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getAvailableApiList()
    {
        try
        {
            return (communicateJSON("camera", "getAvailableApiList", new JSONArray(), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getApplicationInfo()
    {
        try
        {
            return (communicateJSON("camera", "getApplicationInfo", new JSONArray(), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getShootMode()
    {
        try
        {
            return (communicateJSON("camera", "getShootMode", new JSONArray(), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject setShootMode(@NonNull String shootMode)
    {
        try
        {
            return (communicateJSON("camera", "getShootMode", new JSONArray().put(shootMode), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getAvailableShootMode()
    {
        try {
            return (communicateJSON("camera", "getAvailableShootMode", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getSupportedShootMode()
    {
        try {
            return (communicateJSON("camera", "getSupportedShootMode", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject setTouchAFPosition(double Xpos, double Ypos)
    {
        try
        {
            Log.v(TAG, "setTouchAFPosition (" + Xpos + ", " + Ypos + ")");
            return (communicateJSON("camera", "setTouchAFPosition", new JSONArray().put(Xpos).put(Ypos), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getTouchAFPosition()
    {
        try
        {
            return (communicateJSON("camera", "getTouchAFPosition", new JSONArray(), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject cancelTouchAFPosition()
    {
        try
        {
            return (communicateJSON("camera", "cancelTouchAFPosition", new JSONArray(), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject startLiveview()
    {
        try {
            return (communicateJSON("camera", "startLiveview", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject stopLiveview()
    {
        try {
            return (communicateJSON("camera", "stopLiveview", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject startRecMode()
    {
        try {
            return (communicateJSON("camera", "startRecMode", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject actTakePicture()
    {
        try {
            return (communicateJSON("camera", "actTakePicture", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject awaitTakePicture()
    {
        try
        {
            return (communicateJSON("camera", "awaitTakePicture", new JSONArray(), "1.0", -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject startMovieRec()
    {
        try {
            return (communicateJSON("camera", "startMovieRec", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject stopMovieRec()
    {
        try {
            return (communicateJSON("camera", "stopMovieRec", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject actZoom(@NonNull String direction, @NonNull String movement)
    {
        try {
            return (communicateJSON("camera", "actZoom", new JSONArray().put(direction).put(movement), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject getEvent(@NonNull String version, boolean longPollingFlag)
    {
        try {
            int longPollingTimeout = (longPollingFlag) ? 20000 : 8000; // msec
            return (communicateJSON("camera", "getEvent", new JSONArray().put(longPollingFlag), version, longPollingTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject setCameraFunction(@NonNull String cameraFunction)
    {
        try {
            return (communicateJSON("camera", "setCameraFunction", new JSONArray().put(cameraFunction), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject getCameraMethodTypes()
    {
        try {
            return (communicateJSON("camera", "getCameraMethodTypes", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject getAvcontentMethodTypes()
    {
        try {
            return (communicateJSON("avContent", "getMethodTypes", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getSchemeList()
    {
        try {
            return (communicateJSON("avContent", "getSchemeList", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }


    @Override
    public JSONObject getSourceList(String scheme)
    {
        try {
            JSONObject params = new JSONObject().put("scheme", scheme);
            return (communicateJSON("avContent", "getSourceList", new JSONArray().put(0, params), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());
    }

    @Override
    public JSONObject getContentList(JSONArray params)
    {
        try {
            return (communicateJSON("avContent", "getContentList", new JSONArray().put(0, params), "1.3", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());

    }

    @Override
    public JSONObject setStreamingContent(String uri)
    {
        try {
            JSONObject params = new JSONObject().put("remotePlayType", "simpleStreaming").put("uri", uri);
            return (communicateJSON("avContent", "setStreamingContent", new JSONArray().put(0, params), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());

    }

    @Override
    public JSONObject startStreaming()
    {
        try {
            return (communicateJSON("avContent", "startStreaming", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());

    }

    @Override
    public JSONObject stopStreaming()
    {
        try {
            return (communicateJSON("avContent", "stopStreaming", new JSONArray(), "1.0", -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (new JSONObject());

    }

    public static boolean isErrorReply(JSONObject replyJson)
    {
        return ((replyJson != null && replyJson.has("error")));
    }
}
