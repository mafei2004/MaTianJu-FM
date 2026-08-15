package com.matianju.fm;

final class QueueNavigation {
    private QueueNavigation(){}

    static int previousIndex(int current,int size,boolean listLoop){
        if(size<=0)return -1;
        if(current>0)return current-1;
        return listLoop?size-1:0;
    }
}
