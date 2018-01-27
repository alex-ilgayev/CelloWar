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

    public static float BASE_H = 50.00f;
    public static float GOAL_RADIUS = 50.0f;

    public List<Antenna> ants;
    public List<Obstacle> obst;

    public State state;
    private float viewW;
    private float viewH;

    public CelloWarGameData() {
        ants = new ArrayList<Antenna>();
        obst = new ArrayList<Obstacle>();
        state = State.ANT_PLACEMENT;
        viewW = 1080.0f;
        viewH = 1500.0f;
    }

    public void setWH(float w, float h) {
        viewW = w;
        viewH = h;
    }

    private float fixX(float x, float newW) {
        return (x/viewW)*newW;
    }

    private float fixY(float y, float newH) {
        return (y/viewH)*newH;
    }

    public void UpdateViewSize(float newW, float newH) {
        /*for(Obstacle o : obst) {
            o._bottom = fixY(o._bottom, newH);
            o._left = fixX(o._left, newW);
            o._top = fixY(o._top,newH);
            o._right = fixX(o._right, newW);
        }

        for(Antenna a : ants) {
            a._x = fixX(a._x, newW);
            a._y = fixY(a._y, newH);

            a._radius = fixX(a._radius, newW); // IMPORTANT fixing radius using W scale
        }

        Antenna.ANT_H = fixY(Antenna.ANT_H, newH);
        Antenna.ANT_W = fixX(Antenna.ANT_W, newW);
        Antenna.ANT_BASE_H = fixY(Antenna.ANT_BASE_H, newH);

        Antenna.ANT_EW_H = fixY(Antenna.ANT_EW_H, newH);
        Antenna.ANT_EW_W = fixX(Antenna.ANT_EW_W, newW);
        Antenna.ANT_EW_BASE_H = fixY(Antenna.ANT_EW_BASE_H, newH);

        BASE_H = fixY(BASE_H, newH);
        GOAL_RADIUS = fixX(GOAL_RADIUS, newW); // IMPORTANT - Radius is fixed according to width.

        viewH = newH;
        viewW = newW;*/

        CalcRouting(newW, newH); // Recalculate routing
    }


    public void CalcRouting(float width, float height) {
        // First clear
        for (Antenna a : ants) {
            a.routing.routed_antennas.clear();
            a.routing.routed_bases_top.clear();
            a.routing.routed_bases_bottom.clear();
            a.routing.routed_goal = false;
            a.routing.isSpoofed = false;
        }

        // Calculate spoofing by EW antennas. Spoofed antennas can not participate in
        // the transmission
        CalcSpoofing();

        // Blue base - top left, bottom right
        CalcSingleBaseRouting( true, 0.0f, width/2.0f, height, 1);
        CalcSingleBaseRouting( false, width/2.0f, width, height, 1);
        // red base
        CalcSingleBaseRouting( true, width/2.0f, width, height,2);
        CalcSingleBaseRouting( false, 0.0f, width/2.0f, height, 2);

        CalcGoalRouting(width /2.0f, height / 2.0f);

        // Now calculate the rest of the antennas
        CalcTransitiveRouting();

    }

    private void CalcSpoofing() {
        for(Antenna a : ants) {
            if (a._type == Antenna.AntennaType.TRANSMISSION) {
                for(Antenna b : ants) {
                    if (b._type == Antenna.AntennaType.ELECTONIC_WARFARE) {
                        if (b.isInsideHalo(a._x, a._y)) {
                            a.routing.isSpoofed = true;
                        }
                    }
                }
            }
        }
    }

    private void CalcGoalRouting(float goal_x, float goal_y) {
        for (Antenna a : ants) {
            // Ignore spoofed antennas
            if (a.routing.isSpoofed) {
                continue;
            }

            if (Math.pow(a._x - goal_x, 2.0) + Math.pow(a._y - goal_y, 2.0) <
                    Math.pow(a._radius+GOAL_RADIUS, 2.0))
            {
                a.routing.routed_goal = true;
            }
        }
    }

    private void CalcSingleBaseRouting(boolean is_top, float left, float right, float height, int base_id){
        for (Antenna a : ants) {
            // Ignore spoofed antennas
            if (a.routing.isSpoofed) {
                continue;
            }

            if(is_top) {
                if (a._y - a._radius < BASE_H) {
                    if ((a._x > left && a._x < right) ||
                            a.isInsideHalo(right , BASE_H) ||
                            a.isInsideHalo(left , BASE_H)) {
                        a.routing.routed_bases_top.add(base_id);
                    }
                }
            }
            else /*(is_bottom)*/ {
                if (a._y + a._radius > height- BASE_H) {
                    if ((a._x > left && a._x < right) ||
                            a.isInsideHalo(right , height- BASE_H) ||
                            a.isInsideHalo(left , height - BASE_H)) {
                        a.routing.routed_bases_bottom.add(base_id);
                    }
                }
            }
        }
    }

    /**
     * Highly inefficient approximately O(N**4). Use with up to 20 antennas
     */
    public void CalcTransitiveRouting() {
        // Number of steps == number of nodes
        for (int i = 0; i<ants.size(); i++) {
            for(Antenna a : ants) {
                if (a._type == Antenna.AntennaType.TRANSMISSION && a.routing.isSpoofed == false) {
                    for (Antenna b : ants) {
                        if (b._type == Antenna.AntennaType.TRANSMISSION && b.routing.isSpoofed == false) {
                            // a needs to be in b's halo or vice versa
                            /*if (a.isInsideHalo(b._x, b._y) || b.isInsideHalo(a._x, a._y)) {
                                a.routing.routed_bases_top.addAll(b.routing.routed_bases_top);
                                b.routing.routed_bases_top.addAll(a.routing.routed_bases_top);
                                a.routing.routed_bases_bottom.addAll(b.routing.routed_bases_bottom);
                                b.routing.routed_bases_bottom.addAll(a.routing.routed_bases_bottom);
                                if (a.routing.routed_goal || b.routing.routed_goal) {
                                    a.routing.routed_goal = true;
                                    b.routing.routed_goal = true;
                                }
                            }*/

                            // a and b halos' must touch
                            if (Math.pow(a._x - b._x, 2.0) + Math.pow(a._y - b._y, 2.0) <
                                    Math.pow(a._radius+b._radius, 2.0))
                            {
                                a.routing.routed_bases_top.addAll(b.routing.routed_bases_top);
                                b.routing.routed_bases_top.addAll(a.routing.routed_bases_top);
                                a.routing.routed_bases_bottom.addAll(b.routing.routed_bases_bottom);
                                b.routing.routed_bases_bottom.addAll(a.routing.routed_bases_bottom);
                                if (a.routing.routed_goal || b.routing.routed_goal) {
                                    a.routing.routed_goal = true;
                                    b.routing.routed_goal = true;
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}