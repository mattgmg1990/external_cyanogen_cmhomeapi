package org.cyanogenmod.launcher.home.api.cards;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

/**
 * Represents any card that can be published to the host application,
 * to be displayed to the user.
 */
public abstract class PublishableCard {
    private String TAG = "PublishableCard";
    private long mId = -1;
    protected CmHomeContract.ICmHomeContract mICmHomeContract;
    private String mAuthority;

    public PublishableCard(CmHomeContract.ICmHomeContract contract) {
        mICmHomeContract = contract;
    }

    public long getId() {
        return mId;
    }

    protected void setId(long id) {
        mId = id;
    }

    public void setAuthority(String authority) {
        mAuthority = authority;
    }

    public String getAuthority() {
        return mAuthority;
    }

    public String getGlobalId() {
        return mAuthority + "/" + mId;
    }

    public boolean publish(Context context) {
        boolean updated = false;
        // If we have an ID, try to update that row first.
        if (getId() != -1) {
            updated = update(context);
        }

        // If the update could not succeed, either this card never existed,
        // or was deleted. Either way, create a new row for this card.
        if (!updated) {
            ContentResolver contentResolver = context.getContentResolver();

            ContentValues values = getContentValues();

            Uri result = null;
            try {
                result = contentResolver.insert(mICmHomeContract.getContentUri(), values);
            // Catching all Exceptions, since we can't be sure what the extension will do.
            } catch (Exception e) {
                Log.e(TAG,
                      "Error publishing PublishableCard, ContentProvider threw an exception for " +
                      "uri:" +
                      " " + mICmHomeContract.getContentUri(), e);
            }

            if (result != null) {
                // Store the resulting ID
                setId(Integer.parseInt(result.getLastPathSegment()));
            }
        }

        return updated;
    }

    protected abstract ContentValues getContentValues();

    protected boolean update(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = 0;
        try {
            rows = contentResolver.update(ContentUris.withAppendedId(
                                                      mICmHomeContract.getContentUri(),
                                                      getId()),
                                              getContentValues(),
                                              null,
                                              null);
        // Catching all Exceptions, since we can't be sure what the extension will do.
        } catch (Exception e) {
            Log.e(TAG,
                  "Error updating PublishableCard, ContentProvider threw an exception for uri:" +
                  " " + mICmHomeContract.getContentUri(), e);
        }

        // We must have updated at least one row
        return rows > 0;
    }

    public boolean unpublish(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = 0;
        try {
            rows = contentResolver.delete(ContentUris.withAppendedId(
                                                      mICmHomeContract.getContentUri(),
                                                      getId()),
                                              null,
                                              null);
        // Catching all Exceptions, since we can't be sure what the extension will do.
        } catch (Exception e) {
            Log.e(TAG,
                  "Error unpublishing PublishableCard, ContentProvider threw an exception for " +
                  "uri:" + mICmHomeContract.getContentUri(), e);
        }

        return rows > 0;
    }

    /**
     * Has this card been published to the host application?
     * @param context The context of the publishing application
     * @return True if this card has been published.
     */
    public boolean isPublished(Context context) {
        if(mId == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ContentUris.withAppendedId(
                                                   mICmHomeContract.getContentUri(),
                                                   getId()),
                                           new String[]{mICmHomeContract.getIdColumnName()},
                                           null,
                                           null,
                                           null);
        // Catching all Exceptions, since we can't be sure what the extension will do.
        } catch (Exception e) {
            Log.e(TAG,
                  "Error querying PublishableCard, ContentProvider threw an exception for uri:" +
                  " " + mICmHomeContract.getContentUri(), e);
        }

        int cursorCount = 0;
        if (cursor != null) {
            cursorCount = cursor.getCount();
            cursor.close();
        }
        return cursorCount > 0;
    }
}
