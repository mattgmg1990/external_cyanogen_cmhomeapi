package org.cyanogenmod.launcher.home.api.cards;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataCard {
    private static final int PRIORITY_HIGH = 1;
    private static final int PRIORITY_MID  = 2;
    private static final int PRIORITY_LOW  = 3;

    private int    mId = -1;
    private String mSubject;
    private Date   mContentCreatedDate;
    private Date   mCreatedDate;
    private Date   mLastModifiedDate;
    private Uri    mContentSourceImageUri;
    private Uri    mAvatarImageUri;
    private String mTitle;
    private String mSmallText;
    private String mBodyText;
    private String mAction1Text;
    private Uri    mAction1Uri;
    private String mAction2Text;
    private Uri    mAction2Uri;
    private int    mPriority = 3;

    private List<DataCardImage> mImages = new ArrayList<DataCardImage>();

    public DataCard(String subject, Date contentCreatedDate) {
        mSubject = subject;
        mContentCreatedDate = contentCreatedDate;
    }

    public void addDataCardImage(Uri uri) {
        mImages.add(new DataCardImage(getId(), uri));
    }

    public void addDataCardImage(DataCardImage image) {
        mImages.add(image);
    }

    public void clearImages() {
        mImages.clear();
    }

    public void removeDataCardImage(DataCardImage image) {
        mImages.remove(image);
    }

    public int getId() {
        return mId;
    }

    private void setId(int id) {
        mId = id;
    }

    public Date getCreatedDate() {
        return mCreatedDate;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public Date getContentCreatedDate() {
        return mContentCreatedDate;
    }

    public void setContentCreatedDate(Date contentCreatedDate) {
        this.mContentCreatedDate = contentCreatedDate;
    }

    public Date getLastModifiedDate() {
        return mLastModifiedDate;
    }

    public Uri getContentSourceImageUri() {
        return mContentSourceImageUri;
    }

    public void setContentSourceImageUri(Uri contentSourceImageUri) {
        this.mContentSourceImageUri = contentSourceImageUri;
    }

    public Uri getAvatarImageUri() {
        return mAvatarImageUri;
    }

    public void setAvatarImageUri(Uri avatarImageUri) {
        this.mAvatarImageUri = avatarImageUri;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getSmallText() {
        return mSmallText;
    }

    public void setSmallText(String smallText) {
        this.mSmallText = smallText;
    }

    public String getBodyText() {
        return mBodyText;
    }

    public void setBodyText(String bodyText) {
        this.mBodyText = bodyText;
    }

    public String getAction1Text() {
        return mAction1Text;
    }

    public void setAction1Text(String action1Text) {
        this.mAction1Text = action1Text;
    }

    public Uri getAction1Uri() {
        return mAction1Uri;
    }

    public void setAction1Uri(Uri action1Uri) {
        this.mAction1Uri = action1Uri;
    }

    public String getAction2Text() {
        return mAction2Text;
    }

    public void setAction2Text(String action2Text) {
        this.mAction2Text = action2Text;
    }

    public Uri getAction2Uri() {
        return mAction2Uri;
    }

    public void setAction2Uri(Uri action2Uri) {
        this.mAction2Uri = action2Uri;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
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

            Uri result = contentResolver.insert(CmHomeContract.DataCard.CONTENT_URI, values);
            // Store the resulting ID
            setId(Integer.parseInt(result.getLastPathSegment()));
        }
    }

    /**
     * Updates an existing row in the ContentProvider that represents this card.
     * This will update every column at once.
     * @param context A Context object to retrieve the ContentResolver
     * @return true if the update successfully updates a row, false otherwise.
     */
    private boolean update(Context context) {
        if (getId() == -1) {
            return false;
        }

        ContentResolver contentResolver = context.getContentResolver();
        int rows = contentResolver.update(CmHomeContract.DataCard.CONTENT_URI,
                                   getContentValues(),
                                   CmHomeContract.DataCard._ID + " = " + getId(),
                                   new String[]{});

        // We must have updated more than one row
        return rows > 0;

    }

    private ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(CmHomeContract.DataCard.SUBJECT_COL, getSubject());
        values.put(CmHomeContract.DataCard.DATE_CONTENT_CREATED_COL,
                   getContentCreatedDate().getTime());
        values.put(CmHomeContract.DataCard.CONTENT_SOURCE_IMAGE_URI_COL,
                   getContentSourceImageUri().toString());
        values.put(CmHomeContract.DataCard.AVATAR_IMAGE_URI_COL,
                   getAvatarImageUri().toString());
        values.put(CmHomeContract.DataCard.TITLE_TEXT_COL,
                   getTitle());
        values.put(CmHomeContract.DataCard.SMALL_TEXT_COL,
                   getSmallText());
        values.put(CmHomeContract.DataCard.BODY_TEXT_COL,
                   getBodyText());
        values.put(CmHomeContract.DataCard.ACTION_1_TEXT_COL,
                   getAction1Text());
        values.put(CmHomeContract.DataCard.ACTION_1_URI_COL,
                   getAction1Uri().toString());
        values.put(CmHomeContract.DataCard.ACTION_2_TEXT_COL,
                   getAction2Text());
        values.put(CmHomeContract.DataCard.ACTION_2_URI_COL,
                   getAction2Uri().toString());
        values.put(CmHomeContract.DataCard.PRIORITY_COL,
                   getAction2Uri().toString());

        return values;
    }
}
