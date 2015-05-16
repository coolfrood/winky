package org.coolfrood.winky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceDb {

    private DbHelper dbHelper;
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String POWERED = "powered";
    public static final String TAGS = "tags";
    public static final String TABLE = "devices";

    public DeviceDb(Context context) {
        dbHelper = new DbHelper(context);
    }

    public List<Bulb> getBulbs() {
        ArrayList<Bulb> bulbs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(TABLE,
                null,
                null,
                null,
                null,
                null,
                null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            List<Integer> tags = new ArrayList<>();
            String tagString = c.getString(3);
            if (!tagString.equals("")) {
                String[] t = tagString.split(",");
                for (String s : t) {
                    tags.add(Integer.parseInt(s));
                }
            }
            Bulb bulb = new Bulb(
                    c.getInt(0),
                    c.getString(1),
                    c.getInt(2) > 0, tags);
            bulbs.add(bulb);
            c.moveToNext();
        }
        c.close();
        db.close();
        return bulbs;
    }

    public void mergeWithUpdate(List<Bulb> bulbs) {
        List<Bulb> bulbsFromDb = getBulbs();
        Map<Integer, Bulb> m = new HashMap<>();
        for (Bulb b: bulbsFromDb) {
            m.put(b.id, b);
        }
        for (Bulb b: bulbs) {
            Bulb fromDb = m.get(b.id);
            if (fromDb != null) {
                b.tags = fromDb.tags;
                m.remove(b.id);
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            doUpdate(db, bulbs);
            for (Map.Entry<Integer, Bulb> e : m.entrySet()) {
                db.delete(TABLE, ID + " = ?", new String[]{"" + e.getValue().id});
            }
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        db.close();
    }

    public void update(List<Bulb> bulbs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        doUpdate(db, bulbs);
        db.close();
    }

    private void doUpdate(SQLiteDatabase db, List<Bulb> bulbs) {
        for (Bulb bulb: bulbs) {
            ContentValues values = new ContentValues();
            values.put(NAME, bulb.name);
            values.put(POWERED, bulb.powered ? 1 : 0);
            values.put(ID, bulb.id);
            values.put(TAGS, bulb.getTagList());
            db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
}
