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

    public List<Antenna> ants;

    public final List<Obstacle> obst;

    public State state;


    public CelloWarGameData() {
        ants = new ArrayList<Antenna>();
        obst = new ArrayList<Obstacle>();
        state = State.ANT_PLACEMENT;
    }
}
