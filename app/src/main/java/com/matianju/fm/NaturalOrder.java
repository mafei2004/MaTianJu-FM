package com.matianju.fm;

public final class NaturalOrder {
    public static int compare(String a,String b){
        int ia=0,ib=0;
        while(ia<a.length()&&ib<b.length()){
            char ca=a.charAt(ia),cb=b.charAt(ib);
            if(Character.isDigit(ca)&&Character.isDigit(cb)){
                int sa=ia,sb=ib;while(sa<a.length()&&a.charAt(sa)=='0')sa++;while(sb<b.length()&&b.charAt(sb)=='0')sb++;
                int ea=sa,eb=sb;while(ea<a.length()&&Character.isDigit(a.charAt(ea)))ea++;while(eb<b.length()&&Character.isDigit(b.charAt(eb)))eb++;
                int la=ea-sa,lb=eb-sb;if(la!=lb)return la-lb;int c=a.regionMatches(true,sa,b,sb,la)?0:a.substring(sa,ea).compareTo(b.substring(sb,eb));if(c!=0)return c;ia=ea;ib=eb;
            }else{int c=Character.compare(Character.toLowerCase(ca),Character.toLowerCase(cb));if(c!=0)return c;ia++;ib++;}
        }return (a.length()-ia)-(b.length()-ib);
    }
}
