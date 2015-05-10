package org.coolfrood.winky;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class Groups extends ActionBarActivity {

    private ListView listView;
    List<Bulb> bulbs = new ArrayList<>();
    private GetDevicesTask getDevicesTask;
    private ToggleDevicesTask toggleDevicesTask;
    private ArrayAdapter<Bulb> adapter;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        listView = (ListView) findViewById(R.id.devices);
        bulbs.clear();
        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, bulbs);
        listView.setAdapter(adapter);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, intent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        toggleDevices();
    }

    private void refresh() {
        if (getDevicesTask != null)
            return;
        getDevicesTask = new GetDevicesTask();
        getDevicesTask.execute((Void) null);
    }

    private void toggleDevices() {
        if (toggleDevicesTask != null)
            return;
        toggleDevicesTask = new ToggleDevicesTask();
        toggleDevicesTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_groups, menu);
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GetDevicesTask extends AsyncTask<Void, Void, List<Bulb>> {

        @Override
        protected List<Bulb> doInBackground(Void... params) {
            return WinkyContext.getApi(getApplicationContext()).getBulbs();
        }

        @Override
        protected void onPostExecute(final List<Bulb> bulbs) {
            Groups.this.bulbs.addAll(bulbs);
            adapter.notifyDataSetChanged();
            getDevicesTask = null;

        }

    }
    public class ToggleDevicesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int numOn = 0;
            for (Bulb b: bulbs) {
                if (b.powered) numOn++;
            }
            boolean powered = false;
            if (numOn < bulbs.size()/2) {
                powered = true;
            }
            Log.d("Groups", "switching all bulbs to powered=" + powered);
            for (Bulb b: bulbs) {
                WinkyContext.getApi(getApplicationContext()).changeBulbState(b, powered);
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean res) {
            //Groups.this.bulbs.addAll(bulbs);
            adapter.notifyDataSetChanged();
            toggleDevicesTask = null;

        }

    }
}
