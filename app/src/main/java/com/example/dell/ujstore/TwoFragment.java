package com.example.dell.ujstore;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL on 17-Jun-16.
 */
public class TwoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ArrayList<String> myDataset = new ArrayList<String>();
    private ArrayList<String> StoreType = new ArrayList<String>();
    private ArrayList<String> imageUrl = new ArrayList<String>();
    private ArrayList<String> addString = new ArrayList<String>();
    private ArrayList<String> lead_id = new ArrayList<String>();
    private ArrayList<String> date = new ArrayList<String>();
    private ArrayList<String> time = new ArrayList<String>();
    private ArrayList<String> ago = new ArrayList<String>();
    private TwoAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    CoordinatorLayout coordinatorLayout;
    private String URL = "https://ujapi.herokuapp.com/api/v1/s/respond_bookings";
    SharedPreferences pref;
    int check_update=0;

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_two, container, false);
        coordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.coordinator_layout2);
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.my_recycler_view2);
        rv.setHasFixedSize(true);
        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout2);
        System.out.println("Mei");
        adapter = new TwoAdapter(getContext(), myDataset, StoreType, imageUrl, addString, date, time, ago);
        rv.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        pref = getContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                                        int x = pref.getInt("check", 0);
                                        data();
                                        adapter.notifyDataSetChanged();
                                        int a = x - check_update;
                                        if (a != 0) {
                                            Snackbar.make(coordinatorLayout, "Updated List:" + a, Snackbar.LENGTH_SHORT).show();
                                            System.out.println("Snackbar");
                                            check_update = x;
                                        } else {
                                            Snackbar.make(coordinatorLayout, "No Updates", Snackbar.LENGTH_SHORT).show();
                                            System.out.println("Snack");
                                        }
                                    }
                                }
        );


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                data();
                adapter.notifyDataSetChanged();
                handler.postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000);
        return rootView;
    }

    public void onRefresh() {
        pref = getContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        int x =pref.getInt("check", 0);
        data();
        adapter.notifyDataSetChanged();
        int a=x-check_update;
        if(a!=0){
            Snackbar.make(coordinatorLayout,"Updated List:"+a,Snackbar.LENGTH_SHORT).show();
        }
        else
            Snackbar.make(coordinatorLayout,"No Updates",Snackbar.LENGTH_SHORT).show();
    }


    public void data() {
        try {
            swipeRefreshLayout.setRefreshing(true);
            final String authtoken = pref.getString("token", null);
            final NetworkResponse networkResponse = null;
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                    URL,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                myDataset.clear();
                                StoreType.clear();
                                imageUrl.clear();
                                addString.clear();
                                lead_id.clear();
                                date.clear();
                                time.clear();
                                ago.clear();
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject object = response.getJSONObject(i);
                                    String booking = object.getString("booking");
                                    JSONObject object2 = new JSONObject(booking);
                                    myDataset.add("Name");
                                    // String storeid = object.getString("store_category");
                                    //  JSONObject storeobject = new JSONObject(storeid);
                                    StoreType.add("Store Type");
                                    String a1 = object2.getString("attachment");
                                    JSONObject a1obj = new JSONObject(a1);
                                    String a2 = a1obj.getString("attachment");
                                    JSONObject a3 = new JSONObject(a2);
                                    imageUrl.add(a3.getString("url"));
                                    addString.add(object2.getString("address"));
                                    lead_id.add(object2.getString("id"));
                                    date.add("Booking date: " + object2.getString("date"));
                                    time.add("Booking time: " + object2.getString("time"));
                                    ago.add(object2.getString("created_at") + " ago");
                                }
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putInt("check", response.length());
                                editor.apply();
                                adapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof NoConnectionError) {
                        Toast.makeText(getContext(), "No internet Access, Check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", authtoken);
                    return headers;
                }

                @Override
                public Request.Priority getPriority() {
                    return Request.Priority.IMMEDIATE;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            requestQueue.add(jsonArrayRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
