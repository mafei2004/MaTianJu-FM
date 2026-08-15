package com.matianju.fm;

import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Browses descendants of a SAF tree inside the app. Some OEM document pickers
 * return a directory as soon as its row is tapped, so the system picker is used
 * only to obtain access and this dialog owns the actual, explicit selection.
 */
@SuppressLint("SetTextI18n")
public final class FolderBrowserDialog {
    public interface Listener {
        void onSelected(String documentId, String name, String path);
        void onCancelled();
    }

    private static final int INK=Color.rgb(23,34,45), MUTED=Color.rgb(105,113,118);
    private final Context context;
    private final Uri treeUri;
    private final Listener listener;
    private final ArrayList<Node> stack=new ArrayList<>();
    private LinearLayout rows;
    private TextView pathView, emptyView;
    private AlertDialog dialog;
    private boolean selected;

    public FolderBrowserDialog(Context context,Uri treeUri,String startDocumentId,String startName,Listener listener){
        this.context=context;this.treeUri=treeUri;this.listener=listener;
        stack.add(new Node(startDocumentId,startName,startName));
    }

    public void show(){
        LinearLayout content=new LinearLayout(context);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(22),dp(4),dp(22),0);
        TextView hint=text("逐层点进子文件夹。无论进入多少层，只有点最下面的“确认添加并扫描”才会开始扫描。",14,MUTED,false);hint.setPadding(0,0,0,dp(12));content.addView(hint);
        pathView=text("",16,INK,true);pathView.setPadding(dp(12),dp(10),dp(12),dp(10));pathView.setBackground(round(Color.rgb(238,242,239),10));content.addView(pathView,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(context);rows=new LinearLayout(context);rows.setOrientation(LinearLayout.VERTICAL);rows.setPadding(0,dp(10),0,dp(6));scroll.addView(rows);content.addView(scroll,new LinearLayout.LayoutParams(-1,dp(330)));
        emptyView=text("",14,MUTED,false);emptyView.setPadding(dp(8),dp(20),dp(8),dp(20));rows.addView(emptyView);

        dialog=new AlertDialog.Builder(context)
                .setTitle("选择要添加的文件夹")
                .setView(content)
                .setNegativeButton("取消",(d,w)->{})
                .setNeutralButton("上一级",null)
                .setPositiveButton("确认添加并扫描",null)
                .create();
        dialog.setOnShowListener(d->{
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                Node current=current();selected=true;dialog.dismiss();listener.onSelected(current.id,current.name,current.path);
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{if(stack.size()>1){stack.remove(stack.size()-1);render();}});
            render();
        });
        dialog.setOnDismissListener(d->{if(!selected)listener.onCancelled();});
        dialog.show();
    }

    private void render(){
        Node current=current();pathView.setText("当前选择："+current.path);rows.removeAllViews();
        List<Node> children=queryChildren(current);
        if(children==null){dialog.dismiss();return;}
        if(children.isEmpty()){
            emptyView=text("这里没有下一级文件夹。若要添加当前文件夹，请点“确认添加并扫描”。",14,MUTED,false);
            emptyView.setPadding(dp(8),dp(22),dp(8),dp(22));rows.addView(emptyView);
        }else{
            for(Node child:children){
                TextView row=text("📁  "+child.name+"   ›",16,INK,true);row.setGravity(android.view.Gravity.CENTER_VERTICAL);row.setPadding(dp(14),0,dp(12),0);row.setBackground(round(Color.WHITE,10));row.setOnClickListener(v->{stack.add(child);render();});
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.setMargins(0,0,0,dp(7));rows.addView(row,p);
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(stack.size()>1);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("确认添加“"+shortName(current.name)+"”并扫描");
    }

    private List<Node> queryChildren(Node parent){
        ArrayList<Node> out=new ArrayList<>();
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,parent.id);
        String[] projection={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE};
        try(Cursor c=context.getContentResolver().query(children,projection,null,null,null)){
            if(c!=null)while(c.moveToNext())if(DocumentsContract.Document.MIME_TYPE_DIR.equals(c.getString(2))){String name=c.getString(1);out.add(new Node(c.getString(0),name,parent.path+" / "+name));}
        }catch(Exception e){Toast.makeText(context,"无法读取这个文件夹："+(e.getMessage()==null?"权限不足":e.getMessage()),Toast.LENGTH_LONG).show();return null;}
        out.sort((a,b)->NaturalOrder.compare(a.name,b.name));return out;
    }

    private Node current(){return stack.get(stack.size()-1);}
    private String shortName(String name){return name.length()>10?name.substring(0,10)+"…":name;}
    private TextView text(String value,int size,int color,boolean bold){TextView v=new TextView(context);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));return v;}
    private GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private int dp(int value){return (int)(value*context.getResources().getDisplayMetrics().density+.5f);}

    private static final class Node {
        final String id,name,path;
        Node(String id,String name,String path){this.id=id;this.name=name;this.path=path;}
    }
}
