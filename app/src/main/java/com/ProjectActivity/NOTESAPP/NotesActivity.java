package com.ProjectActivity.NOTESAPP;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class NotesActivity extends Activity {
    private DBOpenHelper tdb; // Import database's assets
    private SQLiteDatabase sdb; // Import database's assets

    public String db_title; // Var that will host the title
    public String db_data; // Var that will host the data
    public String db_time; // Var that will host the last update time
    public String db_reminder;

    public int isnew; // To know if it is a new or a edited note
    public int db_id; // Var that will host the ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_activity);

        // Buttons declaration
        final FloatingActionButton save = findViewById(R.id.save);
        final FloatingActionButton reminder = findViewById(R.id.reminder);
        final TextView reminder_text = findViewById(R.id.reminder_text);
        final FloatingActionButton returnlist = findViewById(R.id.returnlist);
        final FloatingActionButton delete = findViewById(R.id.delete);
        final FloatingActionButton share = findViewById(R.id.share);

        // Recover the data from MainActivity
        final Intent MainActivityIntent = getIntent();

        // EditText declaration
        final EditText title = findViewById(R.id.titleNotes);
        final EditText data = findViewById(R.id.dataNotes);

        // Open the database
        tdb = new DBOpenHelper(this, "test.db", null, 1);
        sdb = tdb.getWritableDatabase();

        // Recover selected ID and type of note (new or edited) from MainActivity
        final String selectedID = MainActivityIntent.getStringExtra("selectedID");
        final String isNew = MainActivityIntent.getStringExtra("isNew");
        db_id = Integer.parseInt(selectedID);
        isnew = Integer.parseInt(isNew);

        if (db_id == -1) {
            // Creating a new note
            //Toast.makeText(getApplicationContext(),"New note created",Toast.LENGTH_SHORT).show();
        } else {
            // Editing a note
            //Toast.makeText(getApplicationContext(),"ID : "+selectedID,Toast.LENGTH_SHORT).show();

            // Will recover the data from the note
            Cursor c = sdb.query("test", new String[] {"ID", "TITLE_NAME", "DATA_NAME", "TIME_NAME", "REMINDER_NAME"},
                    null, null, null, null, null);

            int x = 0;
            c.moveToFirst();
            while (x < c.getCount() && c.getInt(0) != db_id) {
                c.moveToNext();
                x++;
            }
            db_id = c.getInt(0); // Current ID
            db_title = c.getString(1); // Current title
            db_data = c.getString(2); // Current data
            db_time = c.getString(3); // Last update of the note
            db_reminder = c.getString(4); // Last update of the note

            title.setText(db_title); // Show title in the NotesActivity
            data.setText(db_data); // Show data in the NotesActivity
            reminder_text.setText(db_reminder);
            // Display the date/time of the last update of the note
            Toast.makeText(getApplicationContext(),"Last update : "+db_time,Toast.LENGTH_SHORT).show();
        } // Case where the user wants to edit an existing note

        // Button that will return to MainActivity
        returnlist.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(NotesActivity.this, MainActivity.class);
                sdb.close();
                finish();
                startActivity(intent);
            }
        });

        // Button that will delete the current note and return to MainActivity
        delete.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(NotesActivity.this, MainActivity.class);
                // Delete the note from database
                sdb.delete("test", "ID="+db_id, null);
                sdb.close();
                finish();
                startActivity(intent);
            }
        });

        //Reminder button
        final int minute=0;
        final int hour = 0;
        reminder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(NotesActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String time="";
                                time+=hourOfDay + ":" + minute;
                                reminder_text.setText(time);
                                startAlarm(hourOfDay, minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        // Button that will save the note
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get the new title, data and time
                String title_text = title.getText().toString();
                String data_text = data.getText().toString();
                String time_text = DateFormat.getDateTimeInstance().format(new Date());
                if (isnew == 1) {
                    // Saving the new note
                    ContentValues newnote = new ContentValues();
                    newnote.put("TITLE_NAME", title_text);
                    newnote.put("DATA_NAME", data_text);
                    newnote.put("TIME_NAME", time_text);
                    newnote.put("REMINDER_NAME", reminder_text.getText().toString());
                    sdb.insert("test", null, newnote);
                    isnew = 0; // the note is not longer 'new'
                } else {
                    ContentValues editednote = new ContentValues();
                    editednote.put("TITLE_NAME", title_text);
                    editednote.put("DATA_NAME", data_text);
                    editednote.put("TIME_NAME", time_text);
                    editednote.put("REMINDER_NAME", reminder_text.getText().toString());
                    sdb.update("test", editednote, "ID="+db_id, null);
                } // Updating the note
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                if (hour != 0 || minute != 0) startAlarm(hour, minute);
            }
        });

        // Button that will share the note (using a new Intent)
        share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String title_text = title.getText().toString();
                String data_text = data.getText().toString();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, title_text + "\n" + data_text);
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
            }
        });
    }

    public void startAlarm (int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Intent notifyIntent = new Intent(getApplicationContext(), ShowNotification.class);
        notifyIntent.putExtra ("title", db_title);
        notifyIntent.putExtra ("text", db_data);
        notifyIntent.setFlags (Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  calendar.getTimeInMillis(),1000 * 60 * 60 * 24, pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}