package com.bookrental.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.sip.SipAudioCall;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;

public class custom_add_books extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_add_books);

        Button searchBookBtn = findViewById(R.id.btSearch);
        searchBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText BookTitleInput = findViewById(R.id.etbookTitle);

                EditText BookTitleKanaInput = findViewById(R.id.etBookTitleKana);

                if(BookTitleInput.getText().toString().isEmpty() || BookTitleKanaInput.getText().toString().isEmpty()) {
                    if (BookTitleInput.getText().toString().isEmpty()) {
                        BookTitleInput.setError("本のタイトルを入力してください.");
                    }
                    if (BookTitleKanaInput.getText().toString().isEmpty()) {
                        BookTitleKanaInput.setError("タイトルカナを入力してください.");
                    }
                    return;
                }

                final String BookTitle = BookTitleInput.getText().toString();

                final String BookTitleKana = BookTitleKanaInput.getText().toString();
                //forecasts一つ目のjsonオブジェクトからtelop文字列を取得
                //登録成功表示
                new AlertDialog.Builder(custom_add_books.this)
                        .setTitle(getString(R.string.tv_insert))
                        .setMessage(BookTitle + "\n\nを登録しますか?")
                        .setPositiveButton(getString(R.string.tv_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CreateBookTitleButtonClick(BookTitle,BookTitleKana);
                            }
                        })
                        .setNegativeButton("Cancel",null).show();
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


    public void CreateBookTitleButtonClick(String title,String titleKana){

        EditText BookTitleInput = findViewById(R.id.etbookTitle);

        EditText BookTitleKanaInput = findViewById(R.id.etBookTitleKana);

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
            BookTitleInput.setText("");

            BookTitleKanaInput.setText("");

        }finally {
            db.close();
        }
    }
}