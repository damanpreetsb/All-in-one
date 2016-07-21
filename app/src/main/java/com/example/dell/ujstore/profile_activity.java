package com.example.dell.ujstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.apptik.widget.multiselectspinner.MultiSelectSpinner;


public class profile_activity extends AppCompatActivity {
    private static final String PUTDETAILS_URL = "https://ujapi.herokuapp.com/api/v1/s/stores";
    private EditText editTextName;
    private EditText editTextadd;
    private EditText editTextdes;
    private Button buttonnext;
    String items;
    private ArrayAdapter<String> adapter2;
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        editTextName = (EditText) findViewById(R.id.nameText);
        editTextadd = (EditText) findViewById(R.id.addText);
        editTextdes = (EditText) findViewById(R.id.desText);
        buttonnext = (Button) findViewById(R.id.btnnext);
        ArrayList<String> options = new ArrayList<>();
        options.add("Hardware Material Store");
        options.add("Electrical Material Store");
        options.add("Paint Material Store");
        options.add("Building Material Store");
        options.add("Wood & Timber Material Store");


        MultiSelectSpinner multiSelectSpinner = (MultiSelectSpinner) findViewById(R.id.multiselectSpinner);
        adapter2 = new ArrayAdapter <String>(this, android.R.layout.simple_list_item_multiple_choice, options);
        multiSelectSpinner.setListAdapter(adapter2)

                .setListener(new MultiSelectSpinner.MultiSpinnerListener() {
                    @Override
                    public void onItemsSelected(boolean[] selected) {
                        StringBuilder builder = new StringBuilder();

                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                builder.append(adapter2.getItem(i)).append(", ");
                            }
                        }
                        items = builder.toString();
                    }
                })
                .setAllUncheckedText("Store category")
                .setSelectAll(false);
        buttonnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PutDetails();
            }
        });

}
    private void PutDetails() {
        final String name = editTextName.getText().toString();
        final String address = editTextadd.getText().toString();
        final String description = editTextdes.getText().toString();
        if (name.isEmpty()) {
            editTextName.setError("Enter store name");
        } else if (address.isEmpty()) {
            editTextadd.setError("Enter your address");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences pref = getBaseContext().getSharedPreferences("MyPref", 0);
                    String id = pref.getString("id", null);
                    final String authtoken = pref.getString("token", null);
                    JSONObject js = new JSONObject();
                    try {
                        JSONObject jsonobject_one = new JSONObject();
                        JSONObject jsonobject_two = new JSONObject();


                        jsonobject_one.put("store_name", name);
                        jsonobject_one.put("address", address);
                        jsonobject_two.put("category",items);
                        jsonobject_one.put("store_category_attributes",jsonobject_two);
                        jsonobject_one.put("description", description);

                        js.put("store", jsonobject_one);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.PUT,
                            PUTDETAILS_URL+"/"+id, js,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Intent intent = new Intent(profile_activity.this, SwipeTabActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    pDialog.dismiss();
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(profile_activity.this, "Invalid Entries", Toast.LENGTH_LONG).show();
                            pDialog.dismiss();
                        }
                    }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            headers.put("Authorization", authtoken);
                            return headers;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(profile_activity.this);
                    requestQueue.add(jsonObject);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pDialog = new SweetAlertDialog(profile_activity.this, SweetAlertDialog.PROGRESS_TYPE);
                            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                            pDialog.setTitleText("Updating");
                            pDialog.setCancelable(false);
                            pDialog.show();
                        }
                    });
                }
            }).start();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
