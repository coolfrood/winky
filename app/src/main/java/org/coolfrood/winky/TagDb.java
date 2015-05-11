package org.coolfrood.winky;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TagDb {
    private DbHelper dbHelper;
    public static final String TABLE = "tags";
    public static final String ID = "_id"; // this becomes autoincrement on its own in sqlite
    public static final String NAME = "name";
    public static final String IGNORED = "ignored";

    public TagDb(Context context) {
        dbHelper = new DbHelper(context);
    }

    public List<NfcTag> getTags(boolean includeIgnored) {
        ArrayList<NfcTag> tags = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = null;

        if (!includeIgnored) {
            selection = IGNORED + " > 0";
        }
        Cursor c = db.query(TABLE,
                new String[] { ID, NAME },
                selection,
                null,
                null,
                null,
                null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            NfcTag tag = new NfcTag(
                    c.getInt(0),
                    c.getString(1));
            tags.add(tag);
            c.moveToNext();
        }
        c.close();
        db.close();
        return tags;
    }

    public void remove(NfcTag tag) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, ID + " = ?", new String[] { "" + tag.id });
        db.close();
    }

    public void update(NfcTag tag) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, tag.name);
        db.update(TABLE, values, ID + " = ?", new String[] { "" + tag.id });
        db.close();
    }
}
