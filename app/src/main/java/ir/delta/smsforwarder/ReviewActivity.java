package ir.delta.smsforwarder;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.WindowManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import androidx.work.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ReviewActivity extends Activity {
    private static final int BG=0xff101a2a, PANEL=0xff1b293b, TEXT=0xfff3f7fc, MUTED=0xffa6b8cc, ACCENT=0xff4fe0b5;
    private LinearLayout root;
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private void label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.START);t.setPadding(0,dp(10),0,dp(10));root.addView(t);}
    private void button(String s,boolean primary,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(primary?BG:TEXT);b.setMinHeight(dp(48));GradientDrawable bg=new GradientDrawable();bg.setColor(primary?ACCENT:PANEL);bg.setCornerRadius(dp(12));b.setBackground(bg);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(12);root.addView(b,p);b.setOnClickListener(v->{b.setEnabled(false);action.run();});}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);scroll.setFillViewport(true);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(android.view.View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(22),dp(40),dp(22),dp(40));scroll.addView(root);setContentView(scroll);
        label("DELTA  /  REVIEW",14,ACCENT);label("بررسی پیام انتخابی",25,TEXT);
        Intent incoming=getIntent();
        CharSequence raw=null;
        if(Intent.ACTION_SEND.equals(incoming.getAction()) && "text/plain".equals(incoming.getType())){
            try{raw=incoming.getCharSequenceExtra(Intent.EXTRA_TEXT);}catch(RuntimeException ignored){}
        }
        final DemoMessage notice=DemoMessage.fromSharedText(raw);
        // No original text, sender, or extras are persisted or passed to a worker.
        if(raw!=null&&raw.length()>0&&raw.length()<=2000){
            label("پیش‌نمایش محلی (ارسال نمی‌شود)",14,MUTED);
            label(raw.toString(),16,TEXT);
        }else label("پیام قابل نمایش نیست.",16,MUTED);
        setIntent(new Intent());
        if(notice==null){label("این پیام در فهرست اعلان‌های مجاز نیست. هیچ داده‌ای به ربات ارسال نمی‌شود.",16,MUTED);button("بستن",false,this::finish);return;}
        label("فقط متن استاندارد زیر به ربات ارسال می‌شود:",14,ACCENT);
        label(notice.body,18,TEXT);
        label("زمان ثبت، زمان تایید شماست؛ برنامه پیامک شماره فرستنده و زمان اصلی دریافت را از طریق اشتراک‌گذاری ارائه نمی‌کند.",14,MUTED);
        button("تایید و ارسال اعلان",true,()->enqueue(notice));
        button("انصراف",false,this::finish);
    }
    private void enqueue(DemoMessage notice){
        Store store=new Store(this);
        try{String[] c=new Vault(this).load();if(!store.enabled()||c==null||c.length!=2){Toast.makeText(this,"ابتدا تنظیمات ربات و ارسال را در صفحه اصلی فعال کنید.",Toast.LENGTH_LONG).show();finish();return;}}
        catch(Exception e){Toast.makeText(this,"تنظیمات اتصال قابل خواندن نیست.",Toast.LENGTH_LONG).show();finish();return;}
        String id=UUID.randomUUID().toString(),time=Store.now();
        store.record(id,notice.label,time,"در صف ارسال");
        Data data=new Data.Builder().putString("demo",notice.name()).putString("id",id).putString("time",time).putString("generation",store.generation()).build();
        OneTimeWorkRequest request=new OneTimeWorkRequest.Builder(SendWorker.class).setInputData(data).setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).addTag("delta-demo").build();
        WorkManager.getInstance(this).enqueue(request);
        Toast.makeText(this,"اعلان به صف ارسال اضافه شد.",Toast.LENGTH_SHORT).show();finish();
    }
}
