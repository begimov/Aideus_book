package com.aideus.book.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.aideus.book.R;
import com.aideus.book.data.local.DatabaseHelper;
import com.aideus.book.events.NoteLoadedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NoteFragment extends Fragment implements TextWatcher {

    public interface Contract {
        void closeNotes();
    }

    private static final String KEY_POSITION = "position";

    private static final String[] commentEmails = new String[]{"potapov@aideus.com", "begimov@aideus.com"};

    private EditText mEditText = null;

    private Intent mShareIntent = new Intent(Intent.ACTION_SEND).setType("text/plain");

    public static NoteFragment newInstance(final int position) {
        NoteFragment frag = new NoteFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        frag.setArguments(args);
        return (frag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.note_fragment, container, false);
        mEditText = (EditText) result.findViewById(R.id.et_note);
        mEditText.addTextChangedListener(this);

        Button buttonSave = (Button) result.findViewById(R.id.btn_note_save);
        Button buttonSend = (Button) result.findViewById(R.id.btn_note_send);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContract().closeNotes();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, commentEmails);
                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.book_comments)
                        + " " + getResources().getString(R.string.book_name)
                        + " " + Integer.toString(getPosition()));
                i.putExtra(Intent.EXTRA_TEXT, mEditText.getText());
                startActivity(Intent.createChooser(i, getResources().getString(R.string.send_mail)));
            }
        });

        return (result);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (TextUtils.isEmpty(mEditText.getText())) {
            //TODO Data access not using ModelFragment!
            DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
            db.loadNote(getPosition());
        }
    }

    @Override
    public void onPause() {
        //TODO Data access not using ModelFragment!
        DatabaseHelper.getInstance(getActivity())
                .updateNote(getPosition(),
                        mEditText.getText().toString());
        //TODO check if mEditText state is not changed from last database state
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notes, menu);
        ShareActionProvider mShareActionProvider;
        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
        mShareActionProvider.setShareIntent(mShareIntent);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            mEditText.setText(null);
            getContract().closeNotes();
            return (true);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(NoteLoadedEvent event) {
        if (event.getPosition() == getPosition()) {
            //TODO Data access not using ModelFragment!
            mEditText.setText(event.getProse());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        mShareIntent.putExtra(Intent.EXTRA_TEXT, s.toString());
    }

    private int getPosition() {
        return (getArguments().getInt(KEY_POSITION, -1));
    }

    private Contract getContract() {
        return ((Contract) getActivity());
    }
}
