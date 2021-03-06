package knf.animeflv.zippy;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import knf.animeflv.DownloadManager.CookieConstructor;
import knf.animeflv.JsonFactory.ServerGetter;
import knf.animeflv.Utils.NoLogInterface;

public class zippyHelper {
    public static void calculate(final String u, final OnZippyResult callback) {
        AsyncHttpClient client = ServerGetter.getClient();
        client.setLogInterface(new NoLogInterface());
        client.get(u, null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("Zippy Calculate", "Error - Status: " + statusCode + " Response: " + responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String cookies = null;
                for (Header header : headers) {
                    if (header.getName().equals("Set-Cookie") && header.getValue().contains("JSESSIONID")) {
                        cookies = header.getValue();
                        break;
                    }
                }
                if (cookies != null) {
                    try {
                        String url = URLDecoder.decode(u, "utf-8");
                        Document document = Jsoup.parse(responseString);
                        Element center = document.select("div.center").first();
                        Element script = center.select("script").get(1);
                        String script_text = script.outerHtml().replace("<script type=\"text/javascript\">", "");

                        String nums = script_text.substring(script_text.indexOf("/d/") + 3, script_text.indexOf(";"));
                        String result = nums.substring(nums.indexOf("(") + 1, nums.lastIndexOf(")")).replace(" ", "");
                        String[] both = result.replace("+", "/").split("/");
                        String[] ab = both[0].split("%");
                        String[] ac = both[1].split("%");

                        int a = Integer.parseInt(ab[0]);
                        int b = Integer.parseInt(ab[1]);
                        int c = Integer.parseInt(ac[1]);
                        String pre = script_text.substring(script_text.indexOf("/d/") + 3, script_text.indexOf("/\""));
                        String d_url = url.substring(0, url.indexOf("/v/")) + "/d/" + pre + "/" + generateNumber(a, b, c) + "/" + script_text.substring(script_text.indexOf("+ \"/") + 4, script_text.indexOf(".mp4\";")) + ".mp4";
                        Log.e("Zippy Download", d_url);
                        callback.onSuccess(new zippyObject(d_url, new CookieConstructor(cookies, System.getProperty("http.agent"), url)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError();
                    }
                } else {
                    callback.onError();
                }
            }
        });
    }

    private static int generateNumber(int a, int b, int c) {
        return ((int) ((a % b) + (a % c)));
    }

    public interface OnZippyResult {
        void onSuccess(zippyObject object);

        void onError();
    }

    public static class zippyObject {
        public String download_url;
        public CookieConstructor cookieConstructor;

        public zippyObject(String url, CookieConstructor cookieConstructor) {
            this.download_url = url;
            this.cookieConstructor = cookieConstructor;
        }
    }
}
