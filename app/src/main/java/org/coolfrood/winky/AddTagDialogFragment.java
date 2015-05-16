package org.coolfrood.winky;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddTagDialogFragment extends DialogFragment {

    public static AddTagDialogFragment newInstance(byte[] id, Tag tag) {
        AddTagDialogFragment frag = new AddTagDialogFragment();
        Bundle args = new Bundle();
        args.putByteArray("device_id", id);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final byte[] deviceId = getArguments().getByteArray("device_id");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_addtag, null);
        final EditText name = (EditText) v.findViewById(R.id.addtag_name);
        builder.setView(v);
        String fmt = getResources().getString(R.string.tag_found);
        builder.setTitle(String.format(fmt, NfcTag.byteArrayToHex(deviceId)))
                .setPositiveButton(R.string.tag_add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton(R.string.tag_ignore, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ((TagsActivity) getActivity()).addIgnoredTag(deviceId);
                    }
                });

        final AlertDialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String s = name.getText().toString().trim();
                        if (TextUtils.isEmpty(s)) {
                            Toast.makeText(getActivity().getApplicationContext(), "Please provide a name", Toast.LENGTH_SHORT).show();
                        } else {
                            ((TagsActivity) getActivity()).addRegisteredTag(s, deviceId);
                            dismiss();
                        }
                    }
                });
            }
        });

        // Create the AlertDialog object and return it
        return d;
    }

}
