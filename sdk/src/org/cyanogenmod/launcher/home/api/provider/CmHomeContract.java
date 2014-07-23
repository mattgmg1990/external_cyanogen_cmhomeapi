package org.cyanogenmod.launcher.home.api.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class CmHomeContract {
    public static String AUTHORITY =
            "org.cyanogenmod.launcher.home.api";

    public static Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    public static final class DataCard implements BaseColumns, ICmHomeContract {
        public static final String SUBJECT_COL = "subject";
        public static final String DATE_CONTENT_CREATED_COL = "date_content_created";
        public static final String DATE_CREATED_COL = "date_created";
        public static final String LAST_MODIFIED_COL = "last_modified";
        public static final String CONTENT_SOURCE_IMAGE_URI_COL = "content_source_image_uri";
        public static final String AVATAR_IMAGE_URI_COL = "avatar_image_uri";
        public static final String TITLE_TEXT_COL = "title_text";
        public static final String SMALL_TEXT_COL = "small_text";
        public static final String BODY_TEXT_COL = "body_text";
        public static final String ACTION_1_TEXT_COL = "action_1_text";
        public static final String ACTION_1_URI_COL = "action_1_uri";
        public static final String ACTION_2_TEXT_COL = "action_2_text";
        public static final String ACTION_2_URI_COL = "action_2_uri";
        public static final String PRIORITY_COL = "priority";

        public static Uri CONTENT_URI =
                Uri.withAppendedPath(CmHomeContract.CONTENT_URI, "datacard");

        public static String LIST_INSERT_UPDATE_URI_PATH = "datacard";
        public static String SINGLE_ROW_INSERT_UPDATE_URI_PATH = "datacard/#";
        public static String SINGLE_ROW_DELETE_URI_PATH = "/datacard/delete";
        public static String SINGLE_ROW_DELETE_URI_PATH_MATCH = "/datacard/delete/#";

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/org.cyanogenmod.home.api.datacard";

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/org.cyanogenmod.home.api.datacard";

        public static final String[] PROJECTION_ALL =
                {_ID, SUBJECT_COL, DATE_CONTENT_CREATED_COL, DATE_CREATED_COL, LAST_MODIFIED_COL,
                 CONTENT_SOURCE_IMAGE_URI_COL, AVATAR_IMAGE_URI_COL, TITLE_TEXT_COL,
                 SMALL_TEXT_COL, BODY_TEXT_COL, ACTION_1_TEXT_COL, ACTION_1_URI_COL,
                 ACTION_2_TEXT_COL, ACTION_2_URI_COL};

        public static final String SORT_ORDER_DEFAULT =
                PRIORITY_COL + " ASC";

        @Override
        public Uri getContentUri() {
            return CONTENT_URI;
        }

        @Override
        public String getIdColumnName() {
            return _ID;
        }
    }

    public static final class DataCardImage implements BaseColumns, ICmHomeContract {
        public static final String DATA_CARD_ID_COL = "data_card_id";
        public static final String IMAGE_URI_COL = "image_uri";

        public static Uri CONTENT_URI =
                Uri.withAppendedPath(CmHomeContract.CONTENT_URI, "datacardimage");

        public static String LIST_INSERT_UPDATE_URI_PATH = "datacardimage";
        public static String SINGLE_ROW_INSERT_UPDATE_URI_PATH = "datacardimage/#";
        public static String SINGLE_ROW_DELETE_URI_PATH = "/datacardimage/delete";
        public static String SINGLE_ROW_DELETE_URI_PATH_MATCH = "/datacardimage/delete/#";

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/org.cyanogenmod.home.api.datacardimage";

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/org.cyanogenmod.home.api.datacardimage";

        public static final String[] PROJECTION_ALL =
                {_ID, DATA_CARD_ID_COL, IMAGE_URI_COL};

        public static final String SORT_ORDER_DEFAULT =
                DATA_CARD_ID_COL + " ASC";

        @Override
        public Uri getContentUri() {
            return CONTENT_URI;
        }

        @Override
        public String getIdColumnName() {
            return _ID;
        }
    }

    public static void setAuthority(String authority) {
        AUTHORITY = authority;
        CONTENT_URI = Uri.parse("content://" + AUTHORITY);
        DataCard.CONTENT_URI = Uri.withAppendedPath(CmHomeContract.CONTENT_URI, "datacard");
        DataCardImage.CONTENT_URI =
                Uri.withAppendedPath(CmHomeContract.CONTENT_URI, "datacardimage");
    }

    public interface ICmHomeContract {
        public Uri getContentUri();
        public String getIdColumnName();
    }
}
