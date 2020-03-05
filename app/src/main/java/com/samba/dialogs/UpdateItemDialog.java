package com.samba.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.samba.R;
import com.samba.SambaFile;

import androidx.fragment.app.DialogFragment;

public class UpdateItemDialog extends DialogFragment {
    private final static String PATH = "path";
    private final static String ISDIRECTORY = "isDirectory";
    private final static String FNAME = "fname";
    private DialogListener mListener;

    public static UpdateItemDialog newInstance(SambaFile file) {
        UpdateItemDialog fragment = new UpdateItemDialog();
        Bundle args = new Bundle();
        args.putString(PATH, file.getPath());
        args.putString(FNAME, file.getName());
        args.putBoolean(ISDIRECTORY,file.isDirectory());
        fragment.setArguments(args);
        return fragment;
    }

    public UpdateItemDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Dialog dialog = new BottomSheetDialog(getActivity(), getTheme());
        final String path = getArguments().getString(PATH);
        final String fname = getArguments().getString(FNAME);
        final boolean isDirectory = getArguments().getBoolean(ISDIRECTORY);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_item_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);

        // title
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(isDirectory ? getString(R.string.folder_options) : getString(R.string.file_options));

        View rename = view.findViewById(R.id.rename);
        View delete = view.findViewById(R.id.delete);
        View move = view.findViewById(R.id.move);
        View copy = view.findViewById(R.id.copy);
        View download = view.findViewById(R.id.download);

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.rename, path,fname,isDirectory);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                mListener.onOptionClick(R.id.delete, path,fname,isDirectory);
            }
        });

        if (!isDirectory) {
            move.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    mListener.onOptionClick(R.id.move, path,fname,isDirectory);
                }
            });

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    mListener.onOptionClick(R.id.copy, path,fname,isDirectory);
                }
            });
            download.setVisibility(View.VISIBLE);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mListener.onOptionClick(R.id.download, path,fname,isDirectory);
                }
            });
        } else {
            move.setVisibility(View.GONE);
            copy.setVisibility(View.GONE);
            download.setVisibility(View.GONE);
        }

        // control dialog width on different devices
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogINterface) {
                int width = (int) getResources().getDimension(R.dimen.bottom_sheet_dialog_width);
                dialog.getWindow().setLayout(
                        width == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : width,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });

        return dialog;
    }

    public interface DialogListener {
        void onOptionClick(int which, String path,String fname,boolean isDirectory);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
