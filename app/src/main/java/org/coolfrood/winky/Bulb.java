package org.coolfrood.winky;

import java.util.ArrayList;
import java.util.List;

public class Bulb {
    public int id;
    public String name;
    public boolean powered;
    public List<Integer> tags;

    Bulb(int id, String name, boolean powered, List<Integer> tags) {
        this.id = id;
        this.name = name;
        this.powered = powered;
        this.tags = tags;
    }
    Bulb(int id, String name, boolean powered) {
        this.id = id;
        this.name = name;
        this.powered = powered;
        this.tags = new ArrayList<>();
    }

    String getTagList() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < tags.size() - 1; i++) {
            b.append(tags.get(i)).append(",");
        }
        if (tags.size() > 0) {
            b.append(tags.get(tags.size() - 1));
        }
        return b.toString();
    }

    @Override
    public String toString() {
        return id + ":" + name + " powered " + powered;
    }
}
