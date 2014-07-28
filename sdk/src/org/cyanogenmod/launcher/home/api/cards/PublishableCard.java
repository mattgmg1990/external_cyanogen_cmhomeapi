package org.cyanogenmod.launcher.home.api.cards;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

/**
 * Represents any card that can be published to the host application,
 * to be displayed to the user.
 */
public abstract class PublishableCard {
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

            Uri result = contentResolver.insert(mICmHomeContract.getContentUri(), values);
            // Store the resulting ID
            setId(Integer.parseInt(result.getLastPathSegment()));
        }

        return updated;
    }

    protected abstract ContentValues getContentValues();

    protected boolean update(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = contentResolver.update(ContentUris.withAppendedId(
                                                      mICmHomeContract.getContentUri(),
                                                      getId()),
                                          getContentValues(),
                                          null,
                                          null);

        // We must have updated at least one row
        return rows > 0;
    }

    public boolean unpublish(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = contentResolver.delete(ContentUris.withAppendedId(
                                                    mICmHomeContract.getContentUri(),
                                                    getId()),
                                          null,
                                          null);
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
        Cursor cursor = contentResolver.query(ContentUris.withAppendedId(
                                                  mICmHomeContract.getContentUri(),
                                                  getId()),
                                              new String[]{mICmHomeContract.getIdColumnName()},
                                              null,
                                              null,
                                              null);
        int cursorCount = cursor.getCount();
        cursor.close();
        return cursorCount > 0;
    }
}
