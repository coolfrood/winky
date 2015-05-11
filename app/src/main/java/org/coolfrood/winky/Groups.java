package org.coolfrood.winky;

import android.app.Activity;
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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class Groups extends Activity {

    private ListView listView;
    private RefreshDevicesTask refreshDevicesTask;
    private ToggleDevicesTask toggleDevicesTask;
    private GetDevicesTask getDevicesTask;
    private ArrayAdapter<Bulb> adapter;
    private NfcAdapter nfcAdapter;
    private DeviceDb deviceDb;
    private TagDb tagDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        listView = (ListView) findViewById(R.id.devices);
        WinkyContext.bulbs.clear();
        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, WinkyContext.bulbs);
        listView.setAdapter(adapter);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        deviceDb = new DeviceDb(getApplicationContext());
        tagDb = new TagDb(getApplicationContext());
        getDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, intent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        toggleDevices();
    }

    private void refreshDevices() {
        if (refreshDevicesTask != null)
            return;
        refreshDevicesTask = new RefreshDevicesTask();
        refreshDevicesTask.execute((Void) null);
    }

    private void getDevices() {
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
        return super.onCreateOptionsMenu(menu);
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
        } else if (id == R.id.action_refresh) {
            refreshDevices();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch all devices from web service and update the database
     */
    public class RefreshDevicesTask extends AsyncTask<Void, Void, List<Bulb>> {

        @Override
        protected List<Bulb> doInBackground(Void... params) {
            List<Bulb> bulbs = WinkyContext.getApi(getApplicationContext()).getBulbs();
            deviceDb.mergeWithUpdate(bulbs);
            return bulbs;
        }

        @Override
        protected void onPostExecute(final List<Bulb> bulbs) {
            WinkyContext.bulbs.clear();
            WinkyContext.bulbs.addAll(bulbs);
            adapter.notifyDataSetChanged();
            refreshDevicesTask = null;

        }
    }

    private class Data {
        List<Bulb> bulbs;
        List<NfcTag> tags;
    }

    public class GetDevicesTask extends AsyncTask<Void, Void, Data> {
        @Override
        protected Data doInBackground(Void... params) {
            Data data = new Data();
            data.bulbs = deviceDb.getBulbs();
            data.tags = tagDb.getTags(false);
            return data;
        }

        @Override
        protected void onPostExecute(final Data data) {
            WinkyContext.tags.clear();
            for (NfcTag tag: data.tags) {
                WinkyContext.tags.put(tag.id, tag);
            }
            WinkyContext.bulbs.addAll(data.bulbs);
            adapter.notifyDataSetChanged();
            getDevicesTask = null;
            if (data.bulbs.isEmpty()) {
                // no devices contained in the database, kick off a web refresh
                refreshDevices();
            }
        }
    }


    public class ToggleDevicesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int numOn = 0;
            for (Bulb b: WinkyContext.bulbs) {
                if (b.powered) numOn++;
            }
            boolean powered = false;
            if (numOn < WinkyContext.bulbs.size()/2) {
                powered = true;
            }
            Log.d("Groups", "switching all bulbs to powered=" + powered);
            for (Bulb b: WinkyContext.bulbs) {
                WinkyContext.getApi(getApplicationContext()).changeBulbState(b, powered);
            }
            deviceDb.update(WinkyContext.bulbs);
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
