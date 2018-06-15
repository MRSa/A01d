package net.osdn.gokigen.a01d.camera.sony.cameraproperty;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.IInterfaceProvider;


public class SonyCameraPropertyListFragment extends Fragment
{
    private final String TAG = toString();


    /**
     *  カメラプロパティをやり取りするインタフェースを生成する
     *
     */
    public static SonyCameraPropertyListFragment newInstance(@NonNull  Context context, @NonNull IInterfaceProvider propertyInterface)
    {
        SonyCameraPropertyListFragment instance = new SonyCameraPropertyListFragment();
        instance.prepare(context, propertyInterface);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(Context context, IInterfaceProvider propertyInterface)
    {
        Log.v(TAG, "prepare()");
    }

    /**
     *
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");

    }

    /**
     *
     *
     */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
    }


}
