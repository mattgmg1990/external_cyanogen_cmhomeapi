package org.cyanogenmod.launcher.home.cmhomeapi;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

import org.cyanogenmod.launcher.home.api.cards.DataCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMHomeApiService extends Service {
    private final static String FEED_READ_PERM = "org.cyanogenmod.launcher.home.api.FEED_READ";
    private final static String FEED_WRITE_PERM = "org.cyanogenmod.launcher.home.api.FEED_WRITE";
    private final static String DATA_CARD_URI_PATH = "datacard";
    private final static String DATA_CARD_IMAGE_URI_PATH = "datacardimage";

    private HashMap<String, ProviderInfo> mProviders = new HashMap<String, ProviderInfo>();
    private HashMap<String, List<DataCard>> mCards = new HashMap<String, List<DataCard>>();

    public CMHomeApiService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        new LoadExtensionsAndCardsAsync().execute();
    }

    @Override
    public void onDestroy() {

    }

    private class LoadExtensionsAndCardsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected
        Void doInBackground(Void... voids) {
            loadAllExtensions();
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
                        mProviders.put(packageInfo.packageName, providerInfo);
                    }
                }
            }
        }
    }

    private void loadAllCards() {
        for (Map.Entry<String, ProviderInfo> entry : mProviders.entrySet()) {
            ProviderInfo providerInfo = entry.getValue();
            Uri getCardsUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                        DATA_CARD_URI_PATH);
            Uri getImagesUri = Uri.parse("content://" + providerInfo.authority + "/" +
                                         DATA_CARD_IMAGE_URI_PATH);
            List<DataCard> cards = DataCard.getAllPublishedDataCards(this,
                                                                     getCardsUri,
                                                                     getImagesUri);
            mCards.put(entry.getKey(), cards);
        }
    }
}
