package org.cyanogenmod.launcher.home.api;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.util.LongSparseArray;

import org.cyanogenmod.launcher.home.api.cards.DataCard;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMHomeApiService extends Service {
    private final static String TAG = "CMHomeApiService";
    private final static String FEED_READ_PERM = "org.cyanogenmod.launcher.home.api.FEED_READ";
    private final static String FEED_WRITE_PERM = "org.cyanogenmod.launcher.home.api.FEED_WRITE";
    private static final int    DATA_CARD_LIST               = 1;
    private static final int    DATA_CARD_ITEM               = 2;
    private static final int    DATA_CARD_DELETE_ITEM        = 3;
    private static final int    DATA_CARD_IMAGE_LIST         = 4;
    private static final int    DATA_CARD_IMAGE_ITEM         = 5;
    private static final int    DATA_CARD_IMAGE_DELETE_ITEM  = 6;

    private final IBinder mBinder = new LocalBinder();

    private HashMap<String, ProviderInfo> mProviders = new HashMap<String, ProviderInfo>();
    private HashMap<String, LongSparseArray<DataCard>> mCards = new HashMap<String,
                                                                  LongSparseArray<DataCard>>();

    private CardContentObserver mContentObserver;
    private HandlerThread mContentObserverHandlerThread;
    private Handler mContentObserverHandler;

    public CMHomeApiService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        // Start up a background thread to handle any incoming changes.
        mContentObserverHandlerThread = new HandlerThread("CMHomeApiObserverThread");
        mContentObserverHandlerThread.start();
        mContentObserverHandler = new Handler(mContentObserverHandlerThread.getLooper());

        new LoadExtensionsAndCardsAsync().execute();
    }

    @Override
    public void onDestroy() {
        mContentObserverHandlerThread.quitSafely();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }

    private class LoadExtensionsAndCardsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected
        Void doInBackground(Void... voids) {
            loadAllExtensions();
            trackAllExtensions();
            loadAllCards();
            return null;
        }
    }

    private void loadAllExtensions() {
        List<PackageInfo> providerPackages =
                getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        for (PackageInfo packageInfo : providerPackages) {
            ProviderInfo[] providers = packageInfo.providers;
            if (providers != null) {
                for (ProviderInfo providerInfo : providers) {
                    if (FEED_READ_PERM.equals(providerInfo.readPermission)
                        && FEED_WRITE_PERM.equals(providerInfo.writePermission)) {
                        mProviders.put(providerInfo.authority, providerInfo);
                    }
                }
            }
        }
    }

    private void trackAllExtensions() {
        mContentObserver = new CardContentObserver(mContentObserverHandler);

         for (Map.Entry<String, ProviderInfo> entry : mProviders.entrySet()) {
             ProviderInfo providerInfo = entry.getValue();
             Uri getCardsUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                         CmHomeContract.DataCard.LIST_INSERT_UPDATE_URI_PATH);
             Uri getImagesUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                          CmHomeContract.DataCardImage.LIST_INSERT_UPDATE_URI_PATH);
             getContentResolver().registerContentObserver(getCardsUri,
                                                          true,
                                                          mContentObserver);
             getContentResolver().registerContentObserver(getImagesUri,
                                                          true,
                                                          mContentObserver);
        }
    }

    private void loadAllCards() {
        for (Map.Entry<String, ProviderInfo> entry : mProviders.entrySet()) {
            ProviderInfo providerInfo = entry.getValue();
            Uri getCardsUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                        CmHomeContract.DataCard.LIST_INSERT_UPDATE_URI_PATH);
            Uri getImagesUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                         CmHomeContract.DataCardImage.LIST_INSERT_UPDATE_URI_PATH);
            List<DataCard> cards = DataCard.getAllPublishedDataCards(this,
                                                                     getCardsUri,
                                                                     getImagesUri);
            // For quick access, build a HashMap using the id as the key
            LongSparseArray<DataCard> cardMap = new LongSparseArray<DataCard>();
            for (DataCard card : cards) {
                cardMap.put(card.getId(), card);
            }
            mCards.put(entry.getKey(), cardMap);
        }
    }

    private class CardContentObserver extends ContentObserver {

        public CardContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (!selfChange) {
                handleUriChange(uri);
            }
        }
    }

    private void handleUriChange(Uri uri) {
        String authority = uri.getAuthority();
        UriMatcher matcher = getUriMatcherForAuthority(authority);
        switch (matcher.match(uri)) {
            case DATA_CARD_LIST:
                // Todo: figure out what rows were changed?
                // It might not be trivial to handle a list of changes
                break;
            case DATA_CARD_ITEM:
                onCardInsertOrUpdate(uri);
                break;
            case DATA_CARD_DELETE_ITEM:
                onCardDelete(uri);
                break;
            case DATA_CARD_IMAGE_LIST:
                // Todo: figure out what rows were changed?
                // It might not be trivial to handle a list of changes
                break;
            case DATA_CARD_IMAGE_ITEM:
                onCardImageInsertOrUpdate(uri);
                break;
            case DATA_CARD_IMAGE_DELETE_ITEM:
                onCardImageDelete(uri);
                break;
            default:
                Log.w(TAG, "Unsupported Uri change notification: " + uri);
        }
    }

    private void onCardInsertOrUpdate(Uri uri) {
        String authority = uri.getAuthority();
        long id = Long.getLong(uri.getLastPathSegment());
        LongSparseArray<DataCard> cards = mCards.get(authority);
        boolean cardExists = false;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri,
                                              CmHomeContract.DataCard.PROJECTION_ALL,
                                              null,
                                              null,
                                              CmHomeContract.DataCard.DATE_CREATED_COL);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            DataCard theNewCard = DataCard.createFromCurrentCursorRow(cursor);
            if (cards == null) {
                cards = new LongSparseArray<DataCard>();
                cards.put(theNewCard.getId(), theNewCard);
                mCards.put(authority, cards);
            } else {
                cardExists = cards.get(id) != null;
                cards.put(theNewCard.getId(), theNewCard);
            }
        }

        cursor.close();

        // TODO notify observers of card update
    }

    private void onCardDelete(Uri uri) {
        String authority = uri.getAuthority();
        LongSparseArray<DataCard> cards = mCards.get(authority);
        if (cards != null) {
            long id = Long.getLong(uri.getLastPathSegment());
            cards.delete(id);
        }

        // TODO notify observers of card delete
    }

    private void onCardImageInsertOrUpdate(Uri uri) {
        // TODO handle a CardImage insert or update
    }

    private void onCardImageDelete(Uri uri) {
        // TODO handle a CardImage delete
    }

    private UriMatcher getUriMatcherForAuthority(String authority) {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority,
                       CmHomeContract.DataCard.LIST_INSERT_UPDATE_URI_PATH,
                       DATA_CARD_LIST);
        matcher.addURI(authority,
                       CmHomeContract.DataCard.SINGLE_ROW_INSERT_UPDATE_URI_PATH,
                       DATA_CARD_ITEM);
        matcher.addURI(authority,
                       CmHomeContract.DataCard.SINGLE_ROW_DELETE_URI_PATH_MATCH,
                       DATA_CARD_DELETE_ITEM);
        matcher.addURI(authority,
                       CmHomeContract.DataCardImage.LIST_INSERT_UPDATE_URI_PATH,
                       DATA_CARD_IMAGE_LIST);
        matcher.addURI(authority,
                       CmHomeContract.DataCardImage.SINGLE_ROW_INSERT_UPDATE_URI_PATH,
                       DATA_CARD_IMAGE_ITEM);
        matcher.addURI(authority,
                       CmHomeContract.DataCardImage.SINGLE_ROW_DELETE_URI_PATH_MATCH,
                       DATA_CARD_IMAGE_DELETE_ITEM);
        return matcher;
    }

    public List<DataCard> getAllDataCards() {
        List<DataCard> theCards = new ArrayList<DataCard>();
        for (LongSparseArray<DataCard> cards : mCards.values()) {
            for (int i = 0; i < cards.size(); i++) {
                theCards.add(cards.valueAt(i));
            }
        }
        return theCards;
    }

    public class LocalBinder extends Binder {
        public CMHomeApiService getService() {
            return CMHomeApiService.this;
        }
    }
}
