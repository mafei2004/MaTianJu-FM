package com.matianju.fm;

import static org.junit.Assert.*;
import org.junit.Test;

public final class FolderIdentityTest {
    @Test public void recognizesExactAndNestedSelections(){String root="primary:Movies";assertTrue(FolderIdentity.isSameOrDescendant(root,root));assertTrue(FolderIdentity.isSameOrDescendant(root,"primary:Movies/大唐狄公探案"));assertTrue(FolderIdentity.isSameOrDescendant(root,"primary:Movies/小说/第一季"));}
    @Test public void rejectsSiblingAndPrefixCollision(){String root="primary:Movies";assertFalse(FolderIdentity.isSameOrDescendant(root,"primary:Music"));assertFalse(FolderIdentity.isSameOrDescendant(root,"primary:Movies2/小说"));}
    @Test public void recoversDocumentIdFromExcludedKey(){assertEquals("primary:Movies/大唐狄公探案",FolderIdentity.documentIdFromExcludedKey("content://provider/tree/primary%3AMovies#primary:Movies/大唐狄公探案"));}
}
