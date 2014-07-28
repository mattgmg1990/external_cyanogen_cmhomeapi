package org.cyanogenmod.launcher.home.api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.LongSparseArray;

import org.cyanogenmod.launcher.home.api.cards.DataCard;
import org.cyanogenmod.launcher.home.api.provider.CmHomeContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMHomeApiManager {
    private final static String TAG = "CMHomeApiManager";
    private final static String FEED_READ_PERM = "org.cyanogenmod.launcher.home.api.FEED_READ";
    private final static String FEED_WRITE_PERM = "org.cyanogenmod.launcher.home.api.FEED_WRITE";
    private static final int    DATA_CARD_LIST               = 1;
    private static final int    DATA_CARD_ITEM               = 2;
    private static final int    DATA_CARD_DELETE_ITEM        = 3;
    private static final int    DATA_CARD_IMAGE_LIST         = 4;
    private static final int    DATA_CARD_IMAGE_ITEM         = 5;
    private static final int    DATA_CARD_IMAGE_DELETE_ITEM  = 6;

    private HashMap<String, ProviderInfo> mProviders = new HashMap<String, ProviderInfo>();
    private HashMap<String, LongSparseArray<DataCard>> mCards = new HashMap<String,
                                                                  LongSparseArray<DataCard>>();

    private CardContentObserver mContentObserver;
    private HandlerThread mContentObserverHandlerThread;
    private Handler mContentObserverHandler;
    private ICMHomeApiUpdateListener mApiUpdateListener;

    private Context mContext;

    public CMHomeApiManager(Context context) {
        mContext = context;
        init();
    }

    public boolean hasCard(String apiAuthority, long cardId) {
        LongSparseArray<DataCard> cards = mCards.get(apiAuthority);
        boolean hasCard = false;
        if (cards != null) {
            hasCard = cards.get(cardId) != null;
        }
        return hasCard;
    }

    public DataCard getCard(String apiAuthority, long cardId) {
        LongSparseArray<DataCard> cards = mCards.get(apiAuthority);
        DataCard card = null;
        if (cards != null) {
            card = cards.get(cardId);
        }
        return card;
    }

    public DataCard getCardWithGlobalId(String cardId) {
        String[] idParts = cardId.split("/");
        if (idParts.length > 1) {
            String apiAuthority = idParts[0];
            String idString = idParts[1];
            try {
                long id = Long.parseLong(idString);
                return getCard(apiAuthority, id);
            } catch (NumberFormatException e) {
                // This card is either malformed or not a
                // CmHome API card. Return null.
            }
        }

        return null;
    }

    public void setApiUpdateListener(ICMHomeApiUpdateListener listener) {
        mApiUpdateListener = listener;
    }

    public void init() {
        // Start up a background thread to handle any incoming changes.
        mContentObserverHandlerThread = new HandlerThread("CMHomeApiObserverThread");
        mContentObserverHandlerThread.start();
        mContentObserverHandler = new Handler(mContentObserverHandlerThread.getLooper());

        new LoadExtensionsAndCardsAsync().execute();
    }

    public void destroy() {
        mContentObserverHandlerThread.quitSafely();
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
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
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> providerPackages =
                pm.getInstalledPackages(PackageManager.GET_PROVIDERS);
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

        ContentResolver contentResolver = mContext.getContentResolver();
         for (Map.Entry<String, ProviderInfo> entry : mProviders.entrySet()) {
             ProviderInfo providerInfo = entry.getValue();
             Uri getCardsUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                         CmHomeContract.DataCard.LIST_INSERT_UPDATE_URI_PATH);
             Uri getImagesUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                          CmHomeContract.DataCardImage.LIST_INSERT_UPDATE_URI_PATH);
             contentResolver.registerContentObserver(getCardsUri,
                                                          true,
                                                          mContentObserver);
             contentResolver.registerContentObserver(getImagesUri,
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
            List<DataCard> cards = DataCard.getAllPublishedDataCards(mContext,
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
        String idString = uri.getLastPathSegment();
        long id = Long.parseLong(idString);
        LongSparseArray<DataCard> cards = mCards.get(authority);

        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri,
                                              CmHomeContract.DataCard.PROJECTION_ALL,
                                              null,
                                              null,
                                              CmHomeContract.DataCard.DATE_CREATED_COL);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            DataCard theNewCard = DataCard.createFromCurrentCursorRow(cursor, authority);
            if (cards == null) {
                cards = new LongSparseArray<DataCard>();
                cards.put(theNewCard.getId(), theNewCard);
                mCards.put(authority, cards);
            } else {
                cards.put(theNewCard.getId(), theNewCard);
            }

            // Update listeners that a card has changed.
            mApiUpdateListener.onCardInsertOrUpdate(theNewCard.getGlobalId());
        }

        cursor.close();

    }

    private void onCardDelete(Uri uri) {
        String authority = uri.getAuthority();
        LongSparseArray<DataCard> cards = mCards.get(authority);
        if (cards != null) {
            long id = Long.getLong(uri.getLastPathSegment());
            String globalId = cards.get(id).getGlobalId();
            cards.delete(id);

            // Update listeners that a card has been deleted
            mApiUpdateListener.onCardDelete(globalId);
        }
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

    public interface ICMHomeApiUpdateListener {
        public void onCardInsertOrUpdate(String globalId);
        public void onCardDelete(String globalId);
    }
}
