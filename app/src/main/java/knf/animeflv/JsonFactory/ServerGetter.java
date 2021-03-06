package knf.animeflv.JsonFactory;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;
import knf.animeflv.Directorio.DB.DirectoryHelper;
import knf.animeflv.JsonFactory.JsonTypes.ANIME;
import knf.animeflv.JsonFactory.JsonTypes.DOWNLOAD;
import knf.animeflv.JsonFactory.JsonTypes.INICIO;
import knf.animeflv.Parser;
import knf.animeflv.TaskType;
import knf.animeflv.Utils.NetworkUtils;
import knf.animeflv.Utils.NoLogInterface;


public class ServerGetter {
    private static int TIMEOUT = 3000;

    private static String getInicio(Context context) {
        return new Parser().getInicioUrl(TaskType.NORMAL, context);
    }

    private static String getDirectorio(Context context) {
        return new Parser().getDirectorioUrl(TaskType.NORMAL, context);
    }

    private static String getAnime(Context context, ANIME anime) {
        return new Parser().getInicioUrl(TaskType.NORMAL, context) + "?url=" + DirectoryHelper.get(context).getAnimeUrl(anime.getAidString()) + "&certificate=" + Parser.getCertificateSHA1Fingerprint(context);
    }

    public static AsyncHttpClient getClient() {
        if (Looper.myLooper() == null) {
            return new SyncHttpClient();
        } else {
            return new AsyncHttpClient();
        }
    }

    public static void getInicio(final Context context, final INICIO inicio, final BaseGetter.AsyncInterface asyncInterface) {
        AsyncHttpClient asyncHttpClient = getClient();
        asyncHttpClient.setLogInterface(new NoLogInterface());
        asyncHttpClient.setLoggingEnabled(false);
        asyncHttpClient.setResponseTimeout(TIMEOUT);
        asyncHttpClient.get(getInicio(context) + "?certificate=" + Parser.getCertificateSHA1Fingerprint(context), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                if (inicio.type == 0)
                    OfflineGetter.backupJson(response, OfflineGetter.inicio);
                asyncInterface.onFinish(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                SelfGetter.getInicio(context, inicio, asyncInterface);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                SelfGetter.getInicio(context, inicio, asyncInterface);
            }
        });
    }

    public static void getDir(final Context context, final BaseGetter.AsyncProgressInterface asyncInterface) {
        AsyncHttpClient asyncHttpClient = getClient();
        asyncHttpClient.setLogInterface(new NoLogInterface());
        asyncHttpClient.setLoggingEnabled(false);
        asyncHttpClient.setResponseTimeout(TIMEOUT);
        asyncHttpClient.get(getDirectorio(context) + "?certificate=" + Parser.getCertificateSHA1Fingerprint(context), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                OfflineGetter.backupJson(response, OfflineGetter.directorio);
                asyncInterface.onFinish(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                SelfGetter.getDir(context, asyncInterface);
            }
        });
    }

    public static void getAnime(final Context context, final ANIME anime, final BaseGetter.AsyncInterface asyncInterface) {
        AsyncHttpClient asyncHttpClient = getClient();
        asyncHttpClient.setLogInterface(new NoLogInterface());
        asyncHttpClient.setLoggingEnabled(false);
        asyncHttpClient.setResponseTimeout(TIMEOUT);
        asyncHttpClient.get(getAnime(context, anime), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                OfflineGetter.backupJson(response, new File(OfflineGetter.animecache, anime.getAidString() + ".txt"));
                asyncInterface.onFinish(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                SelfGetter.getAnime(context, anime, asyncInterface);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                SelfGetter.getAnime(context, anime, asyncInterface);
            }
        });
    }

    public static void getDownload(final Context context, final DOWNLOAD download, final BaseGetter.AsyncInterface asyncInterface) {
        AsyncHttpClient asyncHttpClient = getClient();
        asyncHttpClient.setLogInterface(new NoLogInterface());
        asyncHttpClient.setLoggingEnabled(false);
        asyncHttpClient.setResponseTimeout(TIMEOUT);
        asyncHttpClient.get(getInicio(context) + "?certificate=" + Parser.getCertificateSHA1Fingerprint(context) + "&url=" + download.url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                asyncInterface.onFinish(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                SelfGetter.getDownload(context, download.url, asyncInterface);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                SelfGetter.getDownload(context, download.url, asyncInterface);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                SelfGetter.getDownload(context, download.url, asyncInterface);
            }
        });
    }

    public static void getEmision(Context context, final BaseGetter.AsyncInterface asyncInterface) {
        AsyncHttpClient asyncHttpClient = getClient();
        asyncHttpClient.setLogInterface(new NoLogInterface());
        asyncHttpClient.setLoggingEnabled(false);
        asyncHttpClient.setResponseTimeout(TIMEOUT);
        asyncHttpClient.get(new Parser().getBaseUrl(TaskType.NORMAL, context) + "emisionlist.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                OfflineGetter.backupJson(response, OfflineGetter.emision);
                asyncInterface.onFinish(response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("Server", "GETTER FAIL", throwable);
                asyncInterface.onFinish(OfflineGetter.getEmision());
            }
        });
    }

    public static void backupDir(final Context context) {
        if (NetworkUtils.isNetworkAvailable()) {
            AsyncHttpClient asyncHttpClient = getClient();
            asyncHttpClient.setLogInterface(new NoLogInterface());
            asyncHttpClient.setLoggingEnabled(false);
            asyncHttpClient.setResponseTimeout(TIMEOUT);
            asyncHttpClient.get(getDirectorio(context) + "?certificate=" + Parser.getCertificateSHA1Fingerprint(context), null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    OfflineGetter.backupJson(response, OfflineGetter.directorio);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    SelfGetter.getDir(context, new BaseGetter.AsyncProgressInterface() {
                        @Override
                        public void onFinish(String json) {
                            try {
                                if (!json.equals("null"))
                                    OfflineGetter.backupJson(new JSONObject(json), OfflineGetter.directorio);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onProgress(int progress) {

                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                }
            });
        }
    }
}
