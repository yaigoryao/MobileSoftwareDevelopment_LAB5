package ru.msfd.lab5;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.HashSet;

public class ShowAuthorsCompositions extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private static final int COMPOSITIONS_FLAG = 0;
    ListView compositionsListView;
    SimpleCursorAdapter compositionsCursorAdapter;
    public String authorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_authors_compositions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CheckPermissions();
    }

    private void CheckPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_PERMISSION);
        else Setup();
    }

    private void Setup()
    {
        Intent intent = getIntent();
        authorName = intent.getStringExtra("author").trim();
        Log.d("mytag", authorName);
        ((TextView) findViewById(R.id.compositions_author_label)).setText(authorName);
        SetupCursor();
        SetupList();
    }

    static class MyCursorLoader extends CursorLoader {

        private String authorName;

        public MyCursorLoader(Context context, String authorName) {
            super(context);
            this.authorName = authorName;
        }

        @Override
        public Cursor loadInBackground() {
            return getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME },
                    MediaStore.Audio.Media.ARTIST + " = ?",
                    new String[] { authorName },
                    null);
        }
    }

    private void SetupCursor()
    {
        LoaderManager.getInstance(this).initLoader(COMPOSITIONS_FLAG, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                return new MyCursorLoader(ShowAuthorsCompositions.this, authorName);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                compositionsCursorAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }

    private void SetupList()
    {
        compositionsListView = (ListView) findViewById(R.id.compositions_listview);
        compositionsCursorAdapter = new SimpleCursorAdapter(this,
                R.layout.composition_list_item,
                null,
                new String[] { MediaStore.Audio.Media.DISPLAY_NAME },
                new int[] { R.id.composition_name_textview },
                COMPOSITIONS_FLAG);
        compositionsListView.setAdapter(compositionsCursorAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) Setup();
        else Toast.makeText(this, "Разрешение было отклонено пользователем", Toast.LENGTH_SHORT).show();
    }
}