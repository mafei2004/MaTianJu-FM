package com.matianju.fm;

final class PlaybackSkip {
    private static final long MIN_CONTENT_MS=3000;
    private PlaybackSkip(){}

    static long clamp(long millis){return Math.max(0,Math.min(600000,millis));}
    static boolean isSafe(long duration,long intro,long outro){return duration>clamp(intro)+clamp(outro)+MIN_CONTENT_MS;}
    static long effectiveIntro(long duration,long intro,long outro){return isSafe(duration,intro,outro)?clamp(intro):0;}
    static boolean shouldSkipOutro(long duration,long position,long intro,long outro){long safeOutro=clamp(outro);return safeOutro>0&&isSafe(duration,intro,safeOutro)&&position>=duration-safeOutro;}
}
