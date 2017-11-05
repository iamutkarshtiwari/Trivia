package io.github.iamutkarshtiwari.trivia.models;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by utkarshtiwari on 04/11/17.
 */

public class QuestionContract {
    public static final String CONTENT_AUTHORITY = "io.github.iamutkarshtiwari.trivia";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_QUESTION = "questions";

    private QuestionContract() {

    }

    public static final class QuestionEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUESTION;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUESTION;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_QUESTION);

        public final static String TABLE_NAME = "questions";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_QUESTION = "question";
        public final static String COLUMN_CORRECT_ANSWER = "answer";
    }

}