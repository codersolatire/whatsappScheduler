package www.geekphisher.com.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.io.File;

public class Scheduler extends BroadcastReceiver {
    String msg;
    String toNumber;
    String filepath;
    Boolean fileB;
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = context.getSharedPreferences("details",Context.MODE_PRIVATE);
        toNumber = sharedPreferences.getString("toNumber","");
        toNumber = toNumber.replace("+","");
        toNumber = toNumber.replace(" ","");
        msg = sharedPreferences.getString("msg","");
        filepath = sharedPreferences.getString("filepath","");
        fileB = sharedPreferences.getBoolean("fileB",false);

        if(!fileB) {
            try {
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber+"&text="+msg));
                context.startActivity(intent1);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filepath));
            sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setType("image/*");
            context.startActivity(sendIntent);
        }
    }
}
