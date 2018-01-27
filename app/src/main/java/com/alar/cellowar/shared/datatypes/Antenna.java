package com.alar.cellowar.shared.datatypes;

import android.graphics.Rect;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by alexi on 1/26/2018.
 */

public class Antenna implements Serializable{
    private static final long serialVersionUID = 1L;

    static final int ANT_W = 110;
    static final int ANT_H = 150;
    static final int ANT_BASE_H = ANT_H / 4;

    public enum AntennaType {
        TRANSMISSION,
        ELECTONIC_WARFARE
    }

    public class AntennaRouting implements Serializable{
        private static final long serialVersionUID = 1L;

        public HashSet<Antenna> routed_antennas;
        public HashSet<Integer> routed_bases_top;
        public HashSet<Integer> routed_bases_bottom;

        AntennaRouting() {
            routed_antennas = new HashSet<Antenna>();
            routed_bases_bottom = new HashSet<Integer>();
            routed_bases_top = new HashSet<Integer>();
        }
    }

    public AntennaRouting routing;

    public final float _radius;
    public float _x,_y;
    public final AntennaType _type;

    public Antenna(float radius, float x,float y, AntennaType type) {
        _radius = radius;
        _x = x;
        _y = y;
        _type = type;
        routing = new AntennaRouting();
    }

    public boolean isInsideHalo(float x, float y) {
        return Math.sqrt(Math.pow(x - this._x, 2.0) + Math.pow(y - this._y, 2.0)) < this._radius;
    }

    public Rect getRect() {
        return getRect(0.0f,0.0f);
    }


    public Rect getRect(float dx, float dy) {
        return new Rect(
                (int)(this._x - ANT_W / 2 + dx),
                (int)(this._y - ANT_H / 2 + dy),
                (int)(this._x + ANT_W / 2 + dx),
                (int)(this._y + ANT_H / 2 + dy));
    }

    public Rect getCollisionRect() {
        return getCollisionRect(0.0f, 0.0f);
    }

    public Rect getCollisionRect(float dx, float dy) {
        return new Rect(
                (int) (this._x - ANT_W / 2 + dx),
                (int) (this._y + ANT_H / 2 - ANT_BASE_H + dy),
                (int) (this._x + ANT_W / 2 + dx),
                (int) (this._y + ANT_H / 2 + dy));
    }

    public float getLeft() {
        return this._x - ANT_W / 2;
    }

    public float getTop() {
        return this._y - ANT_H / 2;
    }

    public float getRight() {
        return this._x + ANT_W / 2;
    }

    public float getBottom() {
        return this._y + ANT_H / 2;
    }
}
