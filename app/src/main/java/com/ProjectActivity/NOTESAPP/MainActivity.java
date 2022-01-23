package com.ProjectActivity.NOTESAPP;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> noteslist = new ArrayList<String>(); // Array that will have all the notes
    ArrayList<Integer> notesIDlist = new ArrayList<Integer>(); // Array that will have all the ID of the notes
    private DBOpenHelper tdb; // Import database's assets
    private SQLiteDatabase sdb; // Import database's assets

    public static String[] String_Array(ArrayList<String> arr) {
        String str[] = new String[arr.size()];
        for (int j = 0; j < arr.size(); j++) str[j] = arr.get(j);
        return str;
    } // Convert an array list to a string list for displaying the ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Open the database
        tdb = new DBOpenHelper(this, "test.db", null, 1);
        sdb = tdb.getWritableDatabase();

        // Prepare access to data
        String table_name = "test";
        String[] columns = {"ID", "TITLE_NAME", "DATA_NAME", "TIME_NAME","REMINDER_NAME"};
        String where = null;
        String where_args[] = null;
        String group_by = null;
        String having = null;
        String order_by = null;

        Cursor c = sdb.query(table_name, columns, where, where_args, group_by, having, order_by);

        // Add notes to the list
        c.moveToFirst();
        for (int x = 0; x < c.getCount(); x++)
        {
            noteslist.add(c.getString(1));
            notesIDlist.add(c.getInt(0));
            // Add the title and the ID of the note in the list
            c.moveToNext();
        }
        sdb.close();

        // Convert the array list to a string list
        String[] strlist = String_Array(noteslist);

        // Display the list of notes (using ListView)
        final ListView listofnotes = findViewById(R.id.listofnotes);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, strlist);
        listofnotes.setAdapter(adapter);

        // To know which note the user will select
        listofnotes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Get the ID of the note
                long selectedNotes_long = parent.getItemIdAtPosition(position);
                int selectedNotes = (int)selectedNotes_long;
                String selectedID = notesIDlist.get(selectedNotes).toString();

                // Display the new activity and send the ID of the selected note
                Intent intent = new Intent(MainActivity.this, NotesActivity.class);
                intent.putExtra("selectedID", selectedID);

                // Case for whether it is a new note or not
                intent.putExtra("isNew", "0"); // 0 = false (edited note)

                // Close MainActivity and open NotesActivity
                finish();
                startActivity(intent);
            }
        });


        FloatingActionButton create = findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, NotesActivity.class);
                String selectedID = "-1"; // it is a new note
                intent.putExtra("selectedID", selectedID);
                intent.putExtra("isNew", "1"); // 1 = true (new note)
                finish();
                startActivity(intent);
            }
        });
    }

    public void startAlarm (int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Intent notifyIntent = new Intent(getApplicationContext(),ShowNotification.class);
        notifyIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  calendar.getTimeInMillis(),1000 * 60 * 60 * 24, pendingIntent);
    }

    // Management of the toolbar (removed)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}