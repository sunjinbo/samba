package com.samba.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.samba.R;
import com.samba.SambaUtil;

import java.io.File;

import androidx.fragment.app.DialogFragment;

public class RenameDialog extends DialogFragment {
    private final static String PATH = "path";
    private final static String FNAME = "fname";
    private DialogListener mListener;

    public static RenameDialog newInstance(String path,String fname) {
        RenameDialog fragment = new RenameDialog();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        args.putString(FNAME, fname);
        fragment.setArguments(args);
        return fragment;
    }

    public RenameDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.rename_dialog, (ViewGroup) getView(), false);

        // if text is empty, disable the dialog positive button
        final EditText currentNameText = (EditText) view.findViewById(R.id.current_name);
        final String path = getArguments().getString(PATH);
        final String fname = getArguments().getString(FNAME);

        //final File file = mStorage.getFile(path);
        currentNameText.setText(fname);
        //final String parent = file.getParent();

        final EditText newNameText = (EditText) view.findViewById(R.id.new_name);
        newNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(editable != null &&
                        editable.length() > 0);
            }
        });

        builder.setTitle(R.string.rename);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newName = newNameText.getText().toString();
                String toPath = SambaUtil.getParentPath(path) + File.separator + newName;
                mListener.onRename(path, toPath);
            }
        });

        final AlertDialog dialog = builder.create();
        view.post(new Runnable() {
            @Override
            public void run() {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        dialog.setCancelable(false);
        return dialog;
    }

    public interface DialogListener {
        void onRename(String fromPath, String toPath);
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
