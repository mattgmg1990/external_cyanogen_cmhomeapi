package org.cyanogenmod.launcher.home.api.cards;

import android.content.ContentResolver;
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
    private int mId = -1;
    protected CmHomeContract.ICmHomeContract mICmHomeContract;

    public PublishableCard(CmHomeContract.ICmHomeContract contract) {
        mICmHomeContract = contract;
    }

    public int getId() {
        return mId;
    }

    private void setId(int id) {
        mId = id;
    }

    public void publish(Context context) {
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
    }

    protected abstract ContentValues getContentValues();

    protected boolean update(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = contentResolver.update(mICmHomeContract.getContentUri(),
                                          getContentValues(),
                                          mICmHomeContract.getIdColumnName() + " = ?",
                                          new String[]{Integer.toString(getId())});

        // We must have updated at least one row
        return rows > 0;
    }

    public boolean unpublish(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = contentResolver.delete(mICmHomeContract.getContentUri(),
                                          mICmHomeContract.getIdColumnName() + " = ?",
                                          new String[]{Integer.toString(getId())});
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
        Cursor cursor = contentResolver.query(mICmHomeContract.getContentUri(),
                                              new String[]{mICmHomeContract.getIdColumnName()},
                                              mICmHomeContract.getIdColumnName() + " = ?",
                                              new String[]{Integer.toString(getId())},
                                              null);
        return cursor.getCount() > 0;
    }
}
