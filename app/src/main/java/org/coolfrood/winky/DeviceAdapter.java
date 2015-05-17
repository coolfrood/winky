package org.coolfrood.winky;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private DevicesActivity activity;

    public DeviceAdapter(DevicesActivity activity) {
        this.activity = activity;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageButton editButton;
        public ViewHolder(View v, final DevicesActivity activity) {
            super(v);
            textView = (TextView) v.findViewById(R.id.device_info);
            editButton = (ImageButton) v.findViewById(R.id.device_edit);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectTagsDialogFragment frag = SelectTagsDialogFragment.newInstance(getAdapterPosition());
                    frag.show(activity.getFragmentManager(), "select_tags");
                }
            });
        }
    }

    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_view, parent, false);
        ViewHolder vh = new ViewHolder(v, activity);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(activity.bulbs.get(position).name);
    }

    @Override
    public int getItemCount() {
        return activity.bulbs.size();
    }

}
