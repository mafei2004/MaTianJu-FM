package com.matianju.fm;

final class InterruptionResumeState {
    private boolean pending;

    void onTransientLoss(boolean wasPlaying){if(wasPlaying)pending=true;}
    void onPermanentLoss(){pending=false;}
    void onManualPause(){pending=false;}
    boolean consumeOnGain(){boolean result=pending;pending=false;return result;}
}
