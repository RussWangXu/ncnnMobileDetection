package com.iray.infiray_lt_m3_sdk.utils.Util;


public class DrawElement {

    public int type;//0:点,1:line 2:框
    public int itemNum;
   // public int typeNum;
    public int a1xleft;
    public  int a1ytop;
    public int a2xright;
    public int a2ybottom;
    public float max;
    public float min;
    public float avg;
    public String picName;


    public DrawElement() {
        this.itemNum = 1;
    }

    public DrawElement(int type, int itemNum, int a1x , int a1y, int a2x, int a2y, float max, float min, float avg, String picName) {
       this.type = type;
       this.itemNum = itemNum;
       this.a1xleft = a1x;
       this.a1ytop = a1y;
       this.a2xright= a2x;
       this.a2ybottom =a2y;
       this.max = max;
       this.min = min;
       this.avg = avg;
       this.picName = picName;
    }

    public void setA1xleft(int a1xleft) {
        this.a1xleft = a1xleft;
    }

    public void setA1ytop(int a1ytop) {
        this.a1ytop = a1ytop;
    }

    public void setA2xright(int a2xright) {
        this.a2xright = a2xright;
    }

    public void setA2ybottom(int a2ybottom) {
        this.a2ybottom = a2ybottom;
    }

    public void setType(int type){
        this.type =type;
    }
    public void setItemNum(int itemNum){
        this.itemNum =itemNum;
    }
    public void setMax(float max){
        this.max = max;
    }
    public void setMin(float min){
        this.min = min;
    }
    public void setAvg(float avg){
        this.avg = avg;
    }
    public void setPicName(String picName){
        this.picName = picName;
    }
    public float getA1xleft(){
        return a1xleft;
    }
    public float getA1ytop(){
        return a1ytop;
    }
    public float getA2xright(){
        return a2xright;
    }
    public float getA2ybottom(){
        return  a2ybottom;
    }
    public int getItemNum(){
        return  itemNum;
    }
    public int getType(){
        return  type;
    }
    public float getMax(){
        return max;
    }
    public float getMin(){
        return min;
    }
    public float getAvg(){
        return avg;
    }
    public  String getPicName(){
        return  picName;
    }


}
