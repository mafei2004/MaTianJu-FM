package com.matianju.fm;

import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
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

    private static final int INK=Color.rgb(17,29,39), PAPER=Color.rgb(246,243,236), GREEN=Color.rgb(23,107,91), GREEN_DARK=Color.rgb(0,81,68), MUTED=Color.rgb(105,113,118), BLUE_SOFT=Color.rgb(237,244,255), OUTLINE=Color.rgb(221,226,223);
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
        LinearLayout content=new LinearLayout(context);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(20),dp(14),dp(20),0);content.setBackgroundColor(PAPER);content.setMinimumHeight(context.getResources().getDisplayMetrics().heightPixels-dp(150));
        TextView title=text("选择文件夹",24,GREEN_DARK,true);title.setGravity(Gravity.CENTER);content.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView hint=text("点击进入目录，确认当前路径后才会开始扫描",14,MUTED,false);hint.setGravity(Gravity.CENTER);hint.setPadding(0,dp(8),0,dp(16));content.addView(hint);
        pathView=text("",14,INK,true);pathView.setPadding(dp(14),dp(12),dp(14),dp(12));pathView.setBackground(roundStroke(BLUE_SOFT,12,OUTLINE,1));content.addView(pathView,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(context);rows=new LinearLayout(context);rows.setOrientation(LinearLayout.VERTICAL);rows.setPadding(0,dp(14),0,dp(8));scroll.addView(rows);content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        emptyView=text("",14,MUTED,false);emptyView.setPadding(dp(8),dp(20),dp(8),dp(20));rows.addView(emptyView);

        dialog=new AlertDialog.Builder(context)
                .setView(content)
                .setNegativeButton("取消",(d,w)->{})
                .setNeutralButton("上一级",null)
                .setPositiveButton("确认添加并扫描",null)
                .create();
        dialog.setOnShowListener(d->{
            Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new ColorDrawable(PAPER));window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);}
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                Node current=current();selected=true;dialog.dismiss();listener.onSelected(current.id,current.name,current.path);
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackground(round(GREEN_DARK,24));dialog.getButton(AlertDialog.BUTTON_POSITIVE).setPadding(dp(16),0,dp(16),0);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(GREEN_DARK);dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(MUTED);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{if(stack.size()>1){stack.remove(stack.size()-1);render();}});
            render();
        });
        dialog.setOnDismissListener(d->{if(!selected)listener.onCancelled();});
        dialog.show();
    }

    private void render(){
        Node current=current();pathView.setText("⌂  "+current.path.replace(" / ","  ›  "));rows.removeAllViews();
        List<Node> children=queryChildren(current);
        if(children==null){dialog.dismiss();return;}
        if(children.isEmpty()){
            emptyView=text("这里没有下一级文件夹。若要添加当前文件夹，请点“确认添加并扫描”。",14,MUTED,false);
            emptyView.setPadding(dp(8),dp(22),dp(8),dp(22));rows.addView(emptyView);
        }else{
            for(Node child:children){
                LinearLayout row=new LinearLayout(context);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(8),dp(12),dp(8));row.setBackground(roundStroke(Color.WHITE,16,OUTLINE,1));TextView folder=text("▰",20,GREEN_DARK,true);folder.setGravity(Gravity.CENTER);folder.setBackground(round(BLUE_SOFT,10));row.addView(folder,new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout details=new LinearLayout(context);details.setOrientation(LinearLayout.VERTICAL);details.setPadding(dp(14),0,0,0);details.addView(text(child.name,16,INK,true));details.addView(text("点击进入此文件夹",12,MUTED,false));row.addView(details,new LinearLayout.LayoutParams(0,-2,1));TextView arrow=text("›",27,GREEN_DARK,false);arrow.setGravity(Gravity.CENTER);row.addView(arrow,new LinearLayout.LayoutParams(dp(34),dp(48)));row.setOnClickListener(v->{stack.add(child);render();});
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(76));p.setMargins(0,0,0,dp(8));rows.addView(row,p);
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
    private GradientDrawable roundStroke(int color,int radius,int stroke,int width){GradientDrawable g=round(color,radius);g.setStroke(dp(width),stroke);return g;}
    private int dp(int value){return (int)(value*context.getResources().getDisplayMetrics().density+.5f);}

    private static final class Node {
        final String id,name,path;
        Node(String id,String name,String path){this.id=id;this.name=name;this.path=path;}
    }
}
