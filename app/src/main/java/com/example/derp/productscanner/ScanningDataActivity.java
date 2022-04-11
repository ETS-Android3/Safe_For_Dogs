package com.example.derp.productscanner;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;

public class ScanningDataActivity  extends Activity {

    private static String product_url = "";
    private static String product_img_url = "";
    private static String product_name = "";
    private static boolean product_exist = false;
    private static boolean product_safe = true;

    private static boolean connected;

    private static ArrayList<String> list_of_ingredients;
    private static List<String> list_of_bad_ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning_data);

        list_of_ingredients = new ArrayList<>();
        list_of_bad_ingredients = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        product_url = b.getString("product url");

        load_bad_ingredients();
        new GetData().execute(product_url);
    }

    public class GetData extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // reset
            product_url = "";
            product_img_url = "";
            product_name = "";
            product_exist = false;
            product_safe = true;
        }

        @Override
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            String line;

            try {
                connected = true;
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                connected = false;
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            check_connection();
            if(connected){
                //get the json of the product and all the info
                find_product_info(result);
            }
        }
    }
    //check if the connection was established correctly
    private void check_connection(){
        if(!connected){
            Toast.makeText(this, "Cannot establish connection", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, MainActivity.class);
        startActivity(setIntent);
    }

    private void handle_no_ingredients() {
        Toast.makeText(this, "Product does not have ingredient information to determine safety.", Toast.LENGTH_SHORT).show();
        Intent in = new Intent(this, MainActivity.class);                                   //returns back to main activity if not found
        startActivity(in);
    }

    private void go_to_result(){
        Intent i = new Intent(this, ResultActivity.class);
        i.putStringArrayListExtra("list of ingredients", list_of_ingredients);
        i.putExtra("product name", product_name);
        i.putExtra("product image url", product_img_url);
        i.putExtra("product safe", product_safe);
        startActivity(i);
    }

    private void find_product_info(String result) {
        try {
            if (product_found(result)) {
                parse_data(result);
            } else {
                Toast.makeText(this, "Product information cannot be not found or obtained.", Toast.LENGTH_SHORT).show();
                Intent in = new Intent(this, MainActivity.class);                                   //returns back to main activity if not found
                startActivity(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse_data(String result) {
        try {
            JSONObject json = (JSONObject) new JSONTokener(result).nextValue();

            if (!json.isNull("product")) {
                JSONObject product_json = json.getJSONObject("product");
                if (!product_json.isNull("ingredients_text")) {
                    String ingre = product_json.getString("ingredients_text");

                    list_of_ingredients = new ArrayList<>(Arrays.asList(ingre.split(",")));

                    product_name = (String) product_json.get("product_name");                            //get product name
                    product_img_url = "";
                    product_img_url = (String) product_json.get("image_front_url");                         //get product image url

                    //load ingredients of the product and check if safe if the product is found
                    if (product_exist) {
                        check_if_safe_ingredients();
                        go_to_result();
                    }
                } else {
                    handle_no_ingredients();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean product_found(String result) {
        product_exist = false;
        try {
            JSONObject json = (JSONObject) new JSONTokener(result).nextValue();
            String outcome = (String) json.get("status_verbose");
            if (outcome.equals("product found")) {
                product_exist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return product_exist;
    }

    private void load_bad_ingredients() {
        Resources res = getResources();
        InputStream in = res.openRawResource(R.raw.bad_ingredients);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String read_line = null;

        try {                                                                                        //get call the bad ingredients from txt file and load into an array list (list of bad ingredients)
            while ((read_line = br.readLine()) != null) {
                list_of_bad_ingredients.add(read_line.toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void check_if_safe_ingredients() {                                                       //find if exist an ingredient in the bad ingredient list
        product_safe = true;

        for (int i = 0; i < list_of_ingredients.size(); i++) {
            for (int k = 0; k < list_of_bad_ingredients.size(); k++) {
                if (list_of_ingredients.get(i).contains(list_of_bad_ingredients.get(k))) {
                    product_safe = false;
                    break;
                }
            }
            //stop looping if the product is not safe
            if (!product_safe) {
                break;
            }
        }
    }
}