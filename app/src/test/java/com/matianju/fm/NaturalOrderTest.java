package com.matianju.fm;

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;

public final class NaturalOrderTest {
    @Test public void sortsEpisodeNumbersNumerically() {
        List<String> names = new ArrayList<>(Arrays.asList("11.mp3", "2.mp3", "10.mp3", "1.mp3"));
        names.sort(NaturalOrder::compare);
        assertEquals(Arrays.asList("1.mp3", "2.mp3", "10.mp3", "11.mp3"), names);
    }

    @Test public void sortsNumbersInsideChineseTitles() {
        List<String> names = new ArrayList<>(Arrays.asList("第12集.mp3", "第3集.mp3", "第02集.mp3"));
        names.sort(NaturalOrder::compare);
        assertEquals(Arrays.asList("第02集.mp3", "第3集.mp3", "第12集.mp3"), names);
    }
}
