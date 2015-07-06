package net.dimatomp.tetris;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Date;

import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.TIME;
import static net.dimatomp.tetris.HighScoreStorage.HighScoreColumns.VALUE;

public class MainMenu extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.main_menu)));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case 1:
                new HighScoreFragment().show(getFragmentManager(), "highScore");
                break;
        }
    }

    public static class HighScoreFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private ListView list;

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), Uri.parse("content://net.dimatomp.tetris/highscore"),
                    null, null, null, VALUE + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ((CursorAdapter) list.getAdapter()).swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            ((CursorAdapter) list.getAdapter()).swapCursor(null);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            list = new ListView(getActivity());
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2,
                    null, new String[]{TIME, VALUE}, new int[]{android.R.id.text2, android.R.id.text1}, 0);
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (TIME.equals(cursor.getColumnName(columnIndex))) {
                        ((TextView) view).setText(new Date(cursor.getLong(columnIndex)).toString());
                    } else {
                        ((TextView) view).setText(Integer.toString(cursor.getInt(columnIndex)));
                    }
                    return true;
                }
            });
            list.setAdapter(adapter);
            getLoaderManager().initLoader(0, null, this);
            return new AlertDialog.Builder(getActivity())
                    .setTitle("High Scores")
                    .setView(list)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }).create();
        }
    }
}
