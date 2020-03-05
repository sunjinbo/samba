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

import androidx.fragment.app.DialogFragment;

public class NewTextFileDialog extends DialogFragment {

    private NewTextFileDialog.DialogListener mListener;

    public static NewTextFileDialog newInstance() {
        NewTextFileDialog fragment = new NewTextFileDialog();
        return fragment;
    }

    public NewTextFileDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.new_file_dialog, (ViewGroup) getView(), false);

        final EditText nameEditText = (EditText) view.findViewById(R.id.name);

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(editable.length() > 0);
            }
        });

        builder.setTitle(R.string.new_file);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onNewFile(nameEditText.getText().toString());
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
        void onNewFile(String name);
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
