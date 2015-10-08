package knf.animeflv.info;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import knf.animeflv.LoginServer;
import knf.animeflv.Main;
import knf.animeflv.Parser;
import knf.animeflv.R;
import knf.animeflv.Requests;
import knf.animeflv.TaskType;

/**
 * Created by Jordy on 12/08/2015.
 */
public class Info extends AppCompatActivity implements Requests.callback{
    Parser parser=new Parser();
    Toolbar toolbar;
    String ext_storage_state = Environment.getExternalStorageState();
    File mediaStorage = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache");
    Boolean favBoolean=false;
    Menu Amenu;
    String aid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        if (!isXLargeScreen(getApplicationContext())) { //set phones to portrait;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.dark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.prim));
        }
        toolbar=(Toolbar) findViewById(R.id.info_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.blanco), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SharedPreferences sharedPreferences=getSharedPreferences("data", Context.MODE_PRIVATE);
        aid=sharedPreferences.getString("aid", "");
        Log.d("Base Aid",aid);
        if (ext_storage_state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            if (!mediaStorage.exists()) {
                mediaStorage.mkdirs();
            }
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/Animeflv/cache/"+aid+".txt");
        String file_loc = Environment.getExternalStorageDirectory() + "/Animeflv/cache/"+aid+".txt";
        if (file.exists()) {
            Log.d("Archivo", "Existe");
            String infile = getStringFromFile(file_loc);
            SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putString("titInfo",parser.getTit(infile)).commit();
            getSupportActionBar().setTitle(parser.getTit(infile));
        }else {
            new Requests(this, TaskType.GET_INFO).execute("http://animeflv.net/api.php?accion=anime&aid=" + aid);
        }
        Bundle bundle=new Bundle();
        bundle.putString("aid", getIntent().getExtras().getString("aid", "1"));
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("INFORMACION", AnimeInfo.class,bundle)
                .add("EPISODIOS", InfoCap.class,bundle)
                .create());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager1);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab1);
        viewPagerTab.setViewPager(viewPager);
    }
    public void toast(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Amenu=menu;
        SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        String fav=sharedPreferences.getString("favoritos", "");
        String[] favoritos={};
        favoritos=fav.split(":::");
        String tit=getSharedPreferences("data",MODE_PRIVATE).getString("titInfo","");
        Boolean isfav=false;
        for (String favo:favoritos){
            if (!favo.equals("")) {
                if (Integer.parseInt(favo) == Integer.parseInt(aid)) {
                    getMenuInflater().inflate(R.menu.menu_fav_si, menu);
                    isfav=true;
                    break;
                }
            }
        }
        if (isfav){
            Amenu.clear();
            getMenuInflater().inflate(R.menu.menu_fav_si, menu);
        }else {
            Amenu.clear();
            getMenuInflater().inflate(R.menu.menu_fav_no, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String email_coded=PreferenceManager.getDefaultSharedPreferences(this).getString("login_email_coded", "null");
        String pass_coded=PreferenceManager.getDefaultSharedPreferences(this).getString("login_pass_coded", "null");
        switch (item.getItemId()){
            case R.id.favorito_si:
                SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
                String fav=sharedPreferences.getString("favoritos", "");
                String[] favoritos={};
                favoritos=fav.split(":::");
                List<String> list=new ArrayList<String>();
                for (String i:favoritos){
                    if (!i.equals("")) {
                        if (Integer.parseInt(i) != Integer.parseInt(aid)) {
                            list.add(i);
                        }
                    }
                }
                favoritos=new String[list.size()];
                list.toArray(favoritos);
                StringBuilder builder = new StringBuilder();
                for(String i : favoritos)
                {
                    builder.append(":::" + i);
                }
                toast("Favorito Eliminado");
                getSharedPreferences("data",MODE_PRIVATE).edit().putString("favoritos",builder.toString()).commit();
                if (!email_coded.equals("null")&&!email_coded.equals("null")) {
                    new LoginServer(this, TaskType.GET_FAV_SL, null, null, null, null).execute("http://necrotic-neganebulus.hol.es/fav-server.php?tipo=refresh&email_coded=" + email_coded + "&pass_coded=" + pass_coded + "&new_favs=" + builder.toString());
                }
                Amenu.clear();
                getMenuInflater().inflate(R.menu.menu_fav_no,Amenu);
                break;
            case R.id.favorito_no:
                String[] favoritosNo={getSharedPreferences("data",MODE_PRIVATE).getString("favoritos","")};
                String titNo=getSharedPreferences("data",MODE_PRIVATE).getString("aid","");
                List<String> Listno = new ArrayList<String>(Arrays.asList(favoritosNo));
                Listno.add(aid);
                favoritos=new String[Listno.size()];
                Listno.toArray(favoritos);
                StringBuilder builderNo = new StringBuilder();
                for(String i : favoritos)
                {
                    builderNo.append(":::" + i);
                }
                toast("Favorito Agregado");
                getSharedPreferences("data",MODE_PRIVATE).edit().putString("favoritos",builderNo.toString()).commit();
                if (!email_coded.equals("null")&&!email_coded.equals("null")) {
                    new LoginServer(this, TaskType.GET_FAV_SL, null, null, null, null).execute("http://necrotic-neganebulus.hol.es/fav-server.php?tipo=refresh&email_coded=" + email_coded + "&pass_coded=" + pass_coded + "&new_favs=" + builderNo.toString());
                }
                Amenu.clear();
                getMenuInflater().inflate(R.menu.menu_fav_si, Amenu);
                break;
        }
        return true;
    }

    @Override
    public void sendtext1(String data,TaskType taskType) {
        SharedPreferences.Editor editor=getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("titInfo",parser.getTit(data)).commit();
        getSupportActionBar().setTitle(parser.getTit(data));
    }
    public static boolean isXLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (!isXLargeScreen(getApplicationContext()) ) {
            return;
        }
    }
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    public static String getStringFromFile (String filePath) {
        String ret="";
        try {
            File fl = new File(filePath);
            FileInputStream fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
            fin.close();
        }catch (IOException e){}catch (Exception e){}
        return ret;
    }
}
