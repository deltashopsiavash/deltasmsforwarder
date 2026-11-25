package ir.delta.smsforwarder;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONObject;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
public final class SendWorker extends Worker {
    public SendWorker(@NonNull Context context,@NonNull WorkerParameters params){super(context,params);}
    @NonNull @Override public Result doWork(){
        Store store=new Store(getApplicationContext());
        String id=getInputData().getString("id"), time=getInputData().getString("time"), generation=getInputData().getString("generation");
        DemoMessage demo;
        try{demo=DemoMessage.valueOf(getInputData().getString("demo"));}catch(Exception e){return Result.failure();}
        if(id==null||!id.matches("[0-9a-f-]{36}")||time==null||!time.matches("[0-9 :+\\-]{20,30}")) return Result.failure();
        if(!store.enabled()||!store.generation().equals(generation)||isStopped())return Result.failure();
        String[] config;
        try{config=new Vault(getApplicationContext()).load();}catch(Exception e){store.record(id,demo.label,time,"خطا در خواندن تنظیمات");return Result.failure();}
        if(config==null||config.length!=2||!config[0].matches("[0-9]{5,20}:[A-Za-z0-9_-]{20,100}")||!config[1].matches("-?[0-9]{1,20}")){store.record(id,demo.label,time,"تنظیمات نامعتبر");return Result.failure();}
        HttpURLConnection connection=null;
        try{
            connection=(HttpURLConnection)new URL("https://api.telegram.org/bot"+config[0]+"/sendMessage").openConnection();
            connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(12000);connection.setReadTimeout(15000);connection.setRequestMethod("POST");connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            String data="chat_id="+URLEncoder.encode(config[1],"UTF-8")+"&text="+URLEncoder.encode(demo.render(time,id),"UTF-8");
            if(!store.enabled()||!store.generation().equals(generation)||isStopped())return Result.failure();
            try(java.io.OutputStream out=connection.getOutputStream()){out.write(data.getBytes(StandardCharsets.UTF_8));}
            int status=connection.getResponseCode();
            if(status==200){try(InputStream in=connection.getInputStream()){java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){bytes.write(buffer,0,n);if(bytes.size()>65536)throw new java.io.IOException();}String response=new String(bytes.toByteArray(),StandardCharsets.UTF_8);if(new JSONObject(response).optBoolean("ok")){store.record(id,demo.label,time,"ارسال موفق");return Result.success();}}}
            if((status==429||status>=500)&&getRunAttemptCount()<3){store.record(id,demo.label,time,"در صف تلاش مجدد");return Result.retry();}
            store.record(id,demo.label,time,"ارسال ناموفق (HTTP "+status+")");return Result.failure();
        }catch(Exception e){
            if(isStopped()||!store.enabled()||!store.generation().equals(generation))return Result.failure();
            if(getRunAttemptCount()<3){store.record(id,demo.label,time,"در صف تلاش مجدد");return Result.retry();}
            store.record(id,demo.label,time,"ناموفق؛ شبکه یا پاسخ نامشخص");return Result.failure();
        }finally{if(connection!=null)connection.disconnect();}
    }
}
