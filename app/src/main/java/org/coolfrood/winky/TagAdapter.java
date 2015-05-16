package org.coolfrood.winky;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder>{

    private boolean showIgnored = false;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.tag_info);

        }
    }

    public TagAdapter(List<NfcTag> registeredTags, List<NfcTag> ignoredTags) {
        this.registeredTags = registeredTags;
        this.ignoredTags = ignoredTags;
        this.showIgnored = false;
    }

    private List<NfcTag> registeredTags;
    private List<NfcTag> ignoredTags;

    @Override
    public TagAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < registeredTags.size()) {
            holder.textView.setText(registeredTags.get(position).getDisplayName());
        } else {
            holder.textView.setText(ignoredTags.get(position - registeredTags.size()).getDisplayName());
            holder.textView.setTypeface(null, Typeface.ITALIC);
        }
    }

    public void changeShowIgnored(boolean newState) {
        this.showIgnored = newState;
    }

    @Override
    public int getItemCount() {
        int total = this.registeredTags.size();
        if (showIgnored)
            total += this.ignoredTags.size();
        return total;
    }
}
