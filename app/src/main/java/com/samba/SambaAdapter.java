package com.samba;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SambaAdapter extends ArrayAdapter<SambaFile> {

    private int mResourceId;

    public SambaAdapter(@NonNull Context context, int resourceId) {
        super(context, resourceId);
        mResourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SambaFile smbFile = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (smbFile.isDirectory()) {
            holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.folder));
        } else {
            holder.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.document));
        }
        String name = smbFile.getName();
        if(smbFile.isDirectory()){
            if (!TextUtils.isEmpty(name)) {
                name = name.substring(0, name.length() - 1);
            }
        }
        holder.name.setText(name);

        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolder(View view) {
            icon = view.findViewById(R.id.icon);
            name = view.findViewById(R.id.name);
        }
    }
}
