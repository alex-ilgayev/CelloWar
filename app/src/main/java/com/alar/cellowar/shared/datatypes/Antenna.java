package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;

/**
 * Created by alexi on 1/26/2018.
 */

public class Antenna implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum AntennaType {
        TRANSMISSION,
        ELECTONIC_WARFARE
    }

    public final float _radius;
    public float _x,_y;
    public final AntennaType _type;

    public Antenna(float radius, float x,float y, AntennaType type) {
        _radius = radius;
        _x = x;
        _y = y;
        _type = type;
    }
}
