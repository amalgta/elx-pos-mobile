package styx.mobile.elxpos.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epson.epos2.discovery.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

import styx.mobile.elxpos.R;

/**
 * Created by amalg on 16-11-2017.
 */

public class PrinterRecyclerAdapter extends RecyclerView.Adapter<PrinterRecyclerAdapter.ViewHolder> {
    private List<DeviceInfo> printerList;
    private OnDeviceSelectedListener onDeviceSelectedListener;

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(DeviceInfo deviceInfo);
    }

    public void setOnDeviceSelectedListener(OnDeviceSelectedListener onDeviceSelectedListener) {
        this.onDeviceSelectedListener = onDeviceSelectedListener;
    }

    public PrinterRecyclerAdapter(OnDeviceSelectedListener onDeviceSelectedListener) {
        printerList = new ArrayList<>();
        this.onDeviceSelectedListener = onDeviceSelectedListener;
    }

    public void add(DeviceInfo deviceInfo) {
        printerList.add(deviceInfo);
        notifyItemInserted(printerList.size());
    }

    public void clear() {
        int oldSize = printerList.size();
        printerList.clear();
        notifyItemRangeRemoved(0, oldSize);
    }

    @Override
    public int getItemCount() {
        return printerList.size();
    }

    @Override
    public PrinterRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_printer, parent, false));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelName;
        View container;

        public ViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.container);
            labelName = v.findViewById(R.id.labelName);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DeviceInfo deviceInfo = printerList.get(position);
        holder.labelName.setText(deviceInfo.getDeviceName());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDeviceSelectedListener != null)
                    onDeviceSelectedListener.onDeviceSelected(printerList.get(holder.getAdapterPosition()));
            }
        });
    }


}