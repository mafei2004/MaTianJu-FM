package com.matianju.fm;

import static org.junit.Assert.*;
import org.junit.Test;

public final class InterruptionResumeStateTest {
    @Test public void resumesOnlyWhenTransientLossInterruptedPlayback(){InterruptionResumeState s=new InterruptionResumeState();s.onTransientLoss(true);assertTrue(s.consumeOnGain());assertFalse(s.consumeOnGain());}
    @Test public void doesNotResumeAudioThatWasAlreadyPaused(){InterruptionResumeState s=new InterruptionResumeState();s.onTransientLoss(false);assertFalse(s.consumeOnGain());}
    @Test public void manualPauseOrPermanentLossCancelsResume(){InterruptionResumeState s=new InterruptionResumeState();s.onTransientLoss(true);s.onManualPause();assertFalse(s.consumeOnGain());s.onTransientLoss(true);s.onPermanentLoss();assertFalse(s.consumeOnGain());}
    @Test public void repeatedTransientCallbacksDoNotForgetPendingResume(){InterruptionResumeState s=new InterruptionResumeState();s.onTransientLoss(true);s.onTransientLoss(false);assertTrue(s.consumeOnGain());}
}
