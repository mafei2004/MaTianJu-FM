package com.matianju.fm;

import static org.junit.Assert.*;
import org.junit.Test;

public final class QueueNavigationTest {
    @Test public void movesDirectlyToPreviousEpisode(){assertEquals(312,QueueNavigation.previousIndex(313,500,false));}
    @Test public void firstEpisodeRestartsOrWraps(){assertEquals(0,QueueNavigation.previousIndex(0,500,false));assertEquals(499,QueueNavigation.previousIndex(0,500,true));}
}
