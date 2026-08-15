package com.matianju.fm;

import android.content.*;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import java.io.FileDescriptor;
import java.util.*;
import java.util.concurrent.*;

public final class FolderScanner {
    public interface Callback { void onProgress(String text); void onDone(int folders,int tracks); void onError(String message); }
    private static final Set<String> EXT=new HashSet<>(Arrays.asList("mp3","m4a","aac","wav","flac","ogg","opus"));
    private final Context context; private final LibraryDb db; private final ExecutorService executor=Executors.newSingleThreadExecutor();
    public FolderScanner(Context c){context=c.getApplicationContext();db=LibraryDb.get(c);}
    public void scanAll(Callback cb){executor.execute(()->{int f=0,t=0;try{for(LibraryDb.ScanRoot root:db.scanRoots()){Result x=scanSelection(root,cb);f+=x.folders.size();t+=x.tracks.size();}done(cb,f,t);}catch(Throwable e){error(cb,e);}});}
    public void scan(Uri tree,String rootName,Callback cb){executor.execute(()->{try{Result r=scanRoot(tree,rootName,cb);done(cb,r.folders.size(),r.tracks.size());}catch(Throwable e){error(cb,e);}});}
    public void scanFolder(LibraryDb.Folder folder,Callback cb){executor.execute(()->{try{Uri tree=Uri.parse(folder.rootUri);String prefix=folder.rootUri+"#";if(!folder.id.startsWith(prefix))throw new IllegalArgumentException("目录标识无效");String documentId=folder.id.substring(prefix.length());Result r=new Result();walk(tree,documentId,folder.parentId,folder.name,folder.path,r,cb);db.replaceSubtree(folder.id,r.folders,r.tracks);done(cb,r.folders.size(),r.tracks.size());}catch(Throwable e){error(cb,e);}});}
    private Result scanSelection(LibraryDb.ScanRoot root,Callback cb)throws Exception{Uri tree=Uri.parse(root.permissionUri);Result r=new Result();walk(tree,root.documentId,null,root.name,root.path,r,cb);db.replaceSubtree(root.id,r.folders,r.tracks);return r;}
    private Result scanRoot(Uri tree,String rootName,Callback cb)throws Exception{
        Result result=new Result(); String rootId=DocumentsContract.getTreeDocumentId(tree); walk(tree,rootId,null,rootName,rootName,result,cb);
        db.replaceScan(tree.toString(),result.folders,result.tracks);return result;
    }
    private void walk(Uri tree,String documentId,String parentId,String name,String path,Result out,Callback cb)throws Exception{
        String folderId=tree+"#"+documentId;if(db.isExcluded(folderId))return;
        LibraryDb.Folder folder=new LibraryDb.Folder();folder.id=folderId;folder.rootUri=tree.toString();folder.parentId=parentId;folder.name=name;folder.path=path;out.folders.add(folder);
        post(cb,"正在扫描："+path);
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,documentId);
        String[] projection={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE,DocumentsContract.Document.COLUMN_LAST_MODIFIED};
        try(Cursor c=context.getContentResolver().query(children,projection,null,null,null)){
            if(c==null)return;while(c.moveToNext()){
                String id=c.getString(0), childName=c.getString(1),mime=c.getString(2);long size=c.isNull(3)?0:c.getLong(3),modified=c.isNull(4)?0:c.getLong(4);
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)){walk(tree,id,folder.id,childName,path+" / "+childName,out,cb);continue;}
                if(!isAudio(childName,mime))continue;
                Uri uri=DocumentsContract.buildDocumentUriUsingTree(tree,id); LibraryDb.Track t=metadata(uri);t.uri=uri.toString();t.folderId=folder.id;t.name=childName;t.size=size;t.modified=modified;out.tracks.add(t);
            }
        }
    }
    private boolean isAudio(String name,String mime){if(mime!=null&&mime.startsWith("audio/"))return true;int dot=name.lastIndexOf('.');return dot>0&&EXT.contains(name.substring(dot+1).toLowerCase(Locale.ROOT));}
    private LibraryDb.Track metadata(Uri uri){LibraryDb.Track t=new LibraryDb.Track();MediaMetadataRetriever m=new MediaMetadataRetriever();try(android.os.ParcelFileDescriptor p=context.getContentResolver().openFileDescriptor(uri,"r")){if(p!=null){m.setDataSource(p.getFileDescriptor());t.title=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);t.artist=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);String d=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);if(d!=null)t.duration=Long.parseLong(d);}}catch(Exception ignored){}finally{try{m.release();}catch(Exception ignored){}}return t;}
    private void post(Callback cb,String s){new Handler(Looper.getMainLooper()).post(()->cb.onProgress(s));}
    private void done(Callback cb,int f,int t){new Handler(Looper.getMainLooper()).post(()->cb.onDone(f,t));}
    private void error(Callback cb,Throwable e){new Handler(Looper.getMainLooper()).post(()->cb.onError(e.getMessage()==null?e.getClass().getSimpleName():e.getMessage()));}
    private static final class Result{final List<LibraryDb.Folder>folders=new ArrayList<>();final List<LibraryDb.Track>tracks=new ArrayList<>();}
}
