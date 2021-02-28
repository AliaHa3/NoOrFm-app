package com.example.nocompany.noorfmVersion2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class TrackListActivity extends ListActivity {

    private final String constPath ="https://dl.dropboxusercontent.com/u/96430851/songs/";
    private final int trackCount = 6;
    private String[] paths = new String[trackCount];
    public final String TAG ="log";
    public  ArrayAdapter<String> adapter;
    private  Context context;
    public ListView listview;
    private ProgressDialog progress ;
    private boolean success=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = TrackListActivity.this;
        listview = getListView();
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.setMessage("Loading ...");

        for (int i = 0 ;i<trackCount ;i++ ) {
            paths[i] = constPath +"Track"+ (i + 1) + ".mp3";
        }

        CheckURLs checkURLs = new CheckURLs();
        checkURLs.execute(paths);
    }

    private class CheckURLs extends AsyncTask<String,Void,ArrayList<String>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
            success =true;
        }

        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            URL url;
            HttpURLConnection huc;
            ArrayList<String>list = new ArrayList<>();
            int code = -10 ;
            int numberTrack =1;
            for (String path :urls ){
                //Cheak if valid url
                try {
                    url = new URL (path);
                    huc =  ( HttpURLConnection )  url.openConnection ();
                    code = huc.getResponseCode() ;

                } catch (Exception e) {
                    success =false;

                    e.printStackTrace();
                }

                if (code ==0){
                    success =false;
                    break;
                }
                else if (code != HttpStatus.SC_NOT_FOUND){
                    list.add("Track"+ (numberTrack));
                    numberTrack++;

                }
                else {
                    break;
                }
            }


            return  list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);
            progress.cancel();

            if(success) {
                adapter = new ArrayAdapter<String>(context, R.layout.rowlayout, R.id.label, list);
                listview.setAdapter(adapter);
            }else
            {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("This application need Internet.\nCheak Your Connection Please!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                onDestroy();
                            }
                        });
                alertDialog.show();
            }

        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) listview.getAdapter().getItem(position);
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context,MainActivity.class);
        intent.putExtra("trackNumber",position);
        intent.putExtra("trackNumbers",listview.getAdapter().getCount());
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent =new Intent(context,About.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
