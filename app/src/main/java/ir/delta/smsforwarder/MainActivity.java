package ir.delta.smsforwarder;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.work.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class MainActivity extends Activity {
    private static final int BG=0xff101a2a, PANEL=0xff1b293b, TEXT=0xfff3f7fc, MUTED=0xffa6b8cc, ACCENT=0xff4fe0b5, BORDER=0xff34485d;
    private LinearLayout root, history; private TextView status, preview; private EditText token, chat; private Switch enabled; private Spinner choices; private Store store; private Vault vault; private boolean configured=false;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable refresh=new Runnable(){public void run(){refreshHistory();handler.postDelayed(this,1500);}};
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private GradientDrawable bg(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private void add(LinearLayout parent,View v,int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(margin);parent.addView(v,p);}
    private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.START);t.setLineSpacing(dp(3),1);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout section(String title,String detail){LinearLayout p=column();p.setPadding(dp(18),dp(17),dp(18),dp(17));p.setBackground(bg(PANEL,20,BORDER));add(root,p,14);add(p,label(title,18,TEXT,true),0);if(!detail.isEmpty())add(p,label(detail,13,MUTED,false),5);return p;}
    private Button button(String text,boolean primary,Runnable click){Button b=new Button(this);b.setAllCaps(false);b.setText(text);b.setTextSize(14);b.setTextColor(primary?BG:TEXT);b.setMinHeight(dp(48));b.setPadding(dp(12),dp(8),dp(12),dp(8));b.setBackground(bg(primary?ACCENT:0xff24374c,12,primary?0:BORDER));b.setOnClickListener(v->click.run());return b;}
    private EditText field(String hint,int type){EditText e=new EditText(this);e.setSingleLine(true);e.setInputType(type);e.setHint(hint);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setTextSize(14);e.setTextDirection(View.TEXT_DIRECTION_LTR);e.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);e.setPadding(dp(13),dp(12),dp(13),dp(12));e.setBackground(bg(BG,12,BORDER));return e;}
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);store=new Store(this);vault=new Vault(this);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);root=column();root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(18),dp(28),dp(18),dp(32));scroll.addView(root);setContentView(scroll);
        add(root,label("DELTA  /  LAB",13,ACCENT,true),0);TextView brand=label("Delta SMS Forwarder",26,TEXT,true);brand.setTextDirection(View.TEXT_DIRECTION_LTR);add(root,brand,7);add(root,label("نسخه مستقل آزمایشی  •  0.1.0",14,MUTED,false),5);
        LinearLayout hero=section("مرکز کنترل","آزمایش اتصال و ارسال پیام‌های نمونه به ربات تلگرام");status=label("",15,ACCENT,true);add(hero,status,12);enabled=new Switch(this);enabled.setText("ارسال آزمایشی فعال باشد");enabled.setTextColor(TEXT);enabled.setChecked(store.enabled());add(hero,enabled,10);enabled.setOnCheckedChangeListener((v,on)->{if(on&&!configured){enabled.setChecked(false);toast("ابتدا تنظیمات اتصال را ذخیره کنید.");return;}store.enabled(on);if(!on)cancelPending();updateStatus();});
        LinearLayout config=section("تنظیمات ربات","توکن را فقط در گوشی خود وارد کنید. ابتدا در تلگرام به ربات پیام /start بدهید.");add(config,label("توکن ربات تلگرام",13,MUTED,true),12);token=field("123456789:…",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);token.setSaveEnabled(false);add(config,token,6);add(config,label("شناسه عددی مقصد (Chat ID)",13,MUTED,true),12);chat=field("123456789",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);chat.setSaveEnabled(false);add(config,chat,6);add(config,button("ذخیره تنظیمات",true,this::save),12);add(config,button("حذف تنظیمات اتصال",false,this::clearConfig),8);
        LinearLayout test=section("آزمایش ارسال","پیام‌ها از نمونه‌های داخلی ساخته می‌شوند؛ هیچ پیامک واقعی خوانده نمی‌شود.");add(test,button("تست اتصال تلگرام",false,()->enqueue(DemoMessage.CONNECTION)),12);choices=new Spinner(this);String[] options={DemoMessage.NORMAL.label,DemoMessage.PAYMENT.label,DemoMessage.OTP.label};ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,options);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);choices.setAdapter(adapter);choices.setBackground(bg(BG,12,BORDER));choices.setPadding(dp(12),dp(10),dp(12),dp(10));add(test,choices,12);preview=label("",14,TEXT,false);preview.setPadding(dp(12),dp(12),dp(12),dp(12));preview.setBackground(bg(BG,12,BORDER));add(test,preview,10);choices.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int i,long id){DemoMessage d=selected();preview.setText("فرستنده: "+d.sender+"\n\n"+d.body);}public void onNothingSelected(AdapterView<?> p){}});add(test,button("ارسال پیام نمونه",true,()->enqueue(selected())),12);
        LinearLayout logs=section("تاریخچه ارسال","آخرین ۵۰ آزمایش؛ وضعیت صف و نتیجه ارسال");history=column();add(logs,history,10);add(logs,button("پاک کردن تاریخچه",false,()->{store.clear();refreshHistory();}),12);
        LinearLayout info=section("حریم خصوصی و محدوده تست","این برنامه دسترسی SMS، مخاطبین یا اعلان‌های دیگر برنامه‌ها را ندارد. نمونه کد ورود ساختگی است. اطلاعات اتصال با Android Keystore رمزگذاری می‌شود.");add(info,label("پکیج مستقل: ir.delta.smsforwarder",12,MUTED,false),8);
        try{String[] c=vault.load();if(c!=null&&c.length==2){configured=true;chat.setText(c[1]);token.setHint("توکن ذخیره شده؛ برای تغییر دوباره وارد کنید");}}catch(Exception ignored){toast("تنظیمات قابل خواندن نیست؛ دوباره وارد کنید.");}if(!configured){store.enabled(false);enabled.setChecked(false);}updateStatus();refreshHistory();
    }
    private DemoMessage selected(){return new DemoMessage[]{DemoMessage.NORMAL,DemoMessage.PAYMENT,DemoMessage.OTP}[Math.max(0,Math.min(2,choices.getSelectedItemPosition()))];}
    private void updateStatus(){status.setText(!configured?"● نیازمند تنظیم اتصال":store.enabled()?"● آماده ارسال نمونه":"● ارسال متوقف است");status.setTextColor(configured&&store.enabled()?ACCENT:MUTED);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void cancelPending(){store.rotate();WorkManager.getInstance(this).cancelAllWorkByTag("delta-demo");JSONArray a=store.records();for(int i=0;i<a.length();i++){JSONObject j=a.optJSONObject(i);if(j!=null&&(j.optString("status").contains("صف")))store.record(j.optString("id"),j.optString("label"),j.optString("time"),"لغو شده");}refreshHistory();}
    private void save(){String t=token.getText().toString().trim(), c=chat.getText().toString().trim();try{if(t.isEmpty()&&configured){String[] old=vault.load();if(old!=null)t=old[0];}if(!t.matches("[0-9]{5,20}:[A-Za-z0-9_-]{20,100}")||!c.matches("-?[0-9]{1,20}")){toast("فرمت توکن یا شناسه عددی معتبر نیست.");return;}cancelPending();vault.save(t,c);configured=true;token.setText("");token.setHint("توکن ذخیره شده");updateStatus();toast("تنظیمات ذخیره شد.");}catch(Exception e){toast("ذخیره تنظیمات ناموفق بود.");}}
    private void clearConfig(){new android.app.AlertDialog.Builder(this).setTitle("حذف تنظیمات؟").setMessage("ارسال‌های در انتظار لغو و توکن حذف می‌شود.").setNegativeButton("انصراف",null).setPositiveButton("حذف",(d,w)->{enabled.setChecked(false);store.enabled(false);cancelPending();vault.clear();configured=false;token.setText("");chat.setText("");token.setHint("123456789:…");updateStatus();}).show();}
    private void enqueue(DemoMessage d){if(!configured||!store.enabled()){toast("ابتدا تنظیمات را ذخیره و ارسال را فعال کنید.");return;}String id=UUID.randomUUID().toString(), time=Store.now();store.record(id,d.label,time,"در صف ارسال");Data data=new Data.Builder().putString("demo",d.name()).putString("id",id).putString("time",time).putString("generation",store.generation()).build();OneTimeWorkRequest work=new OneTimeWorkRequest.Builder(SendWorker.class).setInputData(data).setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).setBackoffCriteria(BackoffPolicy.EXPONENTIAL,30,TimeUnit.SECONDS).addTag("delta-demo").build();WorkManager.getInstance(this).enqueue(work);refreshHistory();toast("نمونه به صف ارسال اضافه شد.");}
    private void refreshHistory(){if(history==null)return;history.removeAllViews();JSONArray a=store.records();if(a.length()==0){add(history,label("هنوز آزمایشی ثبت نشده است.",14,MUTED,false),0);return;}for(int i=0;i<a.length();i++){JSONObject j=a.optJSONObject(i);if(j==null)continue;LinearLayout item=column();item.setBackground(bg(BG,12,0));item.setPadding(dp(12),dp(10),dp(12),dp(10));add(history,item,7);add(item,label(j.optString("label"),14,TEXT,true),0);String state=j.optString("status");add(item,label(state,13,state.equals("ارسال موفق")?ACCENT:MUTED,true),4);add(item,label(j.optString("time"),11,MUTED,false),3);}}
    @Override protected void onResume(){super.onResume();handler.removeCallbacks(refresh);handler.post(refresh);}
    @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
