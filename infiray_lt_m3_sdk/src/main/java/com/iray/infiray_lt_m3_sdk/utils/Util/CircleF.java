package com.iray.infiray_lt_m3_sdk.utils.Util;

import android.graphics.PointF;

public class CircleF {
    private PointF centerPoint;
    private float radius;
    public CircleF() {

    }
    public CircleF(PointF centerPoint, float radius) {
        this.centerPoint = centerPoint;
        this.radius = radius;
    }

    public PointF getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(PointF centerPoint) {
        this.centerPoint = centerPoint;
    }

    public float getCenterPointX() {
        return centerPoint.x;
    }

    public void setCenterPointX(float centerPointX) {
        this.centerPoint.x = centerPointX;
    }

    public float getCenterPointY() {
        return centerPoint.y;
    }

    public void setCenterPointY(float centerPointY) {
        this.centerPoint.y = centerPointY;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
