package com.android.internetradio.activities;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internetradio.R;
import com.android.internetradio.adapters.CategoryFmsRvAdapter;
import com.android.internetradio.angmarch.views.NiceSpinner;
import com.android.internetradio.dialogs.LoadingDialog;
import com.android.internetradio.helpers.FMMetaDataProvider;
import com.android.internetradio.helpers.MediaPlayerProvider;
import com.android.internetradio.models.FmCategory;
import com.android.internetradio.models.FmChannel;
import com.android.internetradio.models.FmModel;
import com.android.internetradio.models.FmResponse;
import com.android.internetradio.models.FmSavedStation;
import com.android.internetradio.models.FmStation;
import com.android.internetradio.receivers.NetworkStateReceiver;
import com.android.internetradio.services.RadioInternetService;
import com.android.internetradio.utils.CommonUtils;
import com.android.internetradio.utils.FmSharedPref;
import com.android.internetradio.vodyasov.amr.AudiostreamMetadataManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public final class InternetRadioActivity extends CoreActivity implements
        MediaPlayerProvider.OnUIInteractionListener,
        FMMetaDataProvider.FMMetaDataListener {
    private static final String TAG = InternetRadioActivity.class.getSimpleName();
    private RecyclerView categoryFmsRv;
    private FmModel fmModel;
    private List<FmStation> fmStationList = new ArrayList<>();
    private List<FmCategory> fmCategoryList = new ArrayList<>();
    private NiceSpinner channelSpinner, categorySpinner;
    private CategoryFmsRvAdapter categoryFmsRvAdapter;
    private TextView stationNameCategoryTv;
    private ImageView stationNameBackIv;
    private MediaPlayerProvider mediaPlayerProvider;
    private TextView artistNameTv, titleNameTv, albumNameTv;
    private ProgressBar progressBarBuffer;
    private AnimationDrawable animationDrawable;
    private ImageView visualizerView;
    private ImageButton playPauseIBtn;
    private FmSavedStation savedFmStation;
    private NetworkStateReceiver mNetworkStateReceiver;
    private Snackbar alertSnackBar;
    private FMMetaDataProvider fmMetaDataProvider;
    // Below params taken for GA event
    private long mInitialPlayStationTime, mNextPlayStationTime;
    private RequestOptions glideOptions;
    ////private RequestOptions glideRequestOptions;
    private FmSharedPref fmSharedPref;
    long startTime = 0L;
    long difference = 0L;
    //private GAModel gaModel;
    private String lastTrackFM = "";
    private Handler playHandler;

    private boolean isPlayerServiceAlreadyRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPlayerServiceAlreadyRunning = CommonUtils.isMyServiceRunning(this,
                RadioInternetService.class.getName());
        Log.d(TAG, "isPlayerServiceAlreadyRunning: " + isPlayerServiceAlreadyRunning);

        setContentView(R.layout.activity_player);
        glideOptions = new RequestOptions()
                .placeholder(R.drawable.ic_radio)
                .error(R.drawable.ic_radio);
                /*.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH);*/

        playHandler = new Handler();

        fmSharedPref = new FmSharedPref(dbApplication);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();

        /*// Enable the Up button
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);*/

        initUI();

    }

    private LoadingDialog showTransLoader() {
        LoadingDialog transLoader = new LoadingDialog(this);
        transLoader.show();
        return transLoader;
    }

    private void initUI() {
        mediaPlayerProvider = new MediaPlayerProvider(this);
        mediaPlayerProvider.init();

        initUIComponent();
        processStationsRv();
        processUIComponent();
        mNetworkStateReceiver = new NetworkStateReceiver();
        fmMetaDataProvider = new FMMetaDataProvider(this);

        getFMDataFromServer();
    }

    private void initUIComponent() {
        progressBarBuffer = findViewById(R.id.progress_buffering);

        artistNameTv = findViewById(R.id.tv_artist_name);
        titleNameTv = findViewById(R.id.tv_title_name);
        albumNameTv = findViewById(R.id.tv_album_name);
        visualizerView = findViewById(R.id.iv_visual);
        playPauseIBtn = findViewById(R.id.ibtn_play_pause);
        stationNameBackIv = findViewById(R.id.iv_staion_bg);
        stationNameCategoryTv = findViewById(R.id.tv_station_name_category);
        categoryFmsRv = findViewById(R.id.rv_category_fms);

        channelSpinner = findViewById(R.id.spinner_channel);
        categorySpinner = findViewById(R.id.spinner_category);
    }

    /**
     * Process Horizontal Recycler View for Stations List
     */
    private void processStationsRv() {
        categoryFmsRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryFmsRvAdapter = new CategoryFmsRvAdapter(this, fmStationList);
        categoryFmsRvAdapter.setHasStableIds(true);
        categoryFmsRv.setAdapter(categoryFmsRvAdapter);
    }

    /**
     * Process multiple UI components
     */
    private void processUIComponent() {
        /** Make an visualizer animation for Visualizer view  **/
        animationDrawable = (AnimationDrawable)
                ContextCompat.getDrawable(this, R.drawable.ic_equalizer);
        visualizerView.setImageDrawable(animationDrawable);

        channelSpinner.setBackgroundResource(R.drawable.selector_nice_spinner);
        playPauseIBtn.setOnClickListener(clickListener);

        titleNameTv.setSelected(true);
        artistNameTv.setSelected(true);
        albumNameTv.setSelected(true);
    }

    /**
     * Get FM Model with THResponse from Server
     */
    private void getFMDataFromServer() {

        String radioJson = CommonUtils.readJSONFromAssetFile(dbApplication,"radio.json");

        FmResponse fmResponse = new GsonBuilder().create().fromJson(radioJson,FmResponse.class);
        fmModel = fmResponse.getFmModel();
        feedDataOnViews();
    }

    /**
     * Set Data on Views after getting FM data from server
     */
    private void feedDataOnViews() {
        if (fmModel != null) {
            savedFmStation = fmSharedPref.getCurrentStation();

            List<FmChannel> fmChannels = fmModel.getFmChannels();
            channelSpinner.attachDataSource(fmChannels);

            channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    savedFmStation.setFmChannelId(fmModel.getFmChannels().get(position).getFmChannelId());
                    savedFmStation.setFmChannelName(fmModel.getFmChannels().get(position).getFmChannelName());
                    setCategoryForSelectedChannel();

                    if (savedFmStation == null) {
                        playFmStationForCategory(0);
                    } else {
                        int selectedIndex = 0;
                        for (int index = 0; index < fmCategoryList.size(); index++) {
                            if (fmCategoryList.get(index).getFmCategoryId() == savedFmStation.getFmCategoryId()) {
                                selectedIndex = index;
                                break;
                            }
                        }
                        playFmStationForCategory(selectedIndex);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playFmStationForCategory(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            /***
             * Play Station from saved fm station when found on Shared Pref, Otherwise play Station with Channel index 0, Category index 0, Station index 0.
             */
            if (savedFmStation == null)
                playDefaultFMOnLaunch();
            else {
                playLastPlayedFM();
            }
        }
    }

    /**
     * Play Station with Channel index 0, Category index 0, Station index 0.
     */
    private void playDefaultFMOnLaunch() {
        if (fmModel != null) {
            savedFmStation = new FmSavedStation();
            savedFmStation.setFmChannelId(fmModel.getFmChannels().get(0).getFmChannelId());
            savedFmStation.setFmChannelName(fmModel.getFmChannels().get(0).getFmChannelName());
            setCategoryForSelectedChannel();
            playFmStationForCategory(0);
        }
    }

    /**
     * Play Station from saved fm station.
     */
    private void playLastPlayedFM() {
        if (fmModel != null) {
            int selectedChannelIndex = 0;

            List<FmChannel> fmChannelList = fmModel.getFmChannels();

            for (int index = 0; index < fmChannelList.size(); index++) {
                FmChannel fmChannel = fmChannelList.get(index);
                if (fmChannel.getFmChannelId() == savedFmStation.getFmChannelId()) {
                    selectedChannelIndex = index;
                    break;
                }
            }

            channelSpinner.setSelectedIndex(selectedChannelIndex);

            setCategoryForSelectedChannel();

            int selectedCategoryIndex = 0;
            for (int i = 0; i < fmCategoryList.size(); i++) {
                if (fmCategoryList.get(i).getFmCategoryId() == savedFmStation.getFmCategoryId()) {
                    selectedCategoryIndex = i;
                    break;
                }
            }
            playFmStationForCategory(selectedCategoryIndex);
        }
    }

    /**
     * Set Category Listing for selected channel
     */
    private void setCategoryForSelectedChannel() {
        fmCategoryList.clear();

        List<FmCategory> fmCategories = fmModel.getFmCategories();
        for (FmCategory fmCategory : fmCategories) {
            if (fmCategory.getFmChannelId() == savedFmStation.getFmChannelId()) {
                fmCategoryList.add(fmCategory);

            }
        }
        categorySpinner.attachDataSource(fmCategoryList);
    }

    /**
     * Play Station from selected category index
     *
     * @param position
     */
    public void playFmStationForCategory(int position) {
        categorySpinner.setSelectedIndex(position);
        savedFmStation.setFmCategoryId(fmCategoryList.get(position).getFmCategoryId());
        savedFmStation.setFmCategoryName(fmCategoryList.get(position).getFmCatagoryName());

        List<FmStation> stations = fmModel.getFmStation();

        int size = fmStationList.size();
        fmStationList.clear();
        categoryFmsRvAdapter.notifyItemRangeRemoved(0, size);

        for (FmStation station : stations) {
            if (station.getFmCategoryId() == savedFmStation.getFmCategoryId()
                    && station.getFmChannelId() == savedFmStation.getFmChannelId()) {
                fmStationList.add(station);
            }
        }
        FmStation newStation = null;
        if (savedFmStation.getRadioId() == 0) {
            newStation = fmStationList.get(0);
        } else {
            boolean isSavedStationFoundInRvList = false;
            for (FmStation fmStation : fmStationList) {
                if (fmStation.getRadioId() == savedFmStation.getRadioId()) {
                    newStation = fmStation;
                    isSavedStationFoundInRvList = true;
                    break;
                }
            }

            if (!isSavedStationFoundInRvList) {
                newStation = fmStationList.get(0);
            }
        }

        savedFmStation.setFmChannelId(newStation.getFmChannelId());
        savedFmStation.setFmCategoryId(newStation.getFmCategoryId());
        savedFmStation.setFmUrl(newStation.getFmUrl());
        savedFmStation.setFmIconUrl(newStation.getFmIconUrl());
        savedFmStation.setFmName(newStation.getFmName());
        savedFmStation.setRadioId(newStation.getRadioId());

        categoryFmsRvAdapter.notifyItemRangeInserted(0, fmStationList.size());
        categoryFmsRvAdapter.notifyDataSetChanged();
        playFmStation();

        // Track GA Event for listened channel
        if (mInitialPlayStationTime == 0) {
            mInitialPlayStationTime = System.currentTimeMillis();
        } else {
            mNextPlayStationTime = System.currentTimeMillis();
        }
        if (mInitialPlayStationTime != 0 && mNextPlayStationTime != 0) {
            // long seconds = TimeUnit.MILLISECONDS.toSeconds(mNextPlayStationTime - mInitialPlayStationTime);
            // Track GA Event
            /*TrackerUtils.trackEvent(InternetRadioActivity.this, FmConstants.GAEventCategory.RADIO, FmConstants.GAAction.LISTENED_STATION,
                    savedFmStation.getFmName());*/
            // Change initial channel time to next channel time to get difference when channel will change again
            mInitialPlayStationTime = mNextPlayStationTime;
        }
    }

    private LoadingDialog playFmStationTransLoader;

    /**
     * Play FM Station from AnyWhere
     */
    private void playFmStation() {
        Log.d(TAG, "playFmStation() 1");
        playFmStationTransLoader = showTransLoader();
        playFmStationTransLoader.setCancelable(true);
        Log.d(TAG, "playFmStation() 2");
        fmSharedPref.saveCurrentStation(savedFmStation);

        stationNameCategoryTv.setText(savedFmStation.getFmName());

        Glide.with(this).load(savedFmStation.getFmIconUrl()).
                apply(glideOptions).into(stationNameBackIv);

        titleNameTv.setText(savedFmStation.getFmName());
        artistNameTv.setText("");
        albumNameTv.setText("");

        Log.d(TAG, "playFmStation() 3");
        fmMetaDataProvider.init(savedFmStation.getFmUrl());
        Log.d(TAG, "playFmStation() 4");
        // Track GA Event
//        trackEvent(getGaModel(), FmConstants.GAEventCategory.RADIO, GAConstants.Actions.PLAY, savedFmStation.getFmName());

        playHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "playFmStation() 5");
                    mediaPlayerProvider.playFmStation();
                    Log.d(TAG, "playFmStation() 6");
                } catch (Exception e) {
                    e.printStackTrace();
                    CommonUtils.showInfoDialog(InternetRadioActivity.this,
                            getString(R.string.error_play_fm));
                } finally {
                    try {
                        for (FmStation fmStation : fmStationList) {
                            if (fmStation.getRadioId() == savedFmStation.getRadioId()) {
                                fmStation.setPlaying(true);
                            } else {
                                fmStation.setPlaying(false);
                            }
                        }
                        categoryFmsRvAdapter.notifyDataSetChanged();
                        Log.d(TAG, "playFmStation() 7");

                        if (isPlayerServiceAlreadyRunning) {
                            dismissPlayFmStationTransLoader();
                        }
                        isPlayerServiceAlreadyRunning = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1000);

    }

    private void dismissPlayFmStationTransLoader() {
        if (playFmStationTransLoader != null && playFmStationTransLoader.isShowing()) {
            playFmStationTransLoader.dismiss();
        }
    }

    /**
     * Play Fm when User Select station from Horizontal Recycler Station Adapter
     *
     * @param fmStation
     */
    public void playFmStationWithFmCategoryAdaptor(FmStation fmStation) {

        if (fmStation.getFmUrl().equals(savedFmStation.getFmUrl())) {
            return;
        } else {
            savedFmStation.setFmChannelId(fmStation.getFmChannelId());
            savedFmStation.setFmCategoryId(fmStation.getFmCategoryId());
            savedFmStation.setFmUrl(fmStation.getFmUrl());
            savedFmStation.setFmIconUrl(fmStation.getFmIconUrl());
            savedFmStation.setFmName(fmStation.getFmName());
            savedFmStation.setRadioId(fmStation.getRadioId());
            playFmStation();
        }
    }


    /**
     * On Media Player state Changed.
     *
     * @param playerState
     */
    @Override
    public void onPlayStateChanged(int playerState) {
        Log.d(TAG, "onPlayStateChanged(): " + playerState);
        switch (playerState) {
            case PlaybackState.STATE_PLAYING: {

                animationDrawable.start();
                playPauseIBtn.setEnabled(true);
                playPauseIBtn.setSelected(true);
                progressBarBuffer.setVisibility(View.GONE);
                startTime = System.currentTimeMillis();

                Log.d(TAG, "Play");
                if (TextUtils.isEmpty(lastTrackFM)) {
                    lastTrackFM = "";
                }
                if (savedFmStation != null && !TextUtils.isEmpty(savedFmStation.getFmName()) && !lastTrackFM.equals(savedFmStation.getFmName())) {
                    lastTrackFM = savedFmStation.getFmName();
                }
                dismissPlayFmStationTransLoader();
                break;
            }
            case PlaybackState.STATE_BUFFERING: {
                playPauseIBtn.setEnabled(false);
                animationDrawable.stop();
                progressBarBuffer.setVisibility(View.VISIBLE);
                break;
            }
            case PlaybackState.STATE_PAUSED: {
                difference = System.currentTimeMillis() - startTime;

                String cat = "";
                if (fmModel != null) {
                    cat = fmModel.getFmCategories().get(0) + "";
                }

                animationDrawable.stop();
                playPauseIBtn.setEnabled(true);
                playPauseIBtn.setSelected(false);
                progressBarBuffer.setVisibility(View.GONE);

                Log.d(TAG, "paused");
                break;
            }
            case PlaybackState.STATE_ERROR: {
                playPauseIBtn.setEnabled(true);
                animationDrawable.stop();
                playPauseIBtn.setSelected(false);
                progressBarBuffer.setVisibility(View.GONE);
                CommonUtils.showInfoDialog(InternetRadioActivity.this,
                        getString(R.string.error_play_fm));
                break;
            }
            default: {
                playPauseIBtn.setEnabled(true);
                animationDrawable.stop();
                playPauseIBtn.setSelected(false);
                progressBarBuffer.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    public Context getUiContext() {
        return dbApplication;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (null != mediaPlayerProvider)
            mediaPlayerProvider.onProviderStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != mediaPlayerProvider)
            mediaPlayerProvider.onProviderStop();
    }

    /*
     * This method will be used to register network receiver
     */
    private void registerNetworkReceiver() {
        mNetworkStateReceiver.addListener(networkStateReceiverListener);
        registerReceiver(mNetworkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /*
     * This method will be used to unregister network receiver
     */
    private void unRegisterNetworkReceiver() {
        mNetworkStateReceiver.removeListener(networkStateReceiverListener);
        unregisterReceiver(mNetworkStateReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            // Unregister network receiver
            unRegisterNetworkReceiver();
        } catch (Exception e) {
            Log.e(TAG, "onPause: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            // Register network receiver
            registerNetworkReceiver();
        } catch (Exception e) {
            Log.e(TAG, "onResume: " + e.getMessage());
        }
    }

    private final NetworkStateReceiver.NetworkStateReceiverListener networkStateReceiverListener = new NetworkStateReceiver.NetworkStateReceiverListener() {
        @Override
        public void onNetworkAvailable() {
            hideAlertSnackBar();
        }

        @Override
        public void onNetworkUnavailable() {
            showAlertSnackBar(R.string.alert_network_not_exist);
        }
    };

    /**
     * show SnackBar when network is not available
     *
     * @param alertStringResId
     */
    protected void showAlertSnackBar(@StringRes int alertStringResId) {
        if (null == alertSnackBar) {
            alertSnackBar = Snackbar
                    .make(findViewById(R.id.ll_parent), alertStringResId, Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertSnackBar.dismiss();
                        }
                    });
            alertSnackBar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        } else {
            alertSnackBar.setText(alertStringResId);
        }
        alertSnackBar.show();
    }

    /**
     * hide SnackBar when network is available
     */
    protected void hideAlertSnackBar() {
        if (null != alertSnackBar) {
            alertSnackBar.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        AudiostreamMetadataManager.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public void onFMMetaDataFound(String title, String artis, String album) {
        if (!TextUtils.isEmpty(title)) {
            titleNameTv.setText(title);
        } else
            titleNameTv.setText(savedFmStation.getFmName());

        if (artis != null) {
            artistNameTv.setText(artis);
        } else
            artistNameTv.setText("");

        if (null != album) {
            albumNameTv.setText(album);
        } else
            albumNameTv.setText("");
    }

    @Override
    public void onErrorFound(Exception e) {
        e.printStackTrace();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playFmStationTransLoader != null) {
                    playFmStationTransLoader.dismiss();
                }

                mediaPlayerProvider.onErrorFoundInFmStream();
                onPlayStateChanged(PlaybackState.STATE_ERROR);
            }
        });
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if ((v.getId() == R.id.ibtn_play_pause) && (null != mediaPlayerProvider) && (null != savedFmStation)) {
                mediaPlayerProvider.onPlayBtnPressed();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*public static void startActivity(Activity context, Bundle bundle) {
        Intent intent = new Intent(context, InternetRadioActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }*/
}
