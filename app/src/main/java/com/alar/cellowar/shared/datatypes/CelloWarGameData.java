package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexi on 1/26/2018.
 */

public class CelloWarGameData implements Serializable{
    private static final long serialVersionUID = 1L;

    public enum State {
        ANT_PLACEMENT,    // Player placing antennas.
        WAIT_FOR_OTHER,   // Player finished and is waiting for the other player to finish.
        SHOW_RESULT       // Showing mutual results.
    }

    public static final float BASE_H = 50.0f;

    public List<Antenna> ants;

    public List<Obstacle> obst;

    public State state;

    public CelloWarGameData() {
        ants = new ArrayList<Antenna>();
        obst = new ArrayList<Obstacle>();
        state = State.ANT_PLACEMENT;
    }

    // TODO: EW
    public void CalcRouting(float width) {
        // First clear
        for (Antenna a : ants) {
            a.routing.routed_antennas.clear();
            a.routing.routed_bases_top.clear();
            a.routing.routed_bases_bottom.clear();
        }

        // Blue base - top left, bottom right
        CalcSingleBaseRouting( true, 0.0f, width/2.0f,1);
        CalcSingleBaseRouting( false, width/2.0f, width, 1);
        // red base
        CalcSingleBaseRouting( true, width/2.0f, width, 2);
        CalcSingleBaseRouting( false, 0.0f, width/2.0f, 2);
    }

    public void CalcSingleBaseRouting(boolean is_top, float left, float right, int base_id){
        for (Antenna a : ants) {
            if(is_top) {
                if (a._y - a._radius < BASE_H) {
                    if (a._x > left && a._x < right) {
                        a.routing.routed_bases_top.add(base_id);
                    }
                }
            }
            else /*(is_bottom)*/ {
                if (a._y + a._radius > BASE_H) {
                    if (a._x > left && a._x < right) {
                        a.routing.routed_bases_bottom.add(base_id);
                    }
                }
            }
        }
    }
}