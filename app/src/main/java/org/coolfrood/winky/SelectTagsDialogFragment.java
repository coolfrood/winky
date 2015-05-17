package org.coolfrood.winky;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothClass;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectTagsDialogFragment extends DialogFragment {

    public static SelectTagsDialogFragment newInstance(int position) {
        SelectTagsDialogFragment frag = new SelectTagsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int position = getArguments().getInt("position");
        final DevicesActivity activity = ((DevicesActivity) getActivity());

        final Map<Integer, NfcTag> tags = activity.tags;
        final Bulb bulb = activity.bulbs.get(position);

        final List<Integer> beforeTags = bulb.tags;
        final boolean[] active = new boolean[tags.size()];
        final String[] labels = new String[tags.size()];
        final NfcTag[] tagArray = new NfcTag[tags.size()];

        int idx = 0;
        // Prepare list of currently registered tags
        // whether a tag is currently assigned to the device
        for (Map.Entry<Integer, NfcTag> e : tags.entrySet()) {

            if (beforeTags.contains(e.getKey()))
                active[idx] = true;
            labels[idx] = e.getValue().getDisplayName();
            tagArray[idx] = e.getValue();
            idx++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.selecttags_title)
                .setMultiChoiceItems(labels, active,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                active[which] = isChecked;
                            }
                        })
                .setPositiveButton(R.string.selecttags_update,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final List<Integer> newTags = new ArrayList<>();
                                for (int i = 0; i < active.length; i++) {
                                    if (active[i]) {
                                        newTags.add(tagArray[i].id);
                                    }
                                    if (!newTags.equals(beforeTags)) {
                                        activity.updateTagsForDevice(bulb, newTags);
                                    }
                                }
                            }
                        })
                .setNegativeButton(R.string.selecttags_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
        return builder.create();

    }


}
