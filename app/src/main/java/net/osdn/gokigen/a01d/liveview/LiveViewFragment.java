package net.osdn.gokigen.a01d.liveview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.IInterfaceProvider;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.fujix.cameraproperty.FujiXCameraStatusDialog;
import net.osdn.gokigen.a01d.camera.olympus.myolycameraprops.LoadSaveCameraProperties;
import net.osdn.gokigen.a01d.camera.olympus.myolycameraprops.LoadSaveMyCameraPropertyDialog;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.IFocusingModeNotify;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraProperty;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.OlympusCameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

/**
 *  撮影用ライブビュー画面
 *
 */
public class LiveViewFragment extends Fragment implements IStatusViewDrawer, IFocusingModeNotify, IDialogKicker, ICameraStatusUpdateNotify, IFocusLockIndicator
{
    private final String TAG = this.toString();
    private static final int COMMAND_MY_PROPERTY = 0x00000100;

    private ILiveViewControl liveViewControl = null;
    private IZoomLensControl zoomLensControl = null;
    private IInterfaceProvider interfaceProvider = null;
    private IDisplayInjector interfaceInjector = null;
    private OlympusCameraLiveViewListenerImpl liveViewListener = null;
    private IChangeScene changeScene = null;
    private ICameraInformation cameraInformation = null;
    private LiveViewClickTouchListener onClickTouchListener = null;

    private TextView statusArea = null;
    private TextView focalLengthArea = null;
    private CameraLiveImageView imageView = null;

    private ImageView manualFocus = null;
    private ImageView focusIndicator = null;
    private ImageButton showGrid = null;
    private ImageButton connectStatus = null;
    private Button changeLiveViewScale = null;

    private boolean imageViewCreated = false;
    private View myView = null;
    private String messageValue = "";
    private boolean focusLocked = false;

    private ICameraConnection.CameraConnectionStatus currentConnectionStatus =  ICameraConnection.CameraConnectionStatus.UNKNOWN;

    public static LiveViewFragment newInstance(IChangeScene sceneSelector, @NonNull IInterfaceProvider provider)
    {
        LiveViewFragment instance = new LiveViewFragment();
        instance.prepare(sceneSelector, provider);

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
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        if (liveViewListener == null)
        {
            liveViewListener = new OlympusCameraLiveViewListenerImpl();
        }
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

    /**
     *
     *
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.v(TAG, "onCreateView()");
        if ((imageViewCreated)&&(myView != null))
        {
            // Viewを再利用。。。
            Log.v(TAG, "onCreateView() : called again, so do nothing... : " + myView);
            return (myView);
        }

        View view = inflater.inflate(R.layout.fragment_live_view, container, false);
        myView = view;
        imageViewCreated = true;
        try
        {
            imageView = view.findViewById(R.id.cameraLiveImageView);
            if (interfaceInjector != null)
            {
                interfaceInjector.injectDisplay(imageView, imageView, this);
            }
            else
            {
                Log.v(TAG, "interfaceInjector is NULL...");
            }
            if (onClickTouchListener == null)
            {
                onClickTouchListener = new LiveViewClickTouchListener(this.getContext(), imageView, this, changeScene, interfaceProvider, this);
            }
            imageView.setOnClickListener(onClickTouchListener);
            imageView.setOnTouchListener(onClickTouchListener);
            imageView.setFocuslockIndicator(this);

            view.findViewById(R.id.show_preference_button).setOnClickListener(onClickTouchListener);
            view.findViewById(R.id.camera_property_settings_button).setOnClickListener(onClickTouchListener);
            view.findViewById(R.id.shutter_button).setOnClickListener(onClickTouchListener);
            view.findViewById(R.id.btn_zoomin).setOnClickListener(onClickTouchListener);
            view.findViewById(R.id.btn_zoomout).setOnClickListener(onClickTouchListener);

            focusIndicator =  view.findViewById(R.id.focus_indicator);
            if (focusIndicator != null)
            {
                focusIndicator.setOnClickListener(onClickTouchListener);
            }
            manualFocus = view.findViewById(R.id.focusing_button);
            changeLiveViewScale = view.findViewById(R.id.live_view_scale_button);

            ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();

            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                view.findViewById(R.id.show_favorite_settings_button).setOnClickListener(onClickTouchListener);

                // OPCのときには、フォーカスインジケータのマークを消す。
                if (focusIndicator != null)
                {
                    focusIndicator.setVisibility(View.INVISIBLE);
                }
            }
            else
            {
                // お気に入りボタン(とMFボタン)は、SONYモード, RICOH GR2モードのときには表示しない
                final View favoriteButton = view.findViewById(R.id.show_favorite_settings_button);
                final View propertyButton = view.findViewById(R.id.camera_property_settings_button);
                if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
                {
                    if ((favoriteButton != null)&&(manualFocus != null))
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                favoriteButton.setVisibility(View.INVISIBLE);
                                if (manualFocus != null)
                                {
                                    manualFocus.setVisibility(View.INVISIBLE);
                                }
                                propertyButton.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (changeLiveViewScale != null)
                    {
                        changeLiveViewScale.setVisibility(View.INVISIBLE);
                    }
                    if (focusIndicator != null)
                    {
                        focusIndicator.setVisibility(View.VISIBLE);
                    }
                }
                else if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
                {
                    if ((favoriteButton != null)&&(manualFocus != null))
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                favoriteButton.setVisibility(View.INVISIBLE);
                                if (manualFocus != null)
                                {
                                    manualFocus.setVisibility(View.INVISIBLE);
                                }
                                propertyButton.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (changeLiveViewScale != null)
                    {
                        changeLiveViewScale.setVisibility(View.VISIBLE);
                    }
                    if (focusIndicator != null)
                    {
                        focusIndicator.setVisibility(View.INVISIBLE);
                    }
                }
                else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
                {
                    if (favoriteButton != null)
                    {
                        favoriteButton.setVisibility(View.VISIBLE);
                        favoriteButton.setOnClickListener(onClickTouchListener);
                    }
                    if (manualFocus != null)
                    {
                        manualFocus.setVisibility(View.INVISIBLE);
                    }
                    if (changeLiveViewScale != null)
                    {
                        changeLiveViewScale.setVisibility(View.INVISIBLE);
                    }
                    if (focusIndicator != null)
                    {
                        focusIndicator.setVisibility(View.VISIBLE);
                    }
                    if (propertyButton != null)
                    {
                        propertyButton.setOnClickListener(onClickTouchListener);
                    }
                }
            }
            if (manualFocus != null)
            {
                manualFocus.setOnClickListener(onClickTouchListener);
            }
            changedFocusingMode();

            if (changeLiveViewScale != null)
            {
                changeLiveViewScale.setOnClickListener(onClickTouchListener);
            }

            showGrid = view.findViewById(R.id.show_hide_grid_button);
            showGrid.setOnClickListener(onClickTouchListener);
            updateGridIcon();

            connectStatus = view.findViewById(R.id.connect_disconnect_button);
            connectStatus.setOnClickListener(onClickTouchListener);
            updateConnectionStatus(ICameraConnection.CameraConnectionStatus.UNKNOWN);

            statusArea = view.findViewById(R.id.informationMessageTextView);
            focalLengthArea = view.findViewById(R.id.focal_length_with_digital_zoom_view);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return (view);
    }

    /**
     *
     */
    private void prepare(IChangeScene sceneSelector, IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");

        IDisplayInjector interfaceInjector;
        ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();
        if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
        {
            interfaceInjector = interfaceProvider.getRicohGr2Infterface().getDisplayInjector();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
        {
            interfaceInjector = interfaceProvider.getSonyInterface().getDisplayInjector();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
        {
            interfaceInjector = interfaceProvider.getFujiXInterface().getDisplayInjector();
        }
        else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
        {
            interfaceInjector = interfaceProvider.getOlympusInterface().getDisplayInjector();
        }
        this.changeScene = sceneSelector;
        this.interfaceProvider = interfaceProvider;
        this.interfaceInjector = interfaceInjector;

        if  (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
        {
            this.liveViewControl = interfaceProvider.getRicohGr2Infterface().getLiveViewControl();
            this.zoomLensControl = interfaceProvider.getRicohGr2Infterface().getZoomLensControl();
            this.cameraInformation = interfaceProvider.getRicohGr2Infterface().getCameraInformation();
        }
        else  if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
        {
            this.liveViewControl = interfaceProvider.getSonyInterface().getSonyLiveViewControl();
            this.zoomLensControl = interfaceProvider.getSonyInterface().getZoomLensControl();
            this.cameraInformation = interfaceProvider.getSonyInterface().getCameraInformation();
        }
        else  if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
        {
            this.liveViewControl = interfaceProvider.getFujiXInterface().getLiveViewControl();
            this.zoomLensControl = interfaceProvider.getFujiXInterface().getZoomLensControl();
            this.cameraInformation = interfaceProvider.getFujiXInterface().getCameraInformation();
        }
        else //  if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
        {
            this.liveViewControl = interfaceProvider.getOlympusInterface().getLiveViewControl();
            this.zoomLensControl = interfaceProvider.getOlympusInterface().getZoomLensControl();
            this.cameraInformation = interfaceProvider.getOlympusInterface().getCameraInformation();
        }
        interfaceProvider.setUpdateReceiver(this);
    }

    /**
     *  カメラとの接続状態の更新
     *
     */
    @Override
    public void updateConnectionStatus(ICameraConnection.CameraConnectionStatus connectionStatus)
    {
        try
        {
            currentConnectionStatus = connectionStatus;
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    int id = R.drawable.ic_cloud_off_black_24dp;
                    if (currentConnectionStatus == ICameraConnection.CameraConnectionStatus.CONNECTING)
                    {
                        id = R.drawable.ic_cloud_queue_black_24dp;
                    }
                    else if  (currentConnectionStatus == ICameraConnection.CameraConnectionStatus.CONNECTED)
                    {
                        id = R.drawable.ic_cloud_done_black_24dp;
                    }
                    connectStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), id, null));
                    connectStatus.invalidate();
                    imageView.invalidate();
                }
            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  グリッドの表示・非表示の更新
     *
     */
    @Override
    public void updateGridIcon()
    {
        try
        {
            int id = (imageView.isShowGrid()) ? R.drawable.ic_grid_off_black_24dp : R.drawable.ic_grid_on_black_24dp;
            showGrid.setImageDrawable(ResourcesCompat.getDrawable(getResources(), id, null));
            showGrid.invalidate();
            imageView.invalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   AF/MFの表示を更新する
     *
     */
    @Override
    public void changedFocusingMode()
    {
        try
        {
            if ((cameraInformation == null)||(manualFocus == null))
            {
                return;
            }
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (currentConnectionStatus == ICameraConnection.CameraConnectionStatus.CONNECTED)
                    {
                        manualFocus.setSelected(cameraInformation.isManualFocus());
                        manualFocus.invalidate();
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLiveViewScale(boolean isChangeScale)
    {
        try
        {
            Log.v(TAG, "updateLiveViewScale() : " + isChangeScale);

            // ライブビューの倍率設定
            liveViewControl.updateMagnifyingLiveViewScale(isChangeScale);

            // ボタンの文字を更新する
            float scale = liveViewControl.getMagnifyingLiveViewScale();
            final String datavalue = "LV: " + scale;

            // デジタルズームの倍率を表示する
            float digitalZoom = liveViewControl.getDigitalZoomScale();
            final String digitalValue = (digitalZoom > 1.0f) ? "D x" + digitalZoom : "";

            // 更新自体は、UIスレッドで行う
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    changeLiveViewScale.setText(datavalue);
                    changeLiveViewScale.postInvalidate();

                    focalLengthArea.setText(digitalValue);
                    focalLengthArea.postInvalidate();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     *
     */
    @Override
    public void onStart()
    {
        super.onStart();
        Log.v(TAG, "onStart()");
    }

    /**
     *
     *
     */
    @Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume() Start");
/*
        // 撮影モードかどうかを確認して、撮影モードではなかったら撮影モードに切り替える
        if ((changeRunModeExecutor != null)&&(!changeRunModeExecutor.isRecordingMode()))
        {
            // Runモードを切り替える。（でも切り替えると、設定がクリアされてしまう...。
            changeRunModeExecutor.changeRunMode(true);
        }

        // ステータスの変更を通知してもらう
        camera.setCameraStatusListener(statusListener);

        // 画面下部の表示エリアの用途を切り替える
        setupLowerDisplayArea();
*/
        // propertyを取得
        try
        {
            Context context = getContext();
            if (context != null)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                // グリッド・フォーカスアシストの情報を戻す
                boolean showGrid = preferences.getBoolean(IPreferencePropertyAccessor.SHOW_GRID_STATUS, false);
                if ((imageView != null) && (imageView.isShowGrid() != showGrid)) {
                    imageView.toggleShowGridFrame();
                    imageView.postInvalidate();
                }
            }
            if (currentConnectionStatus == ICameraConnection.CameraConnectionStatus.CONNECTED)
            {
                startLiveView();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "onResume() End");
    }

    /**
     *
     *
     */
    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "onPause() Start");

        // ライブビューの停止
        try
        {
            liveViewControl.stopLiveView();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "onPause() End");
    }

    /**
     *   表示エリアに文字を表示する
     *
     */
    @Override
    public void updateStatusView(String message)
    {
        messageValue = message;
        runOnUiThread(new Runnable()
        {
            /**
             * カメラの状態(ステータステキスト）を更新する
             * (ステータステキストは、プライベート変数で保持して、書き換える)
             */
            @Override
            public void run()
            {
                if (statusArea != null)
                {
                    statusArea.setText(messageValue);
                    statusArea.invalidate();
                }
            }
        });
    }

    /**
     *   ライブビューの開始
     *
     */
    @Override
    public void startLiveView()
    {
        ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();

        if (liveViewControl == null)
        {
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                Log.v(TAG, "startLiveView() : liveViewControl is null.");
                return;
            }
            else
            {
                // ダミー
                prepare(changeScene, interfaceProvider);
            }
        }
        try
        {
            // ライブビューの開始
            Context context = getContext();
            if (context != null)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                liveViewControl.changeLiveViewSize(preferences.getString(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY, IPreferencePropertyAccessor.LIVE_VIEW_QUALITY_DEFAULT_VALUE));
            }
            ILiveViewListener lvListener;
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
            {
                lvListener = interfaceProvider.getRicohGr2Infterface().getLiveViewListener();
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                lvListener = interfaceProvider.getSonyInterface().getLiveViewListener();
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                lvListener = interfaceProvider.getFujiXInterface().getLiveViewListener();
            }
            else  // if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                interfaceProvider.getOlympusLiveViewListener().setOlympusLiveViewListener(liveViewListener);
                lvListener = liveViewListener;
            }
            lvListener.setCameraLiveImageView(imageView);
            liveViewControl.startLiveView();

            // デジタルズームの設定
            liveViewControl.updateDigitalZoom();

            // ズームが制御できない場合は、ボタンを消す
            if (!zoomLensControl.canZoom())
            {
                final Activity activity  = getActivity();
                if (activity != null)
                {
                    activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            activity.findViewById(R.id.btn_zoomin).setVisibility(View.INVISIBLE);
                            activity.findViewById(R.id.btn_zoomout).setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
            else
            {
                // パワーズームの設定 (初期化位置の設定)
                zoomLensControl.moveInitialZoomPosition();
            }

            // 測光モードをスポットに切り替える (OPCのみ)
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                setAEtoSpot();
            }

            // ライブビューの倍率設定
            updateLiveViewScale(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *   フォーカスロック中かどうか
     *
     */
    @Override
    public boolean isFocusLocked()
    {
        return (this.focusLocked);
    }

    /**
     *    測光モードをスポットに切り替える
     *
     */
    private void setAEtoSpot()
    {
        try
        {
            IOlyCameraPropertyProvider propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();
            if (propertyProvider != null)
            {
                propertyProvider.setCameraPropertyValue(IOlyCameraProperty.AE, IOlyCameraProperty.AE_PINPOINT);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void showFavoriteSettingDialog()
    {
        try
        {
            Log.v(TAG, "showFavoriteSettingDialog()");

            LoadSaveMyCameraPropertyDialog dialog = new LoadSaveMyCameraPropertyDialog();
            dialog.setTargetFragment(this, COMMAND_MY_PROPERTY);
            dialog.setPropertyOperationsHolder(new LoadSaveCameraProperties(getActivity(), interfaceProvider.getOlympusInterface()));
            FragmentManager manager = getFragmentManager();
            if (manager != null)
            {
                dialog.show(manager, "my_dialog");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void showCameraStatusDialog()
    {
        try
        {
            // FUJI X用のステータス表示ダイアログを表示する
            FragmentManager manager = getFragmentManager();
            if (manager != null)
            {
                FujiXCameraStatusDialog.newInstance(interfaceProvider.getFujiXInterface()).show(manager, "statusDialog");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void runOnUiThread(Runnable action)
    {
        Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }
        activity.runOnUiThread(action);
    }

    @Override
    public void updateDriveMode(String driveMode)
    {
        Log.v(TAG, "updateDriveMode() : " + driveMode);
    }

    @Override
    public void updateAeLockState(boolean isAeLocked)
    {
        Log.v(TAG, "updateAeLockState() : " + isAeLocked);
    }

    @Override
    public void updateCameraStatus(String message)
    {
        Log.v(TAG, "updateCameraStatus() : " + message);
    }

    @Override
    public void updateLevelGauge(String orientation, float roll, float pitch)
    {
        Log.v(TAG, "updateLevelGauge() : " + orientation + " roll : " + roll + "  pitch : " + pitch);
    }

    @Override
    public void updatedTakeMode(String mode)
    {
        Log.v(TAG, "updatedTakeMode() : " + mode);
    }

    @Override
    public void updatedShutterSpeed(String tv)
    {
        Log.v(TAG, "updatedShutterSpeed() : " + tv);
    }

    @Override
    public void updatedAperture(String av)
    {
        Log.v(TAG, "updatedAperture() : " + av);
    }

    @Override
    public void updatedExposureCompensation(String xv)
    {
        Log.v(TAG, "updatedExposureCompensation() : " + xv);
    }

    @Override
    public void updatedMeteringMode(String meteringMode)
    {
        Log.v(TAG, "updatedExposureCompensation() : " + meteringMode);
    }

    @Override
    public void updatedWBMode(String wbMode)
    {
        Log.v(TAG, "updatedWBMode() : " + wbMode);
    }

    @Override
    public void updateRemainBattery(int percentage)
    {
        Log.v(TAG, "updateRemainBattery() : " + percentage);
    }

    @Override
    public void updateFocusedStatus(final boolean focused, final boolean focusLocked)
    {
        Log.v(TAG, "updateFocusedStatus() : f: " + focused + " fl: " + focusLocked);
        this.focusLocked = focusLocked;
        Activity activity = getActivity();
        if ((activity == null)||(focusIndicator == null)|| (focusIndicator.getVisibility() != View.VISIBLE))
        {
            Log.v(TAG, "updateFocusedStatus() : INVISIBLE");
            return;
        }

        try
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (focused)
                    {
                        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_center_focus_strong_black_24dp, null);
                        if (icon != null)
                        {
                            DrawableCompat.setTint(icon, Color.GREEN);
                            focusIndicator.setImageDrawable(icon);
                        }
                    }
                    else
                    {
                        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_crop_free_black_24dp, null);
                        if (icon != null)
                        {
                            int color = Color.BLACK;
                            if (focusLocked)
                            {
                                color = Color.RED;
                            }
                            DrawableCompat.setTint(icon, color);
                            focusIndicator.setImageDrawable(icon);
                        }
                    }
                    focusIndicator.invalidate();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateIsoSensitivity(String sv)
    {
        Log.v(TAG, "updateIsoSensitivity() : " + sv);
    }

    @Override
    public void updateWarning(String warning)
    {
        Log.v(TAG, "updateWarning() : " + warning);
    }

    @Override
    public void updateStorageStatus(String status)
    {
        Log.v(TAG, "updateStorageStatus() : " + status);
    }

    @Override
    public void updateFocusLockIndicator(final boolean focused, final boolean focusLocked)
    {
        updateFocusedStatus(focused, focusLocked);
    }
}
