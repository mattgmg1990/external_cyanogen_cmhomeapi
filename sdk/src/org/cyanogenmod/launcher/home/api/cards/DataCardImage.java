package org.cyanogenmod.launcher.home.api.cards;

import android.content.ContentValues;
import android.net.Uri;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

public class DataCardImage extends PublishableCard {
    private final static CmHomeContract.ICmHomeContract sContract
            = new CmHomeContract.DataCardImage();
    private int mDataCardId;
    private Uri mImageUri;

    public DataCardImage(int dataCardId, Uri imageUri) {
        super(sContract);

        mDataCardId = dataCardId;
        mImageUri = imageUri;
    }

    public int getDataCardId() {
        return mDataCardId;
    }

    public void setDataCardId(int dataCardId) {
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
        values.put(CmHomeContract.DataCardImage.IMAGE_URI_COL, getImageUri().toString());

        return values;
    }
}
