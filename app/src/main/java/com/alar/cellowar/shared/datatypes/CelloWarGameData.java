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

    public void CalcRouting(float width, float height) {
        // First clear
        for (Antenna a : ants) {
            a.routing.routed_antennas.clear();
            a.routing.routed_bases_top.clear();
            a.routing.routed_bases_bottom.clear();
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

        // Now calculate the rest of the antennas
        CalcTransitiveRouting();

    }

    public void CalcSpoofing() {
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

    public void CalcSingleBaseRouting(boolean is_top, float left, float right, float height, int base_id){
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
                            // this can be changed later
                            if (a.isInsideHalo(b._x, b._y) || b.isInsideHalo(a._x, a._y)) {
                                a.routing.routed_bases_bottom.addAll(b.routing.routed_bases_top);
                                a.routing.routed_bases_bottom.addAll(b.routing.routed_bases_top);
                                a.routing.routed_bases_bottom.addAll(b.routing.routed_bases_bottom);
                                a.routing.routed_bases_bottom.addAll(b.routing.routed_bases_bottom);
                            }
                        }
                    }
                }
            }
        }
    }
}