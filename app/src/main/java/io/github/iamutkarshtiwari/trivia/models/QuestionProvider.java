package io.github.iamutkarshtiwari.trivia.models;

/**
 * Created by utkarshtiwari on 04/11/17.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import io.github.iamutkarshtiwari.trivia.models.QuestionContract.QuestionEntry;

/**
 * Created by utkarshtiwari on 19/10/17.
 */

public class QuestionProvider extends ContentProvider {

    public static final String LOG_TAG = QuestionDbHelper.class.getSimpleName();

    private QuestionDbHelper mDbHelper;

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(QuestionContract.CONTENT_AUTHORITY, QuestionContract.PATH_QUESTION, PRODUCTS);
        sUriMatcher.addURI(QuestionContract.CONTENT_AUTHORITY, QuestionContract.PATH_QUESTION + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new QuestionDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match) {
            case PRODUCTS:
                cursor = database.query(QuestionEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = QuestionEntry._ID +"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(QuestionEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return QuestionEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return QuestionEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case PRODUCTS:
                return insertQuestion(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertQuestion(Uri uri, ContentValues values) {
        String question = values.getAsString(QuestionEntry.COLUMN_QUESTION);
        if (question == null) {
            throw new IllegalArgumentException("Question is required");
        }

        String answer = values.getAsString(QuestionEntry.COLUMN_CORRECT_ANSWER);
        if (answer == null) {
            throw new IllegalArgumentException("Answer is required");
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        long id = database.insert(QuestionEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(QuestionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = QuestionEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted =  database.delete(QuestionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    /**
     * Update is not supported
     */
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        throw new IllegalArgumentException("Update is not supported for " + uri);
    }

}