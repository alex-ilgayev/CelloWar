package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;

/**
 * Created by alexi on 1/26/2018.
 */

public class Obstacle implements Serializable {
    private static final long serialVersionUID = 1L;

    float _top, _right, _bottom, _left;

    public Obstacle(float left, float top, float right, float bottom) {
        _top = top;
        _right = right;
        _bottom = bottom;
        _left = left;
    }
}
