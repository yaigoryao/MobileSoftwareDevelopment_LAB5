package ru.msfd.lab5;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int AUTHORS_FLAG = 0;
    private static final int REQUEST_PERMISSION = 1;
    ListView authorsListView;
    SimpleCursorAdapter authorsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CheckPermissions();
    }

    private void CheckPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_PERMISSION);
        else Setup();
    }

    private void Setup()
    {
        SetupCursor();
        SetupList();
    }

    static class MyCursorLoader extends CursorLoader {

        public MyCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.ARTIST},
                    null,
                    null,
                    null);

            if (cursor != null)
            {
                HashSet<String> uniqueArtists = new HashSet<>();
                MatrixCursor uniqueCursor = new MatrixCursor(new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST});
                while (cursor.moveToNext())
                {
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    if (artist != null && uniqueArtists.add(artist))
                    {
                        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        uniqueCursor.addRow(new Object[]{ id, artist });
                    }
                }
                cursor.close();
                return uniqueCursor;
            }
            return null;
        }
    }

    private void SetupCursor()
    {
        LoaderManager.getInstance(this).initLoader(AUTHORS_FLAG, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                return new MyCursorLoader(MainActivity.this);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                authorsListAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }

    private void SetupList()
    {
        authorsListView = (ListView) findViewById(R.id.authors_listview);
        authorsListAdapter = new SimpleCursorAdapter(this,
                R.layout.author_list_item,
                null,
                new String[] { MediaStore.Audio.Media.ARTIST },
                new int[] { R.id.author_name_textview },
                AUTHORS_FLAG);
        authorsListView.setAdapter(authorsListAdapter);
        authorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MatrixCursor matrixCursor = (MatrixCursor) adapterView.getItemAtPosition(i);
                Intent intent = new Intent("android.intent.action.show_authors_compositions");
                intent.putExtra("author", matrixCursor.getString(matrixCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) Setup();
        else Toast.makeText(this, "Разрешение было отклонено пользователем", Toast.LENGTH_SHORT).show();
    }
}