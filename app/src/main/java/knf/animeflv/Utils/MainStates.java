package knf.animeflv.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import knf.animeflv.Utils.eNums.DownloadTask;
import knf.animeflv.WaitList.WaitDBHelper;
import pl.droidsonroids.gif.GifImageButton;

/**
 * Created by Jordy on 26/03/2016.
 */
public class MainStates {
    private static MainStates states;
    private static SharedPreferences preferences;
    private static boolean listing = false;
    private static boolean isprocessing = false;
    private static DownloadTask task;
    private static String UrlZippy;
    private static ImageButton imageButton;
    private static GifImageButton gifImageButton;
    private static ImageButton downState;
    private static String processingEid = "";
    private static boolean loadingEmision = true;
    private static int position = -1;
    private static Context context;

    public MainStates(Context context) {
        preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        MainStates.context = context;
    }

    public static MainStates init(Context con) {
        if (states == null) {
            states = new MainStates(con);
        }
        return states;
    }

    public static void setZippyState(DownloadTask task1, String url, ImageButton button, ImageButton state, int position) {
        task = task1;
        UrlZippy = url;
        imageButton = button;
        downState = state;
        MainStates.position = position;
    }

    public static void setZippyState(DownloadTask task1, String url, GifImageButton button, ImageButton state, int position) {
        task = task1;
        UrlZippy = url;
        gifImageButton = button;
        downState = state;
        MainStates.position = position;
    }

    public static int getPosition() {
        return position;
    }

    public static boolean isLoadingEmision() {
        return loadingEmision;
    }

    public static void setLoadingEmision(boolean loadingEmision) {
        MainStates.loadingEmision = loadingEmision;
    }

    public static ImageButton getDownStateButton() {
        return downState;
    }

    public static GifImageButton getGifDownButton() {
        return gifImageButton;
    }

    public static String getUrlZippy() {
        return UrlZippy;
    }

    public static DownloadTask getDowloadTask() {
        return task;
    }

    public static boolean isListing() {
        return listing;
    }

    public static void setListing(boolean value) {
        listing = value;
    }

    public static void setProcessing(boolean value, @Nullable String eid) {
        isprocessing = value;
        if (eid != null) {
            processingEid = eid;
        } else {
            processingEid = "null";
        }
    }

    public static String getProcessingEid() {
        return processingEid;
    }

    public static boolean isProcessing() {
        return isprocessing;
    }

    public List<String> getGlobalWaitList() {
        Set<String> set = preferences.getStringSet("GlobalWaiting", new HashSet<String>());
        List<String> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    public List<String> getWaitList(String aid) {
        Set<String> set = preferences.getStringSet(aid + "waiting", new HashSet<String>());
        List<String> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    public void UpdateWaitList(String key, List<WaitDBHelper.WaitObject> list) {
        Set<String> set = new HashSet<>();
        for (WaitDBHelper.WaitObject object : list) {
            set.add(object.aid);
        }
        preferences.edit().putStringSet(key, set).apply();
    }

    public void UpdateWaitList(String key, List<String> list, @Nullable String i) {
        Set<String> set = new HashSet<>();
        set.addAll(list);
        preferences.edit().putStringSet(key, set).apply();
    }

    public void addToWaitList(String eid) {
        try {
            String[] data = eid.replace("E", "").split("_");
            String key = data[0] + "waiting";
            Set<String> set = preferences.getStringSet(key, new HashSet<String>());
            List<String> list = new ArrayList<>();
            list.addAll(set);
            if (!list.contains(data[1])) {
                list.add(data[1]);
                UpdateWaitList(key, list, null);
            }
            set = preferences.getStringSet("GlobalWaiting", new HashSet<String>());
            list.clear();
            list.addAll(set);
            if (!list.contains(data[0])) {
                list.add(data[0]);
                UpdateWaitList("GlobalWaiting", list, null);
            }
            new WaitDBHelper(context).addToList(eid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delFromWaitList(String eid) {
        try {
            String[] data = eid.replace("E", "").split("_");
            String key = data[0] + "waiting";
            Set<String> set = preferences.getStringSet(key, new HashSet<String>());
            List<String> list = new ArrayList<>();
            list.addAll(set);
            if (list.contains(data[1])) {
                list.remove(list.indexOf(data[1]));
                UpdateWaitList(key, list, null);
            }
            if (list.isEmpty()) {
                set = preferences.getStringSet("GlobalWaiting", new HashSet<String>());
                list.clear();
                list.addAll(set);
                list.remove(list.indexOf(data[0]));
                UpdateWaitList("GlobalWaiting", list, null);
            }
            new WaitDBHelper(context).delFromList(eid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delFromGlobalWaitList(String aid) {
        Set<String> set = preferences.getStringSet("GlobalWaiting", new HashSet<String>());
        List<String> list = new ArrayList<>();
        list.addAll(set);
        if (list.contains(aid)) {
            list.remove(list.indexOf(aid));
            UpdateWaitList("GlobalWaiting", list, null);
            list.clear();
            UpdateWaitList(aid + "waiting", list, null);
        }
    }

    public boolean WaitContains(String eid) {
        String[] data = eid.replace("E", "").split("_");
        return getGlobalWaitList().contains(data[0]) && getWaitList(data[0]).contains(data[1]);
    }
}
