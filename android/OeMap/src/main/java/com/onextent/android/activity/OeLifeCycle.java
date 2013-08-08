package com.onextent.android.activity;

public interface OeLifeCycle {

    public void onCreate();
    public void onDestroy();

    public void onResume();
    public void onPause();

    public void onStart();
    public void onStop();
}
