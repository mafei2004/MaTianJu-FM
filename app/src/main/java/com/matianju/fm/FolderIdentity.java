package com.matianju.fm;

public final class FolderIdentity {
    private FolderIdentity(){}
    public static boolean isSameOrDescendant(String rootDocumentId,String selectedDocumentId){return selectedDocumentId.equals(rootDocumentId)||selectedDocumentId.startsWith(rootDocumentId+"/");}
    public static String documentIdFromExcludedKey(String key){int marker=key.indexOf('#');return marker<0?"":key.substring(marker+1);}
}
