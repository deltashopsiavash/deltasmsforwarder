package ir.delta.smsforwarder;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
final class Store {
    private static final Object LOCK=new Object();
    final SharedPreferences p;
    Store(Context c) { p=c.getSharedPreferences("demo",Context.MODE_PRIVATE); }
    boolean enabled(){return p.getBoolean("enabled",false);}
    void enabled(boolean e){p.edit().putBoolean("enabled",e).commit();}
    String generation(){return p.getString("generation", "initial");}
    void rotate(){p.edit().putString("generation",UUID.randomUUID().toString()).commit();}
    JSONArray records(){try{return new JSONArray(p.getString("history","[]"));}catch(Exception e){return new JSONArray();}}
    void record(String id,String label,String time,String status){ synchronized(LOCK){
        JSONArray old=records(), next=new JSONArray();
        try { JSONObject item=new JSONObject(); item.put("id",id);item.put("label",label);item.put("time",time);item.put("status",status);next.put(item);
            for(int i=0;i<old.length()&&next.length()<50;i++){JSONObject x=old.optJSONObject(i);if(x!=null&&!id.equals(x.optString("id")))next.put(x);}
            p.edit().putString("history",next.toString()).commit();
        }catch(Exception ignored){}
    }}
    void clear(){synchronized(LOCK){p.edit().remove("history").commit();}}
    static String now(){return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z",Locale.US).format(new Date());}
}
