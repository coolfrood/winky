package org.coolfrood.winky;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DevicesActivity extends Activity {

    private RecyclerView recyclerView;
    private RefreshDevicesTask refreshDevicesTask;
    private ToggleDevicesTask toggleDevicesTask;
    private GetDevicesTask getDevicesTask;
    private UpdateDeviceTask updateDeviceTask;
    private TagFiredTask tagFiredTask;
    private NfcAdapter nfcAdapter;
    private DeviceDb deviceDb;
    private TagDb tagDb;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    public List<Bulb> bulbs = new ArrayList<>();
    public Map<Integer, NfcTag> tags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        recyclerView = (RecyclerView) findViewById(R.id.devices);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        bulbs.clear();
        adapter = new DeviceAdapter(this);
        recyclerView.setAdapter(adapter);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        deviceDb = new DeviceDb(getApplicationContext());
        tagDb = new TagDb(getApplicationContext());

        if (!WinkyContext.getApi(getApplicationContext()).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            getDevices();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.i("DevicesActivity", "started due to NDEF");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // remove the tag from the extra to make sure redelivery of
            // intent doesn't trigger another update.
            intent.removeExtra(NfcAdapter.EXTRA_TAG);
            if (tagFiredTask == null && tag != null) {
                tagFiredTask = new TagFiredTask(tag.getId());
                tagFiredTask.execute((Void) null);
            }
        }
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

    private void toggleDevices(List<Bulb> bulbs) {
        if (toggleDevicesTask != null)
            return;
        toggleDevicesTask = new ToggleDevicesTask();
        toggleDevicesTask.execute(bulbs);
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
        } else if (id == R.id.action_add_tag) {
            Intent intent = new Intent(this, TagsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateTagsForDevice(Bulb b, List<Integer> newTags) {
        if (updateDeviceTask == null) {
            updateDeviceTask = new UpdateDeviceTask();
            b.tags = newTags;
            updateDeviceTask.execute(b);
        }
    }

    private class UpdateDeviceTask extends AsyncTask<Bulb, Void, List<Bulb>> {
        @Override
        protected List<Bulb> doInBackground(Bulb... params) {
            deviceDb.update(Arrays.asList(params));
            return deviceDb.getBulbs();
        }

        @Override
        protected void onPostExecute(final List<Bulb> bulbs) {
            bulbs.clear();
            bulbs.addAll(bulbs);
            adapter.notifyDataSetChanged();
            updateDeviceTask = null;
        }
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
            Log.i("DevicesActivity", "bulbs.size=" + bulbs.size());
            DevicesActivity.this.bulbs.clear();
            DevicesActivity.this.bulbs.addAll(bulbs);
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
            tags.clear();
            for (NfcTag tag: data.tags) {
                tags.put(tag.id, tag);
            }
            bulbs.addAll(data.bulbs);
            adapter.notifyDataSetChanged();
            getDevicesTask = null;
            if (data.bulbs.isEmpty()) {
                // no devices contained in the database, kick off a web refresh
                refreshDevices();
            }
        }
    }

    /**
     * Toggle the given devices. Make a majority decision about the
     * current state they are in.
     */
    public class ToggleDevicesTask extends AsyncTask<List<Bulb>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(List<Bulb>... params) {
            int numOn = 0;
            for (Bulb b: params[0]) {
                if (b.powered) numOn++;
            }
            boolean powered = false;
            if (numOn < params[0].size()/2) {
                powered = true;
            }
            Log.d("DevicesActivity", "switching all " + params[0].size()  + " bulbs to powered=" + powered);
            for (Bulb b: params[0]) {
                WinkyContext.getApi(getApplicationContext()).changeBulbState(b, powered);
            }
            deviceDb.update(params[0]);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean res) {
            adapter.notifyDataSetChanged();
            toggleDevicesTask = null;
            finish();

        }
    }

    /**
     * NFC Tag was fired. Find the devices that are controlled by the tag,
     * refresh the state and then toggle their state.
     */
    private class TagFiredTask extends AsyncTask<Void, Void, List<Bulb>> {
        private byte[] deviceId;
        TagFiredTask(byte[] id) {
            this.deviceId = id;
        }

        @Override
        protected List<Bulb> doInBackground(Void... params) {
            List<NfcTag> tags = tagDb.getTags(false);
            List<Bulb> bulbsToToggle = new ArrayList<>();
            for (NfcTag t: tags) {
                if (Arrays.equals(deviceId, t.deviceId)) {
                    List<Bulb> bulbs = WinkyContext.getApi(getApplicationContext()).getBulbs();
                    deviceDb.mergeWithUpdate(bulbs);
                    for (Bulb b: bulbs) {
                        if (b.tags.contains(t.id)) {
                            Log.d("DevicesActivity", "bulb " + b.name + " is powered " + b.powered);
                            bulbsToToggle.add(b);
                        }
                    }
                    break;
                }
            }
            return bulbsToToggle;
        }

        @Override
        protected void onPostExecute(final List<Bulb> bulbs) {
           tagFiredTask = null;
           toggleDevices(bulbs);
        }
    }
}
