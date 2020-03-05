package com.samba.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.samba.R;

import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialog extends DialogFragment {
    private final static String PATH = "path";
    private final static String ISDIRECTORY = "isDirectory";
    private String mPath;

    public static ConfirmDeleteDialog newInstance(String path,boolean isDirectory) {
        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        args.putBoolean(ISDIRECTORY,isDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String msg;
        final String path = getArguments().getString(PATH);
        final boolean isDirectory = getArguments().getBoolean(ISDIRECTORY);
        if (isDirectory) {
            msg = "You are about to delete the folder with all it's content for real.";
        } else {
            msg = "You are about to delete the file";
        }
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ConfirmListener) getActivity()).onConfirmDelete(isDirectory,path);
            }
        });
        builder.setNegativeButton(R.string.label_cancel, null);
        return builder.create();
    }

    public interface ConfirmListener {
        void onConfirmDelete(boolean isDirectory,String path);
    }
}
