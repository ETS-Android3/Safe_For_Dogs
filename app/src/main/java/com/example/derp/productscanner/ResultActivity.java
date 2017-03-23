package com.example.derp.productscanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;

import java.io.InputStream;
import java.util.ArrayList;

import android.os.AsyncTask;

public class ResultActivity extends Activity implements View.OnClickListener {

    private static String product_img_url = "";
    private static boolean product_safe = true;
    private static String product_name = "";

    private static ArrayList<String> list_of_ingredients;

    private ArrayAdapter<String> adapter;

    private ImageView result_header;
    private ImageView product_img;
    private TextView product_name_txt;

    private LinearLayout l_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //get all the data from extra
        Bundle b = getIntent().getExtras();
        product_img_url = b.getString("product image url");
        product_safe = b.getBoolean("product safe");
        product_name = b.getString("product name");
        list_of_ingredients = b.getStringArrayList("list of ingredients");

        //initializing and putting into layout
        l_layout = (LinearLayout) findViewById(R.id.linear_lay);
        result_header = (ImageView) findViewById(R.id.result_img_view);                             //result header image
        if(product_safe){
            result_header.setImageResource(R.drawable.safe);
            l_layout.setBackgroundResource(R.drawable.bg2);
        } else {
            result_header.setImageResource(R.drawable.not_safe);
            l_layout.setBackgroundResource(R.drawable.bg3);
        }

        product_img = (ImageView) findViewById(R.id.product_img_view);
        product_name_txt = (TextView) findViewById(R.id.product_name_txt_view);                     //product name
        product_name_txt.setText(product_name);
        ListView ingredients_list_view = (ListView) findViewById(R.id.list_of_ingredients_view);

        adapter = new ArrayAdapter<String>(                                                         //list of ingredients
                this,
                android.R.layout.simple_list_item_1,
                list_of_ingredients
        );
        ingredients_list_view.setAdapter(adapter);

        new DownloadImageTask(product_img).execute(product_img_url);                                //product image
    }

    @Override
    public void onClick(View v){
        Intent i = new Intent(this, WebActivity.class);
        startActivity(i);
    }

    //return back to main activity rather than the scanning activity when back button is pressed
    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, MainActivity.class);
        startActivity(setIntent);
    }

    //download image from url
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}