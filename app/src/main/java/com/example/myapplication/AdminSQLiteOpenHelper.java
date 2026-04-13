package com.example.myapplication;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla de usuarios alineada con lo que usa MainActivity
        db.execSQL("create table usuarios(" +
                "alias text primary key, " +
                "password text, " +
                "ultimo_programa text, " +
                "volumen_max int, " +
                "modo_audio int, " +
                "pitch_nivel real" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists usuarios");
        onCreate(db);
    }
}