package io.github.iamutkarshtiwari.trivia.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import io.github.iamutkarshtiwari.trivia.models.QuestionContract.QuestionEntry;

/**
 * Created by utkarshtiwari on 04/11/17.
 */

public class QuestionDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public QuestionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCTS_TABLE =  "CREATE TABLE " + QuestionEntry.TABLE_NAME + " ("
                + QuestionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + QuestionEntry.COLUMN_QUESTION + " TEXT NOT NULL, "
                + QuestionEntry.COLUMN_CORRECT_ANSWER + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}