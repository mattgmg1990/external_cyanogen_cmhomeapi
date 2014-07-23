package org.cyanogenmod.launcher.home.api.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

/**
 * Creates the SQLite database that backs CMHomeContentProvider.
 */
public class CmHomeDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME          = "CmHomeAPI";
    private static final String TAG                    = "CmHomeDatabaseHelper";
    private static final int    DATABASE_VERSION       = 1;
    public static final String DATA_CARD_TABLE_NAME    = "DataCard";
    private static final String DATA_CARD_TABLE_CREATE =
            "CREATE TABLE " + DATA_CARD_TABLE_NAME +
            "(" + CmHomeContract.DataCard._ID + " INTEGER PRIMARY KEY NOT NULL," +
            CmHomeContract.DataCard.SUBJECT_COL + " TEXT NOT NULL," +
            CmHomeContract.DataCard.DATE_CONTENT_CREATED_COL + " TEXT NOT NULL," +
            CmHomeContract.DataCard.DATE_CREATED_COL + " TEXT DEFAULT CURRENT_TIMESTAMP " +
            "NOT NULL," +
            CmHomeContract.DataCard.LAST_MODIFIED_COL + " TEXT DEFAULT CURRENT_TIMESTAMP " +
            "NOT NULL," +
            CmHomeContract.DataCard.CONTENT_SOURCE_IMAGE_URI_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.AVATAR_IMAGE_URI_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.TITLE_TEXT_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.SMALL_TEXT_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.BODY_TEXT_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.ACTION_1_TEXT_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.ACTION_1_URI_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.ACTION_2_TEXT_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.ACTION_2_URI_COL + " TEXT DEFAULT NULL," +
            CmHomeContract.DataCard.PRIORITY_COL + " INTEGER DEFAULT NULL);";

    private static final String DATA_CARD_UPDATE_TIME_TRIGGER =
            "CREATE TRIGGER data_card_update_time_trigger " +
            "AFTER UPDATE ON " + DATA_CARD_TABLE_NAME + " FOR EACH ROW" +
            " BEGIN " +
            "UPDATE " + DATA_CARD_TABLE_NAME +
            " SET " + "last_modified" + " = CURRENT_TIMESTAMP" +
            " WHERE " + "_id" + " = old._id;" +
            " END";

    public static final String DATA_CARD_IMAGE_TABLE_NAME   = "DataCardImage";
    private static final String DATA_CARD_IMAGE_TABLE_CREATE =
            "CREATE TABLE " + DATA_CARD_IMAGE_TABLE_NAME +
            "(_id INTEGER PRIMARY KEY," +
            "data_card_id INTEGER NOT NULL," +
            "image_uri TEXT NOT NULL," +
            "FOREIGN KEY(data_card_id) REFERENCES " + DATA_CARD_TABLE_NAME +
            "(_id));";

    private static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS";

    public CmHomeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATA_CARD_TABLE_CREATE);
        database.execSQL(DATA_CARD_UPDATE_TIME_TRIGGER);
        database.execSQL(DATA_CARD_IMAGE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion +
                   ". All existing data will be destroyed.");
        database.execSQL(DROP_TABLE_STATEMENT + " " + DATA_CARD_TABLE_NAME);
        database.execSQL(DROP_TABLE_STATEMENT + " " + DATA_CARD_IMAGE_TABLE_NAME);
        onCreate(database);
    }
}
