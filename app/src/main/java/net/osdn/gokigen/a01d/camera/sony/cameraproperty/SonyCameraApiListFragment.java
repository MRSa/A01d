package net.osdn.gokigen.a01d.camera.sony.cameraproperty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import	android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.IInterfaceProvider;

import java.util.ArrayList;
import java.util.List;


public class SonyCameraApiListFragment extends ListFragment
{
    private final String TAG = toString();
    private ArrayAdapter<String> adapter;
    private List<String> dataItems = new ArrayList<>();
    private IInterfaceProvider interfaceProvider = null;


    /**
     *  カメラプロパティをやり取りするインタフェースを生成する
     *
     */
    public static SonyCameraApiListFragment newInstance(@NonNull IInterfaceProvider interfaceProvider)
    {
        SonyCameraApiListFragment instance = new SonyCameraApiListFragment();
        instance.prepare(interfaceProvider);

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
    private void prepare(@NonNull IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");
        this.interfaceProvider = interfaceProvider;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.api_view, menu);
        String title = getString(R.string.app_name) + " " + getString(R.string.pref_sony_api_list);
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null)
            {
                ActionBar bar = activity.getSupportActionBar();
                if (bar != null)
                {
                    bar.setTitle(title);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_refresh)
        {
            update();
            return (true);
        }
        if (item.getItemId() == R.id.action_share)
        {
            share();
            return (true);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *   API一覧の他アプリへ共有
     *
     */
    private void share()
    {
        if ((dataItems != null)&&(dataItems.size() > 0))
        {
            try
            {
                StringBuilder shareData = new StringBuilder();
                for (String item : dataItems)
                {
                    shareData.append(item);
                    shareData.append("\r\n");
                }
                String title = "; " + getString(R.string.pref_sony_api_list);
                Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, new String(shareData));
                FragmentActivity activity = getActivity();
                if (activity != null)
                {
                    // Intent発行(ACTION_SEND)
                    startActivity(sendIntent);
                    Log.v(TAG, "<<< SEND INTENT >>> : " + title);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     *   表示データの更新
     *
     */
    private void update()
    {
        dataItems.clear();
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.v(TAG, "START GET API LIST");
                dataItems = interfaceProvider.getSonyInterface().getApiCommands();
                Log.v(TAG, "FINISH GET API LIST");
                try
                {
                    final FragmentActivity activity = getActivity();
                    if (activity != null)
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    // 中身があったらクリアする
                                    if (adapter.getCount() > 0)
                                    {
                                        adapter.clear();
                                    }

                                    // リストの内容を更新する
                                    adapter.addAll(dataItems);

                                    // 最下部にカーソルを移したい
                                    ListView view = activity.findViewById(android.R.id.list);
                                    view.setSelection(dataItems.size());

                                    // 更新終了通知
                                    Toast.makeText(getActivity(), getString(R.string.finish_refresh), Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception ee)
                                {
                                    ee.printStackTrace();
                                }
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        try
        {
            // 本当は、ここでダイアログを出したい
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume()");

        update();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "SonyCameraApiListFragment::onCreate()");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG, "SonyCameraApiListFragment::onActivityCreated()");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        adapter = new ArrayAdapter<>(inflater.getContext(), android.R.layout.simple_list_item_1, dataItems);
        setListAdapter(adapter);
        return (super.onCreateView(inflater, container, savedInstanceState));
    }

    @Override
    public void onListItemClick (ListView l, View v, int position, long id)
    {
        try
        {
            ListAdapter listAdapter = l.getAdapter();
            String apiName = (String) listAdapter.getItem(position);
            Log.v(TAG, "onListItemClick() [" + position + "] " + apiName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
