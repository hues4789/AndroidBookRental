package com.bookrental.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.sip.SipAudioCall;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddBookActivity extends AppCompatActivity implements View.OnClickListener{
    private BooksInfoReceiver bookInfoReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Button searchBookBtn = findViewById(R.id.btSearch);
        searchBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edISBN = findViewById(R.id.etISBN);
                bookInfoReceiver = new BooksInfoReceiver();
                bookInfoReceiver.execute(edISBN.getText().toString());

                //★★★ ソフトキーボードを隠す。
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        //遷移ボタン
        BootstrapButton RentalBook = findViewById(R.id.rentalBook);

        BootstrapButton addBook = findViewById(R.id.addBook);

        RentalBook.setOnClickListener(this);

        addBook.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v !=null){
            switch(v.getId()){

                case R.id.rentalBook:
                    Intent RandomIntent = new Intent(this,MainActivity.class);
                    startActivity(RandomIntent);
                    break;

                case R.id.addBook:
                    Intent AddBook = new Intent(this,AddBookActivity.class);
                    startActivity(AddBook);
                    break;

                default:
                    break;
            }
        }

    }

    private class BooksInfoReceiver extends AsyncTask<String, String, String> {

        private TextView _tvWeatherTelop;

        private TextView _tvWeatherDesc;

        private SipAudioCall.Listener listener;


        @Override
        public String doInBackground(String... params) {
            String id = params[0];
            String urlStr = "https://api.openbd.jp/v1/get?isbn=" + id;
            String result = "";

            HttpURLConnection con = null;

            InputStream is = null;
            try {
                //URLオブジェクトを生成
                URL url = new URL(urlStr);
                //urlからhttpUrlConnectionオブジェクトを取得
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                //接続
                con.connect();
                //レスポンスデータを取得
                is = con.getInputStream();
                //レスポンスデータを文字列に変換
                result = is2String(is);
            } catch (MalformedURLException ex) {

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return result;
        }

        private String is2String(InputStream is) throws IOException {
            //textファイルを読み込んでくれるAPI
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }

        @Override
        public void onPostExecute(String result) {
            String telop = "";

            try {
                if(result.equals("[null]")){
                    //ISBN検索失敗時
                    new AlertDialog.Builder(AddBookActivity.this)
                            .setTitle(getString(R.string.tv_insert))
                            .setMessage("本のタイトルを取得できませんでした。\nカスタマイズ登録から本の登録を行ってください。")
                            .setPositiveButton(getString(R.string.tv_yes),null).show();
                    return;
                }
                //JSON文字列からJSONObjectオブジェクトを生成。
                JSONArray rootJSON = new JSONArray(result);
                //ルートJSON直下のJSONオブジェクトを取得
                final String desc = rootJSON.getJSONObject(0).getJSONObject("onix").getJSONObject("DescriptiveDetail").getJSONObject("TitleDetail").getJSONObject("TitleElement").getJSONObject("TitleText").getString("content");
                final String descKana = rootJSON.getJSONObject(0).getJSONObject("onix").getJSONObject("DescriptiveDetail").getJSONObject("TitleDetail").getJSONObject("TitleElement").getJSONObject("TitleText").getString("collationkey");

                //forecasts一つ目のjsonオブジェクトからtelop文字列を取得
                //登録成功表示
                new AlertDialog.Builder(AddBookActivity.this)
                        .setTitle(getString(R.string.tv_insert))
                        .setMessage(desc + "\n\nを登録しますか?")
                        .setPositiveButton(getString(R.string.tv_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CreateBookTitleButtonClick(desc,descKana);
                            }

                        })
                        .setNegativeButton("Cancel",null).show();

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

        }

        void setListener(SipAudioCall.Listener listener) {
            this.listener = listener;
        }
    }

    public void CreateBookTitleButtonClick(String title,String titleKana){

        EditText ISBNInput = findViewById(R.id.etISBN);

        //データベースヘルパーを取得
        DatabaseHelper helper = new DatabaseHelper(this);

        //データベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();

        try{

            String sqlInsert = "INSERT INTO M_BOOK(book_title,book_kana,rental_start_date)VALUES (?,?,?)";
            SQLiteStatement stmt = db.compileStatement(sqlInsert);

            Date date = new Date();

            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy/MM/dd");

            String fdate = sdformat.format(date);

            stmt.bindString(1,title);
            stmt.bindString(2,titleKana);
            stmt.bindString(3,fdate);


            stmt.executeInsert();

            //登録成功表示
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.tv_insert))
                    .setMessage(getString(R.string.tv_inserted))
                    .setPositiveButton(getString(R.string.tv_yes), null)
                    .create().show();

            //設定に成功した場合、入力textを空に
            ISBNInput.setText("");

        }finally {
            db.close();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_menu_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int ItemId = item.getItemId();
        switch(ItemId){
            case R.id.custom_add_menu:
                Intent HistoryIntent = new Intent(this,custom_add_books.class);
                startActivity(HistoryIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}