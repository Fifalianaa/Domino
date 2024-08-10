package com.example.domino;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class PlayerDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "players.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PLAYERS = "players";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";

    public PlayerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_PLAYERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        onCreate(db);
    }

    public long addPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = getNextAvailableId(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_NAME, player.getName());
        db.insert(TABLE_PLAYERS, null, values);
        db.close();
        return id;
    }

    public List<Player> getAllPlayers() {
        List<Player> playersList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYERS, null, null, null, null, null, COLUMN_ID);

        if (cursor.moveToFirst()) {
            do {
                Player player = new Player();
                player.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                player.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                playersList.add(player);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return playersList;
    }

    public void updatePlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, player.getName());
        db.update(TABLE_PLAYERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(player.getId())});
        db.close();
    }

    public void deletePlayer(int playerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, COLUMN_ID + " = ?", new String[]{String.valueOf(playerId)});
        reorganizePlayerIds(); // Réorganiser les IDs après la suppression
        db.close();
    }

    private void reorganizePlayerIds() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_PLAYERS + " ORDER BY " + COLUMN_ID, null);
            int newId = 1;
            while (cursor.moveToNext()) {
                int oldId = cursor.getInt(0);
                if (oldId != newId) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ID, newId);
                    db.update(TABLE_PLAYERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(oldId)});
                }
                newId++;
            }
            cursor.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private long getNextAvailableId(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT MIN(" + COLUMN_ID + ") FROM " + TABLE_PLAYERS + " WHERE " + COLUMN_ID + " > (SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_PLAYERS + ")", null);
        long id = 1; // Default ID if no available ID is found
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
            if (id == 0) {
                id = getMaxId(db) + 1;
            }
        }
        cursor.close();
        return id;
    }

    private long getMaxId(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_PLAYERS, null);
        long maxId = 0;
        if (cursor.moveToFirst()) {
            maxId = cursor.getLong(0);
        }
        cursor.close();
        return maxId;
    }
}
