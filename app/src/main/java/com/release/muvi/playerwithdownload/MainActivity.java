package com.release.muvi.playerwithdownload;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;

import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity   {
    private static final int AUDIO = 4;
     public static final int NORMAL_MEDIA = 1;
    public static final int DASH_URL = 2;
    public static final int M3U8_URL = 3;
    // bandwidth meter to measure and estimate bandwidth
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = "nihar";
    private ComponentListener componentListener;
    private DefaultTrackSelector trackSelector;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;
    ProgressBar progressBar;
    Button Download_bt;
    private Handler mainHandler;

    String url ;
        Button download_btn;
    String proxy_url;
    private DefaultExtractorsFactory extractorsFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler();

        //added for .m3u8 support

        componentListener = new ComponentListener();

    }


    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 21) {
            initializePlayer(url);
//            prepareExoPlayerFromFileUri(proxy_url);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT > 21 || player == null)) {
            initializePlayer(url);
//            prepareExoPlayerFromFileUri(proxy_url);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }



    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.setVideoListener(null);
            player.setVideoDebugListener(null);
            player.setAudioDebugListener(null);
            player.release();
            player = null;
        }
    }
    private void initializePlayer(String proxy_url) {
        if (player == null) {
            // a factory to create an AdaptiveVideoTrackSelection
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            // let the factory create a player instance with default components
            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());

            playerView.setPlayer(player);
            player.addListener(componentListener);
            player.setVideoDebugListener(componentListener);
            player.setAudioDebugListener(componentListener);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }

        url="https://secure-streams.akamaized.net/rt-esp/index.m3u8";
        MediaSource mediaSource = buildMediaSource(Uri.parse(url),M3U8_URL);

//        DataSpec dataSpec = new DataSpec(Uri.parse(url) , 0, 100 * 1024, null);
//        CacheUtil.cache(dataSpec,null,mediaSource,null);
//    MediaSource mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash)));
        player.prepare(mediaSource, true, false);
 /*       player.addMetadataOutput(new MetadataOutput() {
            @Override
            public void onMetadata(Metadata metadata) {
            }
        });*/
       String cacheKey =  CacheUtil.generateKey(Uri.parse(url));

//        DataSpec dataSpec = new DataSpec(Uri.parse(url),0, 1000 * 1024, null);
       /* Cache cache = null;
        CacheUtil.CachingCounters cachingCounters = CacheUtil.getCached(dataSpec,cache,null);
        long bytes = cachingCounters.downloadedBytes;
        Log.v("NiharChace",""+bytes);*/




    }
    private MediaSource buildMediaSource(Uri uri, int MediaType) {
        MediaSource mediaSource = null;
        switch (MediaType) {
            case NORMAL_MEDIA:
                mediaSource = new ExtractorMediaSource(uri,
                        new DefaultHttpDataSourceFactory("ua"),
                        new DefaultExtractorsFactory(), null, null);
                break;
            case DASH_URL:
                DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER);
                DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(
                        dataSourceFactory);
                mediaSource = new DashMediaSource(uri, dataSourceFactory, dashChunkSourceFactory, null, null);
                break;

            case M3U8_URL:
               return new HlsMediaSource(uri, new DefaultHttpDataSourceFactory("Android-ExoPlayer", BANDWIDTH_METER), null, null);
               /* mediaSource = new HlsMediaSource(
                        uri,
                        new DefaultDataSourceFactory(this, "Android-ExoPlayer", BANDWIDTH_METER),
                        1, null, null);*/
            case AUDIO:
                dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener<? super DataSource>) BANDWIDTH_METER);
                extractorsFactory = new DefaultExtractorsFactory();
                mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);


        }
        return mediaSource;
    }
    private class ComponentListener implements ExoPlayer.EventListener, VideoRendererEventListener, AudioRendererEventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
           Log.v("Niharbandwidth",BANDWIDTH_METER.getBitrateEstimate()+"");
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    progressBar.setVisibility(View.VISIBLE);
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    progressBar.setVisibility(View.VISIBLE);
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    progressBar.setVisibility(View.GONE);

                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString
                    + " playWhenReady: " + playWhenReady);
        }


        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity() {

        }




        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }



        /////Video render listner .
        @Override
        public void onVideoEnabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onVideoInputFormatChanged(com.google.android.exoplayer2.Format format) {

        }


        @Override
        public void onDroppedFrames(int count, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {

        }

        /////Audio render listner
        @Override
        public void onAudioEnabled(DecoderCounters counters) {

        }

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onAudioInputFormatChanged(com.google.android.exoplayer2.Format format) {

        }

        @Override
        public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }





        @Override
        public void onAudioDisabled(DecoderCounters counters) {

        }
    }


    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }






    private void prepareExoPlayerFromFileUri(String proxy_url){
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
        player.addListener(componentListener);
        String path = "/storage/emulated/0/Download/test.mlv";
        Log.v("Nihar",path);
        File file = new File(path);

        final Uri uri = Uri.fromFile(file);
        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource videoSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);



        playerView.setPlayer(player);
        player.addListener(componentListener);
        player.setVideoDebugListener(componentListener);
        player.setAudioDebugListener(componentListener);
        player.setPlayWhenReady(playWhenReady);
        player.prepare(videoSource);

//        CacheUtil.cache(dataSpec, cache, upstreamDataSource, counters);

    }
/*
    private void prepareExoPlayerFromFileUri(String proxy_url){
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(null), new DefaultLoadControl());
        player.addListener(componentListener);


        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(proxy_url),
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);



        playerView.setPlayer(player);
        player.addListener(componentListener);
        player.setVideoDebugListener(componentListener);
        player.setAudioDebugListener(componentListener);
        player.setPlayWhenReady(playWhenReady);
        player.prepare(mediaSource);
    }
*/




}

