package org.coolfrood.winky;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TagsActivity extends Activity {

    private GetTagsTask getTagsTask;
    private AddTagTask addTagTask;
    private RegisterTagTask registerTagTask;
    private NfcAdapter nfcAdapter;
    private TagDb tagDb;
    private List<NfcTag> registeredTags = new ArrayList<>();
    private List<NfcTag> ignoredTags = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private TagAdapter adapter;
    private RegisterParams tagToAdd;
    private RecyclerView recyclerView;
    private boolean dialogActive = false;

    private boolean gotTags = false;

    private static String[][] techList = { new String[] { Ndef.class.getName() }};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("TagsActivity", "launched tagsactivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        tagDb = new TagDb(getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.tags);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new TagAdapter(registeredTags, ignoredTags);
        recyclerView.setAdapter(adapter);
        tagToAdd = null;
        getTags();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tags, menu);
        return true;
    }

    private boolean tagExists(List<NfcTag> tags, byte[] id) {
        for (NfcTag t: tags) {
            if (Arrays.equals(t.deviceId, id))
                return true;
        }
        return false;
    }

    private RegisterParams createNewTagParams(Tag t, Parcelable[] rawMsgs) {
        RegisterParams p = new RegisterParams(new NfcTag(0, null, false, t.getId()), t);
        if (rawMsgs != null && rawMsgs.length >= 1) {
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            NdefRecord[] records = msg.getRecords();
            for (NdefRecord r: records) {
                if (r.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE) {
                    String tpe = new String(r.getType());
                    Log.i("TagsActivity", "tpe=" + tpe);
                    String payload = new String(r.getPayload());

                    if (tpe.equals("org.coolfrood.winky:name")) {
                        p.nfcTag.name = payload;
                    }
                }
            }
        }
        return p;
    }
    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        if (gotTags && !dialogActive) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            //Log.i("TagsActivity", "rawMsgs.size=" + rawMsgs.length);
            byte[] id = tag.getId();
            boolean found = tagExists(registeredTags, id) || tagExists(ignoredTags, id);

            Log.i("TagsActivity", tag.toString());

            if (!found) {
                tagToAdd = createNewTagParams(tag, rawMsgs);
                AddTagDialogFragment newTag = AddTagDialogFragment.newInstance(id, tagToAdd.nfcTag.name);
                newTag.show(getFragmentManager(), "add_tag");
                dialogActive = true;
            }
        }
        //toggleDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            nfcAdapter.enableForegroundDispatch(this, intent, null, techList);
        }
    }



    @Override
    public void onPause() {
        super.onPause();

        if (nfcAdapter != null)
          nfcAdapter.disableForegroundDispatch(this);
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

    public void addIgnoredTag(byte[] deviceId) {
        dialogActive = false;
        tagToAdd = null;
        NfcTag tag = new NfcTag(0, null, true, deviceId);
        if (addTagTask != null)
            return;
        addTagTask = new AddTagTask();
        addTagTask.execute(tag);

    }

    public void addRegisteredTag(String name, byte[] deviceId) {
        dialogActive = false;
        NfcTag nfcTag = new NfcTag(0, name, false, deviceId);
        if (registerTagTask == null) {
            registerTagTask = new RegisterTagTask(tagToAdd.nfcTag.name == null);
            RegisterParams p = new RegisterParams(nfcTag, tagToAdd.tag);
            registerTagTask.execute(p);
            tagToAdd = null;
        }

    }

    private void getTags() {
        if (getTagsTask != null)
            return;
        getTagsTask = new GetTagsTask();
        getTagsTask.execute((Void) null);
    }

    private class GetTagsTask extends AsyncTask<Void, Void, List<NfcTag>> {
        @Override
        protected List<NfcTag> doInBackground(Void... params) {
            return tagDb.getTags(true);

        }

        @Override
        protected void onPostExecute(final List<NfcTag> tags) {
            updateTagList(tags);
            adapter.notifyDataSetChanged();
            getTagsTask = null;
            gotTags = true;

        }
    }

    private class AddTagTask extends AsyncTask<NfcTag, Void, List<NfcTag>> {
        @Override
        protected List<NfcTag> doInBackground(NfcTag... params) {
            tagDb.add(params[0]);
            return tagDb.getTags(true);
        }

        @Override
        protected void onPostExecute(final List<NfcTag> tags) {
            updateTagList(tags);
            adapter.notifyDataSetChanged();
            addTagTask = null;
        }
    }

    private class RegisterParams {
        public NfcTag nfcTag;
        public Tag tag;
        public RegisterParams(NfcTag nfcTag, Tag tag) {
            this.nfcTag = nfcTag;
            this.tag = tag;
        }
    }

    private class RegisterTagTask extends AsyncTask<RegisterParams, Void, List<NfcTag>> {

        private String errMsg = null;
        private boolean doProgramTag = false;

        public RegisterTagTask(boolean doProgramTag) {
            this.doProgramTag = doProgramTag;
        }

        @Override
        protected List<NfcTag> doInBackground(RegisterParams... params) {
            RegisterParams p = params[0];

            if (doProgramTag) {
                Log.i("TagsActivity", "Programming tag");
                WriteResponse resp = writeTag(p.tag, p.nfcTag.name, p.nfcTag.deviceId);
                if (resp.success) {
                    tagDb.add(p.nfcTag);
                    return tagDb.getTags(true);
                } else {
                    errMsg = resp.message;
                    return null;
                }
            } else {
                tagDb.add(p.nfcTag);
                return tagDb.getTags(true);
            }
        }

        @Override
        protected void onPostExecute(final List<NfcTag> tags) {
            if (errMsg != null) {
                Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
            } else {
                updateTagList(tags);
                adapter.notifyDataSetChanged();
            }
            registerTagTask = null;
        }
    }

    private void updateTagList(final List<NfcTag> tagsFromDb) {
        registeredTags.clear();
        ignoredTags.clear();
        for (NfcTag tag: tagsFromDb) {
            if (tag.ignored)
                ignoredTags.add(tag);
            else
                registeredTags.add(tag);
        }
    }



    public void onClickShowIgnored(View view) {
        boolean showIgnored = ((Switch) view).isChecked();
        adapter.changeShowIgnored(showIgnored);
        adapter.notifyDataSetChanged();

    }

    private class WriteResponse {
        public boolean success;
        public String message;
        public WriteResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private WriteResponse writeTag(Tag tag, String name, byte[] deviceId) {
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] {
                     NdefRecord.createExternal("org.coolfrood.winky", "name", name.getBytes()),
                     NdefRecord.createExternal("org.coolfrood.winky", "id", deviceId)
                     //,NdefRecord.createApplicationRecord("org.coolfrood.winky")

                }
        );
        /*
        NdefMessage msg = new NdefMessage(
                new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)
        );
        */
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable())
                    return new WriteResponse(false, "Tag is read-only");
                int size = msg.toByteArray().length;
                if (ndef.getMaxSize() < size)
                    return new WriteResponse(false, "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size);
                ndef.writeNdefMessage(msg);
                return new WriteResponse(true, "Wrote message to pre-formatted tag.");
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    format.connect();
                    format.format(msg);
                    return new WriteResponse(true, "Formatted tag and wrote message");

                } else {
                    return new WriteResponse(false, "Tag does not support NDEF.");
                }
            }

        } catch (Exception e) {
            return new WriteResponse(false, "Failed to write tag: " + e.getMessage());
        }
    }
}
