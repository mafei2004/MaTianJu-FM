package com.matianju.fm;

import static org.junit.Assert.*;
import org.junit.Test;

public final class PlaybackSkipTest {
    @Test public void appliesIntroAndOutroForNormalEpisodes(){assertEquals(30000,PlaybackSkip.effectiveIntro(600000,30000,45000));assertFalse(PlaybackSkip.shouldSkipOutro(600000,554999,30000,45000));assertTrue(PlaybackSkip.shouldSkipOutro(600000,555000,30000,45000));}
    @Test public void disablesSkippingWhenAudioIsTooShort(){assertEquals(0,PlaybackSkip.effectiveIntro(60000,30000,30000));assertFalse(PlaybackSkip.shouldSkipOutro(60000,59000,30000,30000));}
    @Test public void clampsInvalidDurations(){assertEquals(0,PlaybackSkip.clamp(-1));assertEquals(600000,PlaybackSkip.clamp(999999));}
}
