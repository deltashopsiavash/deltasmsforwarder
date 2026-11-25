package ir.delta.smsforwarder;
public enum DemoMessage {
    CONNECTION("تست اتصال", "DEMO-SYSTEM", "اتصال نسخه آزمایشی برقرار است."),
    NORMAL("پیام عادی ساختگی", "DEMO-1000", "این پیام صرفاً برای آزمایش نرم‌افزار ساخته شده است."),
    PAYMENT("واریزی ساختگی", "DEMO-BANK", "واریزی نمونه: ۸۰٬۰۰۰ تومان. هیچ تراکنش واقعی انجام نشده است."),
    OTP("کد ورود ساختگی", "DEMO-LOGIN", "این نمونه رمز واقعی نیست: DEMO-CODE."),
    ORDER_READY("سفارش آماده", "", "سفارش آماده تحویل است."),
    ORDER_SHIPPED("سفارش ارسال شد", "", "سفارش ارسال شده است."),
    ORDER_DELIVERED("سفارش تحویل شد", "", "سفارش تحویل داده شده است."),
    ORDER_CANCELLED("سفارش لغو شد", "", "سفارش لغو شده است.");
    public final String label, sender, body;
    DemoMessage(String label,String sender,String body){this.label=label;this.sender=sender;this.body=body;}
    public boolean isNotice(){return sender.isEmpty();}
    public String render(String time,String id){
        if(isNotice())return "📨 Delta SMS Forwarder | اعلان انتخابی\n\nنوع: "+label+"\nزمان تایید ارسال: "+time+"\nشناسه: "+id+"\n\n"+body+"\n\nارسال دستی؛ شماره فرستنده و زمان دریافت پیامک در دسترس نیست.";
        return "📨 Delta SMS Forwarder | TEST\n\nفرستنده: "+sender+"\nزمان: "+time+"\nشناسه تست: "+id+"\n\n"+body+"\n\n⚠️ پیام ساختگی؛ از صندوق پیامک دریافت نشده است.";
    }
    public static DemoMessage fromSharedText(CharSequence input){
        if(input==null || input.length()>150)return null;
        String s=input.toString().trim();
        switch(s){
            case "سفارش آماده تحویل است.":
            case "سفارش شما آماده تحویل است.":
            case "Your order is ready for pickup.": return ORDER_READY;
            case "سفارش ارسال شده است.":
            case "سفارش شما ارسال شد.":
            case "Your order has been shipped.": return ORDER_SHIPPED;
            case "سفارش تحویل داده شده است.":
            case "سفارش شما تحویل داده شد.":
            case "Your order has been delivered.": return ORDER_DELIVERED;
            case "سفارش لغو شده است.":
            case "سفارش شما لغو شد.":
            case "Your order has been cancelled.": return ORDER_CANCELLED;
            default:return null;
        }
    }
}
