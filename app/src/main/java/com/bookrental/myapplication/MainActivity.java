package com.bookrental.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //貸し出し処理
        ListView lvMenu = findViewById(R.id.bookStock);

        lvMenu.setOnItemClickListener(new ListItemClickListener());

        //遷移ボタン
        BootstrapButton RentalBook = findViewById(R.id.rentalBook);

        BootstrapButton addBook = findViewById(R.id.addBook);

        RentalBook.setOnClickListener(this);

        addBook.setOnClickListener(this);

        EditText searchWord = findViewById(R.id.searchBookStock);

        searchWord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ){
                    searchBookStock(textView.getText().toString());
                }
                return false;
            }
        });

        //登録本表示
        showBookStock();
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?>parent,View view,int position,long id){


            Map<String,String> bookDesp = (Map<String, String>) parent.getItemAtPosition(position);

            final String title =bookDesp.get("title");
            final String rentaDate =bookDesp.get("rentalDate");
            final String bookId = bookDesp.get("bookId");

            //レンタル中の場合
            if(!rentaDate.isEmpty()) {
                //貸し出し処理
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.tv_insert))
                        .setMessage(title + "\n\nを返却しますか?")
                        .setPositiveButton(getString(R.string.tv_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateReturn(bookId);
                                showBookStock();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }else{
                //貸し出し処理
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.tv_insert))
                        .setMessage(title + "\n\nをレンタルしますか?")
                        .setPositiveButton(getString(R.string.tv_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateRentalFlg(bookId);
                                showBookStock();
                            }

                        })
                        .setNegativeButton("Cancel", null).show();
            }
        }
    }

    //登録本表示
    public void showBookStock(){
        final List<Integer> rentalFlg_pos = new ArrayList<>();

        int i =0;
        //表示リスト
        ListView TodayTaskList = findViewById(R.id.bookStock);

        DatabaseHelper helper = new DatabaseHelper(this);

        List<Map<String,String>> showBooks = new ArrayList<>();

//データベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();
        List<String> titles = new ArrayList<>();
        try {

            String sql = "SELECT * FROM M_BOOK LIMIT 30";

            Cursor cursor = db.rawQuery(sql, null);


            while (cursor.moveToNext()) {

                Map<String,String> showBook = new HashMap<>();

                String title = cursor.getString(cursor.getColumnIndex("book_title"));
                String rentalDate = cursor.getString(cursor.getColumnIndex("rental_start_date"));
                String rentalFlg = cursor.getString(cursor.getColumnIndex("rental_flg"));
                String bookId = cursor.getString(cursor.getColumnIndex("book_id"));
                titles.add(title);

                showBook.put("title",title);

                showBook.put("bookId",bookId);

                //貸し出し中の時
                if(rentalFlg.equals("1")) {
                    rentalFlg_pos.add(i);
                    showBook.put("rentalDate","貸出日:"+rentalDate);
                }else{
                    showBook.put("rentalDate","");
                }

                showBooks.add(showBook);
                i++;
            }
        }catch(SQLiteException e){
            e.printStackTrace();
        }finally{
            db.close();
        }

        String[] from = {"title","rentalDate"};
        int [] to = {R.id.row_textview1,R.id.row_textview2};

        SimpleAdapter adapter = new SimpleAdapter(this,showBooks,R.layout.row1,from,to){
          @Override
            public View getView(int position, View convertView, ViewGroup parent){
              View view = super.getView(position,convertView,parent);

              if(rentalFlg_pos.contains(position)){
                  view.setBackgroundResource(R.color.kihada);
                  TextView tvRental = view.findViewById(R.id.row_textview);
                  tvRental.setText(R.string.book_rental);
              }
              return view;
          }
        };
        TodayTaskList.setAdapter(adapter);

    }


    //登録本検索表示
    public void searchBookStock(String searchWord){
        final List<Integer> rentalFlg_pos = new ArrayList<>();

        int i =0;
        //表示リスト
        ListView TodayTaskList = findViewById(R.id.bookStock);

        DatabaseHelper helper = new DatabaseHelper(this);

        List<Map<String,String>> showBooks = new ArrayList<>();

//データベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();
        List<String> titles = new ArrayList<>();
        try {

            String sql = "SELECT * FROM M_BOOK WHERE book_title like ? OR book_kana like ? LIMIT 50";

            Cursor cursor = db.rawQuery(sql,new String[]{'%' +searchWord+'%','%'+searchWord+'%'});


            while (cursor.moveToNext()) {

                Map<String,String> showBook = new HashMap<>();

                String title = cursor.getString(cursor.getColumnIndex("book_title"));
                String rentalDate = cursor.getString(cursor.getColumnIndex("rental_start_date"));
                String rentalFlg = cursor.getString(cursor.getColumnIndex("rental_flg"));
                titles.add(title);

                showBook.put("title",title);

                //貸し出し中の時
                if(rentalFlg.equals("1")) {
                    rentalFlg_pos.add(i);
                    showBook.put("rentalDate","貸出日:"+rentalDate);
                }else{
                    showBook.put("rentalDate","");
                }

                showBooks.add(showBook);
                i++;
            }
        }catch(SQLiteException e){
            e.printStackTrace();
        }finally{
            db.close();
        }

        String[] from = {"title","rentalDate"};
        int [] to = {R.id.row_textview1,R.id.row_textview2};

        SimpleAdapter adapter = new SimpleAdapter(this,showBooks,R.layout.row1,from,to){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position,convertView,parent);

                if(rentalFlg_pos.contains(position)){
                    view.setBackgroundResource(R.color.kihada);
                    TextView tvRental = view.findViewById(R.id.row_textview);
                    tvRental.setText(R.string.book_rental);
                }
                return view;
            }
        };
        TodayTaskList.setAdapter(adapter);

    }

    //レンタル処理
    public void updateRentalFlg(String bookId){

        DatabaseHelper helper = new DatabaseHelper(this);
//データベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();
        try {

            String sql = "UPDATE M_BOOK SET rental_flg = 1 WHERE book_id = ?";

            SQLiteStatement stmt = db.compileStatement(sql);

            stmt.bindString(1,bookId);

            stmt.executeUpdateDelete();

            //成功表示
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.rental))
                    .setMessage(getString(R.string.tv_rentaled))
                    .setPositiveButton(getString(R.string.tv_yes), null)
                    .create().show();

        }catch(SQLiteException e){
            e.printStackTrace();
        }finally{
            db.close();
        }

    }

    //返却処理
    public void updateReturn(String bookId){

        DatabaseHelper helper = new DatabaseHelper(this);
//データベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();
        try {

            String sql = "UPDATE M_BOOK SET rental_flg = 0 WHERE book_id = ?";

            SQLiteStatement stmt = db.compileStatement(sql);

            stmt.bindString(1,bookId);

            stmt.executeUpdateDelete();

            //成功表示
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.rental))
                    .setMessage(getString(R.string.tv_return))
                    .setPositiveButton(getString(R.string.tv_yes), null)
                    .create().show();

        }catch(SQLiteException e){
            e.printStackTrace();
        }finally{
            db.close();
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_delete_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int ItemId = item.getItemId();
        switch(ItemId){
            case R.id.delete_menu:
                Intent DeleteIntent = new Intent(this,DeleteBookActivity.class);
                startActivity(DeleteIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}