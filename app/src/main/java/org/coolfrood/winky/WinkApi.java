package org.coolfrood.winky;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WinkApi {

    private static final String BASE_URL = "https://winkapi.quirky.com";
    private static final String TAG = "WinkApi";

    private SharedPreferences prefs;
    private final String clientId;
    private final String clientSecret;



    public WinkApi(Context ctx) {
        prefs = ctx.getSharedPreferences("prefs", 0);
        accessToken = prefs.getString("access_token", null);
        refreshToken = prefs.getString("refresh_token", null);
        clientId = ctx.getResources().getString(R.string.client_id);
        clientSecret = ctx.getResources().getString(R.string.client_secret);
    }
    private String accessToken = null;
    private String refreshToken = null;

    private static class HTTPException extends Exception {
        int code;
        HTTPException(int code) { this.code = code; }
        @Override public String toString() { return "HTTPException(" + code + ")"; }
    }

    private JSONObject doGet(String endPoint) throws IOException, HTTPException {
        try {
            URL url = new URL(BASE_URL + endPoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoInput(true);
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestMethod("GET");
                int result = conn.getResponseCode();
                if (result == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String output;
                    StringBuilder builder = new StringBuilder();
                    while ((output = br.readLine()) != null) {
                        builder.append(output + '\n');
                    }
                    br.close();
                    //System.out.println("get result=" + builder.toString());
                    return new JSONObject(builder.toString());
                } else {
                    throw new HTTPException(result);
                }

            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (JSONException e) {
            throw new IOException("Invalid JSON received " + e.getMessage());
        }
    }

    private JSONObject doPut(String endpoint, JSONObject params) throws IOException {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestMethod("PUT");
                byte[] body = params.toString().getBytes("UTF-8");

                conn.setRequestProperty("Content-Length", Integer.toString(body.length));
                conn.connect();
                OutputStream os = conn.getOutputStream();
                os.write(body);
                os.close();
                int result = conn.getResponseCode();
                if (result == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String output;
                    StringBuilder builder = new StringBuilder();
                    while ((output = br.readLine()) != null) {
                        builder.append(output + '\n');
                    }
                    br.close();
                    System.out.println("ret = " + builder.toString());
                    return new JSONObject(builder.toString());
                } else {
                    throw new IOException("HTTP Error: " + result);
                }

            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (JSONException e) {
            throw new IOException("Invalid JSON received " + e.getMessage());
        }
    }

    private JSONObject doPost(String endpoint, JSONObject params) throws IOException {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                byte[] body = params.toString().getBytes("UTF-8");

                conn.setRequestProperty("Content-Length", Integer.toString(body.length));
                conn.connect();
                OutputStream os = conn.getOutputStream();
                os.write(body);
                os.close();
                int result = conn.getResponseCode();
                if (result == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String output;
                    StringBuilder builder = new StringBuilder();
                    while ((output = br.readLine()) != null) {
                        builder.append(output + '\n');
                    }
                    br.close();
                    //
                    // System.out.println("ret = " + builder.toString());
                    return new JSONObject(builder.toString());
                } else {
                    throw new IOException("HTTP Error: " + result);
                }

            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (JSONException e) {
            throw new IOException("Invalid JSON received " + e.getMessage());
        }

    }


    boolean isLoggedIn() {
        return accessToken != null;
    }

    boolean login(String username, String password) {
        if (accessToken != null)
            return true;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("username", username);
            params.put("password", password);
            params.put("grant_type", "password");
            JSONObject obj = new JSONObject(params);
            JSONObject response = doPost("/oauth2/token", obj);
            saveTokens(response);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred while login: " + e);
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse login response correctly " + e);
            return false;
        }
    }

    private void saveTokens(JSONObject response) throws JSONException {
        accessToken = response.getJSONObject("data").getString("access_token");
        refreshToken = response.getJSONObject("data").getString("refresh_token");
        Log.d(TAG, "Obtained access token " + accessToken);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", accessToken);
        editor.putString("refresh_token", refreshToken);
        editor.commit();
    }

    private void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        accessToken = null;
        editor.remove("access_token");
        refreshToken = null;
        editor.remove("refresh_token");
        editor.commit();
    }

    boolean verifyLogin() {
        try {
            JSONObject resp = doGet("/users/me/wink_devices");
            return true;
        } catch (HTTPException e) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", clientId);
                params.put("client_secret", clientSecret);
                params.put("refresh_token", refreshToken);
                params.put("grant_type", "refresh_token");
                JSONObject obj = new JSONObject(params);
                JSONObject resp = doPost("/oauth2/token", obj);
                saveTokens(resp);
                return true;
            } catch (IOException ioe) {
                Log.e(TAG, "Exception while refreshing token " + ioe);
                clearTokens();
                return false;
            } catch (JSONException je) {
                clearTokens();
                return false;
            }
        } catch (IOException e) {
            clearTokens();
            return false;
        }
    }

    boolean changeBulbState(Bulb bulb, boolean powered) {
        try {

            JSONObject obj = new JSONObject();
            JSONObject desiredState = new JSONObject();
            desiredState.put("powered", powered);
            obj.put("desired_state", desiredState);
            doPut("/light_bulbs/" + bulb.id, obj);
            bulb.powered = powered;
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Could not toggle bulb state");
            return false;
        }

    }

    List<Bulb> getBulbs() {
        List<Bulb> bulbs = new ArrayList<>();
        try {
            JSONObject resp = doGet("/users/me/light_bulbs");
            JSONArray devices = resp.getJSONArray("data");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                bulbs.add(new Bulb(Integer.parseInt(device.getString("light_bulb_id")),
                        device.getString("name"),
                        device.getJSONObject("last_reading").getBoolean("powered")));
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not get light bulbs " + e);
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse light_bulbs response correctly " + e);
        } catch (HTTPException e) {
            Log.e(TAG, "HTTP error while getting light bulbs " + e);
        }
        System.out.println("Found " + bulbs.size() + " bulbs");
        return bulbs;
    }


}
