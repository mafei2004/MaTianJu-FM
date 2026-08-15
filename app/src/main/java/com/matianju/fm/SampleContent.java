package com.matianju.fm;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import java.io.*;
import java.util.*;

public final class SampleContent {
    public static final String ROOT_URI="internal://sample";
    public static final String FOLDER_ID=ROOT_URI+"#root";
    private static final String FILE_NAME="Theme from -Knight Rider.mp3";
    private SampleContent(){}

    public static void ensure(Context context){LibraryDb db=LibraryDb.get(context);if("1".equals(db.setting("sample_removed","0")))return;try{File dir=new File(context.getFilesDir(),"samples");if(!dir.exists()&&!dir.mkdirs())return;File audio=new File(dir,FILE_NAME);if(!audio.exists()){try(InputStream in=context.getResources().openRawResource(R.raw.knight_rider_theme);OutputStream out=new FileOutputStream(audio)){byte[]buffer=new byte[32768];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);}}
            LibraryDb.Folder folder=new LibraryDb.Folder();folder.id=FOLDER_ID;folder.rootUri=ROOT_URI;folder.parentId=null;folder.name="内置示例";folder.path="内置示例";
            LibraryDb.Track track=new LibraryDb.Track();track.uri=Uri.fromFile(audio).toString();track.folderId=FOLDER_ID;track.name=FILE_NAME;track.size=audio.length();track.modified=audio.lastModified();MediaMetadataRetriever m=new MediaMetadataRetriever();try{m.setDataSource(audio.getAbsolutePath());track.title=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);track.artist=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);String duration=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);if(duration!=null)track.duration=Long.parseLong(duration);}finally{try{m.release();}catch(Exception ignored){}}
            db.upsertRoot(ROOT_URI,"内置示例");db.replaceScan(ROOT_URI,Collections.singletonList(folder),Collections.singletonList(track));
        }catch(Exception ignored){}
    }
    public static void remove(Context context){LibraryDb db=LibraryDb.get(context);db.setSetting("sample_removed","1");db.removeRoot(ROOT_URI);File audio=new File(new File(context.getFilesDir(),"samples"),FILE_NAME);if(audio.exists())audio.delete();}
    public static boolean isSample(LibraryDb.Folder folder){return folder!=null&&ROOT_URI.equals(folder.rootUri);}
}
