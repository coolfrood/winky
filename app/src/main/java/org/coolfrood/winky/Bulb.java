package org.coolfrood.winky;

/**
 * Created by akshat on 5/9/15.
 */
public class Bulb {
    public String id;
    public String name;
    public boolean powered;

    Bulb(String id, String name, boolean powered) {
        this.id = id;
        this.name = name;
        this.powered = powered;
    }



    @Override
    public String toString() {
        return id + ":" + name + " powered " + powered;
    }
}
