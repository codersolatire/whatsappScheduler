package www.geekphisher.com.scheduler;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    public  static final int RequestPermissionCode  = 1 ;
    EditText msg,current_date,current_time;
    Button contact;
    TimePicker picktime;
    TextView name,filepath;
    Intent intent;
    String TempNameHolder, TempNumberHolder;
    String currentTime,FilePath="";
    ImageView selectedimage;
    long time;
    Calendar calNow,calSet;
    private int mYear, mMonth, mDay, mHour, mMinute;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contact = (Button)findViewById(R.id.contact);
        msg = (EditText) findViewById(R.id.msg);
        current_date = (EditText) findViewById(R.id.current_date);
        current_time = (EditText) findViewById(R.id.current_time);
        filepath = (TextView) findViewById(R.id.filepath);
        name = (TextView) findViewById(R.id.name);
        selectedimage = (ImageView) findViewById(R.id.selectedimage);

        EnableRuntimePermission();
        sharedPreferences = getSharedPreferences("details",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("fileB",false);
        editor.commit();


        contact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {

                intent = new Intent(Intent.ACTION_PICK);
                intent.setPackage("com.whatsapp");
                try{
                    startActivityForResult(intent, 7);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        current_date.setText(new SimpleDateFormat("dd/MM/YYYY").format(new Date()));
        current_time.setText(new SimpleDateFormat("HH:mm").format(new Date()));
    }

    private void createSchedule(Calendar targetCal)
    {
        Toast.makeText(this, "\n\n***\n" + "Your message has been scheduled @ " + targetCal.getTime() + "\n" + "***\n", Toast.LENGTH_SHORT).show();

        editor.putString("toNumber",TempNumberHolder);
        editor.putString("msg",msg.getText().toString());

        Intent intent = new Intent(getBaseContext(), Scheduler.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 1, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);

        editor.commit();

    }

    public void openWhatsApp(View view)
    {
        /*try {
            WhatsappApi.getInstance().getContacts(MainActivity.this, new GetContactsListener() {
                @Override
                public void receiveWhatsappContacts(List<WContact> list) {
                    Toast.makeText(getApplicationContext(), "Contacts received", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (WhatsappNotInstalledException e) {
            e.printStackTrace();
        }*/

        createSchedule(calSet);
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            Toast.makeText(MainActivity.this,"Contact Permission Allowed...", Toast.LENGTH_LONG).show();
        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]
                    {Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public void onActivityResult(int RequestCode, int ResultCode, Intent ResultIntent) {

        super.onActivityResult(RequestCode, ResultCode, ResultIntent);

        switch (RequestCode) {

            case(1):
                if(RequestCode == 1)
                {
                    Uri selectedImage = ResultIntent.getData();
                    FilePath = new File(getRealPathFromURI(selectedImage)).toString();

                    InputStream inputStream;

                    try {
                        inputStream = getContentResolver().openInputStream(selectedImage);

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        selectedimage.setImageBitmap(bitmap);
                        filepath.setText(FilePath);

                        editor.putString("filepath",FilePath);
                        editor.putBoolean("fileB",true);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(this, "Unable to Open Image", Toast.LENGTH_SHORT).show();
                    }

                }
                break;

            case (7):
                if (ResultCode == Activity.RESULT_OK)
                {
                    if(ResultIntent.hasExtra("contact"))
                    {
                        TempNumberHolder = ResultIntent.getStringExtra("contact");
                        TempNumberHolder = TempNumberHolder.replace("@s.whatsapp.net","");
                        TempNameHolder = getContactName(TempNumberHolder,this);
                        name.setText(TempNameHolder);
                    }
                }
                break;
        }
    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void datePick(View view)
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                Toast.makeText(MainActivity.this, year + "."+ month + "."+dayOfMonth, Toast.LENGTH_SHORT).show();
                current_date.setText(dayOfMonth+"/"+ (month+1) +"/"+year);

                SimpleDateFormat dateformat = new SimpleDateFormat("H:mm");
                currentTime = dateformat.format(Calendar.getInstance().getTime());

                calNow = Calendar.getInstance();
                calSet = (Calendar) calNow.clone();

                calSet.set(year,month,dayOfMonth);

                if(calSet.compareTo(calNow) <= 0){
                    //Today Set time passed, count to tomorrow
                    calSet.add(Calendar.DATE, 1);
                }
            }
        });
        datePickerDialog.show();
    }

    public void timePick(View view)
    {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                Toast.makeText(MainActivity.this, hourOfDay + ":"+ minute, Toast.LENGTH_SHORT).show();
                current_time.setText(hourOfDay + ":"+ minute);

                time = hourOfDay*3600*1000+minute*60000;

                SimpleDateFormat dateformat = new SimpleDateFormat("H:mm");
                currentTime = dateformat.format(Calendar.getInstance().getTime());

                calNow = Calendar.getInstance();
                calSet = (Calendar) calNow.clone();

                calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calSet.set(Calendar.MINUTE, minute);
                calSet.set(Calendar.SECOND, 0);
                calSet.set(Calendar.MILLISECOND, 0);

                if(calSet.compareTo(calNow) <= 0){
                    //Today Set time passed, count to tomorrow
                    calSet.add(Calendar.DATE, 1);
                }

            }
        },mHour,mMinute,true);
        timePickerDialog.show();
    }

    public void attachFile(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }
}
