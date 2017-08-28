package net.osdn.gokigen.a01d.liveview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.operation.IFocusingControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import java.io.File;


/**
 *  撮影用ライブビュー画面
 *
 */
public class LiveViewFragment extends Fragment implements IStatusViewDrawer
{
    private final String TAG = this.toString();
    private static final int COMMAND_MY_PROPERTY = 0x00000100;

    private ILiveViewControl liveViewControl = null;
    private IOlympusInterfaceProvider interfaceProvider = null;
    private IOlympusDisplayInjector interfaceInjector = null;

//    private IOlyCameraCoordinator camera = null;
//    private MyInterfaceProvider factory = null;
//    private ICameraRunMode changeRunModeExecutor = null;
//    private OlyCameraLiveViewOnTouchListener onTouchListener = null;
    private CameraLiveViewListenerImpl liveViewListener = null;
//    private CameraStatusListenerImpl statusListener = null;
//    private IGpsLocationPicker locationPicker = null;

    private IChangeScene changeScene = null;

    private IFocusingControl focusingControl = null;

    private LiveViewClickTouchListener onClickTouchListener = null;


    private TextView statusArea = null;
    private CameraLiveImageView imageView = null;
    //private CameraControlPanel cameraPanel = null;

/*
    private ImageView manualFocus = null;
    private ImageView afLock = null;
    private ImageView aeLock = null;
    private ImageView focusAssist = null;
*/
    private ImageButton showGrid = null;
    private ImageButton connectStatus = null;

    private boolean imageViewCreated = false;
    private View myView = null;
    private String messageValue = "";

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
            liveViewListener = new CameraLiveViewListenerImpl();
        }


/*
        if (onTouchListener == null)
        {
            onTouchListener = new OlyCameraLiveViewOnTouchListener(getContext().getApplicationContext());
        }
        if (statusListener == null)
        {
            statusListener = new CameraStatusListenerImpl(getContext().getApplicationContext(), this);
        }
        if (locationPicker == null)
        {
            locationPicker = new GpsLocationPicker(getContext().getApplicationContext(), onTouchListener);
        }
*/
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
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
                interfaceInjector.injectOlympusDisplay(imageView, imageView);
            }
            if ((interfaceProvider != null) &&(focusingControl == null))
            {
                focusingControl = interfaceProvider.getFocusingControl();
            }
           if (onClickTouchListener == null)
           {
               onClickTouchListener = new LiveViewClickTouchListener(imageView, this, changeScene, focusingControl);
           }
            imageView.setOnClickListener(onClickTouchListener);
            imageView.setOnTouchListener(onClickTouchListener);

            view.findViewById(R.id.show_preference_button).setOnClickListener(onClickTouchListener);
            view.findViewById(R.id.camera_property_settings_button).setOnClickListener(onClickTouchListener);

            showGrid = view.findViewById(R.id.show_hide_grid_button);
            showGrid.setOnClickListener(onClickTouchListener);
            updateGridIcon();

            connectStatus = view.findViewById(R.id.connect_disconnect_button);
            connectStatus.setOnClickListener(onClickTouchListener);
            updateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus.UNKNOWN);

            statusArea = view.findViewById(R.id.informationMessageTextView);
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }

        /*
        imageView.setOnClickListener(onTouchListener);
        imageView.setOnTouchListener(onTouchListener);
*/
/*
        liveViewListener.setCameraLiveImageView(imageView);
        if (factory != null)
        {
            factory.setAutoFocusFrameDisplay(imageView);
        }
*/
/*
        // 画面下部のスマホカメラ領域
        PhoneCameraView phoneCameraView = (PhoneCameraView) view.findViewById(R.id.phoneCameraView);

        // カメラ画像の大きさを動的に調整（したい）
        //phoneCameraView.getViewTreeObserver().addOnGlobalLayoutListener(phoneCameraView);

        ImageView shutter = (ImageView) view.findViewById(R.id.shutterImageView);
        shutter.setOnClickListener(onTouchListener);

        ImageView config = (ImageView) view.findViewById(R.id.configImageView);
        config.setOnClickListener(onTouchListener);

        ImageView build = (ImageView) view.findViewById(R.id.buildImageView);
        build.setOnClickListener(onTouchListener);

        ImageButton gps = (ImageButton) view.findViewById(R.id.gpsLocationButton);
        if ((locationPicker.prepare(camera.getCameraPropertyProvider()))&&(locationPicker.hasGps()))
        {
            // GPSボタンの状態を更新しておく
            updateGpsTrackingStatus();

            // GPSが使用可能な状態のとき...ボタンを押せるようにする
            gps.setOnClickListener(onTouchListener);
        }
        else
        {
            // GPSが利用不可のとき、、、ボタンは無効(非表示)にする
            gps.setEnabled(false);
            gps.setVisibility(View.INVISIBLE);
        }

        manualFocus = (ImageView) view.findViewById(R.id.manualFocusImageView);
        manualFocus.setOnClickListener(onTouchListener);

        afLock = (ImageView) view.findViewById(R.id.AutoFocusLockImageView);
        afLock.setOnClickListener(onTouchListener);

        aeLock = (ImageView) view.findViewById(R.id.AutoExposureLockImageView);
        aeLock.setOnClickListener(onTouchListener);

        focusAssist = (ImageView) view.findViewById(R.id.FocusAssistImageView);
        focusAssist.setOnClickListener(onTouchListener);
*/




       return (view);
    }

    /**
     *
     */
    public void prepare(IChangeScene sceneSelector, IOlympusInterfaceProvider interfaceProvider, IOlympusDisplayInjector interfaceInjector)
    {
        this.changeScene = sceneSelector;
        this.interfaceProvider = interfaceProvider;
        this.liveViewControl = interfaceProvider.getLiveViewControl();
        this.interfaceInjector = interfaceInjector;
    }

    /**
     *  カメラとの接続状態の更新
     *
     */
    @Override
    public void updateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus connectionStatus)
    {
        try
        {
            int id = R.drawable.ic_cloud_off_black_24dp;
            if (connectionStatus == IOlyCameraConnection.CameraConnectionStatus.CONNECTING)
            {
                id = R.drawable.ic_cloud_queue_black_24dp;
            }
            else if  (connectionStatus == IOlyCameraConnection.CameraConnectionStatus.CONNECTED)
            {
                id = R.drawable.ic_cloud_done_black_24dp;
            }
            connectStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), id, null));
            connectStatus.invalidate();
            imageView.invalidate();
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
     *   作例表示モードの画像のURIを応答する
     *
     * @return Uri : 作例表示する画像のURI
     */
    private Uri isSetupSampleImageFile(String fileName)
    {
        try
        {
            File file = new File(fileName);
            if (file.exists())
            {
                Log.v(TAG, "isSetupSampleImageFile() : " + file.toString());
                return (Uri.fromFile(file));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "isSetupSampleImageFile() : nothing");
        return (null);
    }

    /**
     *   画面下部の表示エリアの用途を切り替える
     *
     */
    private void setupLowerDisplayArea()
    {
/*
        ScalableImageViewPanel sampleImageView = (ScalableImageViewPanel) getActivity().findViewById(R.id.favoriteImageView);
        PhoneCameraView phoneCameraView = (PhoneCameraView) getActivity().findViewById(R.id.phoneCameraView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String value = preferences.getString(IPreferencePropertyAccessor.SHOW_SAMPLE_IMAGE, IPreferencePropertyAccessor.SHOW_SAMPLE_IMAGE_DEFAULT_VALUE);
        if (value.equals("2"))
        {
            // 操作パネル表示モード
            try
            {
                phoneCameraView.setVisibility(View.GONE);
                sampleImageView.setVisibility(View.VISIBLE);
                CameraControlPanel cameraPanel = new CameraControlPanel(sampleImageView, camera.getCameraPropertyProvider());
                statusListener.setDelegateListener(cameraPanel);
                sampleImageView.setOnClickListener(cameraPanel);
                sampleImageView.setOnTouchListener(cameraPanel);
                sampleImageView.setOnLongClickListener(cameraPanel);
                sampleImageView.setCameraPanelDrawer(true, cameraPanel);
                sampleImageView.invalidate();
                return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        statusListener.setDelegateListener(null);
        sampleImageView.setOnClickListener(null);
        sampleImageView.setOnTouchListener(null);
        sampleImageView.setOnLongClickListener(null);
        sampleImageView.setCameraPanelDrawer(false, null);

        Uri uri = null;
        if (value.equals("1"))
        {
            // 作例表示用の画像を取得
            uri = isSetupSampleImageFile(preferences.getString(IPreferencePropertyAccessor.SELECT_SAMPLE_IMAGE, ""));
        }
        if (uri != null)
        {
            // 作例表示モード
            phoneCameraView.setVisibility(View.GONE);
            sampleImageView.setVisibility(View.VISIBLE);
            sampleImageView.setImageURI(uri);
            sampleImageView.invalidate();
        }
        else
        {
            // デュアルカメラモード
            phoneCameraView.setVisibility(View.VISIBLE);
            sampleImageView.setVisibility(View.GONE);

            // カメラの画像にタッチリスナを付与
            phoneCameraView.setOnClickListener(onTouchListener);
            phoneCameraView.setOnTouchListener(onTouchListener);
        }
*/
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        try
        {
            // グリッド・フォーカスアシストの情報を戻す
            boolean showGrid = preferences.getBoolean(IPreferencePropertyAccessor.SHOW_GRID_STATUS, false);
            if ((imageView != null)&&(imageView.isShowGrid() != showGrid))
            {
                imageView.toggleShowGridFrame();
                imageView.postInvalidate();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /*
        imageView.setFocusAssist(preferences.getBoolean(IPreferencePropertyAccessor.SHOW_FOCUS_ASSIST_STATUS, false));
        updateCameraPropertyStatus();

        // ステータスの初期情報を表示する
        updateStatusView(camera.getCameraStatusSummary(statusListener));

        // ライブビューの開始
        camera.changeLiveViewSize(preferences.getString(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY, IPreferencePropertyAccessor.LIVE_VIEW_QUALITY_DEFAULT_VALUE));
        camera.setLiveViewListener(liveViewListener);
        liveViewListener.setCameraLiveImageView(imageView);
        camera.startLiveView();

        // GPSボタンの更新
        updateGpsTrackingStatus();

        // デジタル水準器を有効にするかどうか
        if (statusListener != null)
        {
            statusListener.updateLevelGaugeChecking();
        }
*/
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

/*
        // ライブビューの停止
        camera.stopLiveView();
        camera.setLiveViewListener(null);
        liveViewListener.setCameraLiveImageView(null);

        if (locationPicker != null)
        {
            // GPS監視の終了
            locationPicker.controlGps(false);
            camera.clearGeolocation();
        }
*/
        Log.v(TAG, "onPause() End");
    }

    /**
     * カメラクラスをセットする
     *
     */
/*
    public void setInterfaces(IOlyCameraCoordinator camera, MyInterfaceProvider factory)
    {
        Log.v(TAG, "setInterfaces()");
        this.camera = camera;
        this.factory = factory;
        this.changeRunModeExecutor = camera.getChangeRunModeExecutor();

        factory.setStatusInterface(this);
        factory.setStatusViewDrawer(this);
        //if (imageView != null)
        {
        //    factory.setAutoFocusFrameDisplay(imageView);
        }
    }
*/
/*
    @Override
    public void updateFocusAssistStatus()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateGridFrameStatus()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateTakeMode()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateDriveMode()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateWhiteBalance()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateBatteryLevel()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateAeMode()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateAeLockState()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateCameraStatus()
    {
        updateCameraPropertyStatus();
    }

    @Override
    public void updateCameraStatus(String message)
    {
        updateStatusView(message);
    }
*/
/*
    @Override
    public void updateLevelGauge(String orientation, float roll, float pitch)
    {
        if (imageView == null)
        {
            return;
        }

        // レベルゲージ(デジタル水準器の情報)が更新されたとき
        //Log.v(TAG, String.format(Locale.getDefault(), "LEVEL GAUGE : %s roll: %3.3f pitch: %3.3f", orientation, roll, pitch));
        try
        {
            if ((Float.isNaN(roll))||(Float.isNaN(pitch)))
            {
                // roll と pitch のどちらかがNaNなら、表示を消す
                imageView.getMessageDrawer().setMessageToShow(IMessageDrawer.MessageArea.LOWRIGHT, Color.argb(0xff, 0x6e, 0x6e, 0x6e), IMessageDrawer.SIZE_STD, "");
                imageView.getMessageDrawer().setLevelToShow(IMessageDrawer.LevelArea.LEVEL_HORIZONTAL, Float.NaN);
                imageView.getMessageDrawer().setLevelToShow(IMessageDrawer.LevelArea.LEVEL_VERTICAL, Float.NaN);
                return;
            }

            // 傾きのデータを設定する
            String message = String.format(Locale.getDefault(), "[%3.1f, %3.1f]", roll, pitch);
            imageView.getMessageDrawer().setMessageToShow(IMessageDrawer.MessageArea.LOWRIGHT, Color.argb(0xff, 0x6e, 0x6e, 0x6e), IMessageDrawer.SIZE_STD, message);
            imageView.getMessageDrawer().setLevelToShow(IMessageDrawer.LevelArea.LEVEL_HORIZONTAL, roll);
            imageView.getMessageDrawer().setLevelToShow(IMessageDrawer.LevelArea.LEVEL_VERTICAL, pitch);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/
/*
    @Override
    public void showFavoriteSettingDialog()
    {
        LoadSaveMyCameraPropertyDialog dialog = new LoadSaveMyCameraPropertyDialog();
        dialog.setTargetFragment(this, COMMAND_MY_PROPERTY);
        dialog.setPropertyOperationsHolder(new CameraPropertyLoadSaveOperations(getActivity(), camera.getLoadSaveCameraProperties(), this));
        dialog.show(getChildFragmentManager(), "my_dialog");
    }
*/
/*
    @Override
    public void toggleTimerStatus()
    {
        boolean isBracketing = !isBracketing();
        SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IPreferencePropertyAccessor.USE_BRACKETING, isBracketing);
        editor.apply();
        if (bracketing != null)
        {
            bracketing.setSelected(isBracketing);
        }
    }

    private boolean isBracketing()
    {
        SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isBracketing = false;
        if (preferences != null)
        {
            isBracketing = preferences.getBoolean(IPreferencePropertyAccessor.USE_BRACKETING, false);
        }
        return (isBracketing);
    }
*/
    /**
     *
     *
     */
/*
    private void updateCameraPropertyStatus()
    {
        try
        {
            final boolean isManualFocus = camera.isManualFocus();
            final boolean isAfLock = camera.isAFLock();
            final boolean isAeLock = camera.isAELock();
            final boolean isTimerOn = isBracketing();
            boolean checkFocusAssist = false;
            boolean checkShowGrid = false;
            if (imageView != null)
            {
                checkFocusAssist = imageView.isFocusAssist();
                checkShowGrid = imageView.isShowGrid();
            }
            final boolean isFocusAssist = checkFocusAssist;
            final boolean isShowGrid = checkShowGrid;

            runOnUiThread(new Runnable()
            {
                //カメラの状態(インジケータ)を更新する
                @Override
                public void run() {
                    if (camera == null) {
                        return;
                    }
                    Log.v(TAG, "--- UPDATE CAMERA PROPERTY (START) ---");
                    if (manualFocus != null) {
                        manualFocus.setSelected(isManualFocus);
                    }
                    if (afLock != null) {
                        afLock.setSelected(isAfLock);
                    }
                    if (aeLock != null) {
                        aeLock.setSelected(isAeLock);
                    }
                    if ((focusAssist != null) && (imageView != null)) {
                        focusAssist.setSelected(isFocusAssist);
                    }
                    if ((showGrid != null) && (imageView != null)) {
                        showGrid.setSelected(isShowGrid);
                    }
                    if (bracketing != null)
                    {
                        bracketing.setSelected(isTimerOn);
                    }
                    Log.v(TAG, "--- UPDATE CAMERA PROPERTY (END) ---");
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/

    /**
     *
     *
     */
/*
    @Override
    public IMessageDrawer getMessageDrawer()
    {
        return (imageView.getMessageDrawer());
    }
*/
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
        if (liveViewControl == null)
        {
            Log.v(TAG, "startLiveView() : liveViewControl is null.");
            return;
        }
        try
        {
            // ライブビューの開始
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            liveViewControl.changeLiveViewSize(preferences.getString(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY, IPreferencePropertyAccessor.LIVE_VIEW_QUALITY_DEFAULT_VALUE));
            liveViewControl.setLiveViewListener(liveViewListener);
            liveViewListener.setCameraLiveImageView(imageView);
            liveViewControl.startLiveView();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /*
    @Override
    public void toggleGpsTracking()
    {
        if (locationPicker == null)
        {
            return;
        }
        locationPicker.controlGps(!locationPicker.isTracking());
        updateGpsTrackingStatus();
    }

    @Override
    public void updateGpsTrackingStatus()
    {
        Log.v(TAG, "updateGpsTrackingStatus()");
        if ((myView == null)||(locationPicker == null))
        {
            Log.v(TAG, "updateGpsTrackingStatus() : null");
            return;
        }

        ImageButton gps = (ImageButton) myView.findViewById(R.id.gpsLocationButton);
        int id = R.drawable.btn_location_off;
        if (locationPicker.isTracking())
        {
            if (locationPicker.isFixedLocation())
            {
                // 位置が確定している
                id = R.drawable.btn_location_on;
            }
            else
            {
                // 位置検索中だが未確定...
                id = R.drawable.btn_gps_not_fixed;
            }
        }
        else
        {
            // 位置情報をクリアする
            camera.clearGeolocation();
        }
        try
        {
            // ボタンの表示を変える
            gps.setImageResource(id);
            //gps.setImageDrawable(getContext().getResources().getDrawable(id));
            gps.invalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/

    private void runOnUiThread(Runnable action)
    {
        Activity activity = getActivity();
        if (activity == null)
        {
            return;
        }
        activity.runOnUiThread(action);
    }
}
