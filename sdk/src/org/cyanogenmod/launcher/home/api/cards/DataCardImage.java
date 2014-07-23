package org.cyanogenmod.launcher.home.api.cards;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

import java.util.ArrayList;
import java.util.List;

public class DataCardImage extends PublishableCard {
    private final static CmHomeContract.ICmHomeContract sContract
            = new CmHomeContract.DataCardImage();
    private long mDataCardId;
    private Uri mImageUri;

    public DataCardImage(long dataCardId, Uri imageUri) {
        super(sContract);

        mDataCardId = dataCardId;
        mImageUri = imageUri;
    }

    public long getDataCardId() {
        return mDataCardId;
    }

    public void setDataCardId(long dataCardId) {
        mDataCardId = dataCardId;
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public void setImageUri(Uri imageUri) {
        mImageUri = imageUri;
    }

    @Override
    protected ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(CmHomeContract.DataCardImage.DATA_CARD_ID_COL, getDataCardId());

        if (getImageUri() != null) {
            values.put(CmHomeContract.DataCardImage.IMAGE_URI_COL, getImageUri().toString());
        }

        return values;
    }

    public static List<DataCardImage> getAllPublishedDataCardImages(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(CmHomeContract.DataCardImage.CONTENT_URI,
                                              CmHomeContract.DataCardImage.PROJECTION_ALL,
                                              null,
                                              null,
                                              null);

        List<DataCardImage> allImages = new ArrayList<DataCardImage>();
        while (cursor.moveToNext()) {
            int dataCardId = cursor.getInt(
                    cursor.getColumnIndex(CmHomeContract.DataCardImage.DATA_CARD_ID_COL));
            String imageUriString = cursor.getString(
                    cursor.getColumnIndex(CmHomeContract.DataCardImage.IMAGE_URI_COL));
            int imageId = cursor.getInt(cursor.getColumnIndex(CmHomeContract.DataCardImage._ID));

            if (!TextUtils.isEmpty(imageUriString)) {
                DataCardImage image = new DataCardImage(dataCardId, Uri.parse(imageUriString));
                image.setId(imageId);
                allImages.add(image);
            }
        }
        cursor.close();
        return allImages;
    }

    public static List<DataCardImage> getPublishedDataCardImagesForDataCardId(Context context,
                                                                              long dataCardId) {
        return getPublishedDataCardImagesForDataCardId(context,
                                                       CmHomeContract.DataCardImage.CONTENT_URI,
                                                       dataCardId);
    }

    public static List<DataCardImage> getPublishedDataCardImagesForDataCardId(Context context,
                                                                              Uri contentUri,
                                                                              long dataCardId) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri,
                                          CmHomeContract.DataCardImage.PROJECTION_ALL,
                                          CmHomeContract.DataCardImage.DATA_CARD_ID_COL + " = ?",
                                          new String[]{Long.toString(dataCardId)},
                                          null);

        List<DataCardImage> allImages = new ArrayList<DataCardImage>();
        while (cursor.moveToNext()) {
            String imageUriString = cursor.getString(
                    cursor.getColumnIndex(CmHomeContract.DataCardImage.IMAGE_URI_COL));
            int imageId = cursor.getInt(cursor.getColumnIndex(CmHomeContract.DataCardImage._ID));

            if (!TextUtils.isEmpty(imageUriString)) {
                DataCardImage image = new DataCardImage(dataCardId, Uri.parse(imageUriString));
                image.setId(imageId);
                allImages.add(image);
            }
        }
        cursor.close();
        return allImages;
    }
}
