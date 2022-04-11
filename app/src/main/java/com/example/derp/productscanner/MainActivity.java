package com.example.derp.productscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends AppCompatActivity implements OnClickListener{

    private ImageButton scan_button;
    private String product_code;
    private String product_url = "";
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan_button = (ImageButton) findViewById(R.id.scan_btn);
        scan_button.setOnClickListener(this);

        text= (TextView) findViewById(R.id.scan_btn_text);
    }

    @Override
    public void onClick(View v){
        //Create an instance of ConnectivityManager
        ConnectivityManager cmanager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        //Get info about the network
        NetworkInfo networkInfoObj = cmanager.getActiveNetworkInfo();

        //check if connected to a network, if not it will not scan
        if(networkInfoObj!= null && networkInfoObj.isConnected() && networkInfoObj.isAvailable()){
            //Create a new IntentIntegrator and start scan
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        } else {
            Toast.makeText(this, "No network found", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //parse result
        IntentResult scan_result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scan_result != null && resultCode == RESULT_OK) {           //checks if scan was successful
            product_code = scan_result.getContents();                   //get content of the barcode
            String scan_format = scan_result.getFormatName();           //get format type of the barcode

            if (scan_format.equals("UPC_A") || scan_format.equals("UPC_E") || scan_format.equals("EAN_8") || scan_format.equals("EAN_13")) {
                //set product url
                product_url = "";
                product_url = "http://world.openfoodfacts.org/api/v0/product/" + product_code + ".json";

                Intent i = new Intent(this, ScanningDataActivity.class);
                i.putExtra("product url", product_url);
                startActivity(i);
            } else {
                handleUnsupportedScanFormat();
            }
        } else {
            Toast.makeText(this, "Could not complete scan", Toast.LENGTH_SHORT).show();
            Intent go_back = new Intent(this, MainActivity.class);
            startActivity(go_back);
        }
    }

    private void handleUnsupportedScanFormat() {
        Toast.makeText(this, "Barcode format not supported.", Toast.LENGTH_SHORT).show();
        Intent go_back = new Intent(this, MainActivity.class);
        startActivity(go_back);
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, MainActivity.class);
        startActivity(setIntent);
    }
}
