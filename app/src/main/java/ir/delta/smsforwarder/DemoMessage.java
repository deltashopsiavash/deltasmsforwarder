package ir.delta.smsforwarder;
public enum DemoMessage {
    CONNECTION("تست اتصال", "DEMO-SYSTEM", "اتصال نسخه آزمایشی برقرار است."),
    NORMAL("پیام عادی ساختگی", "DEMO-1000", "این پیام صرفاً برای آزمایش نرم‌افزار ساخته شده است."),
    PAYMENT("واریزی ساختگی", "DEMO-BANK", "واریزی نمونه: ۸۰٬۰۰۰ تومان. هیچ تراکنش واقعی انجام نشده است."),
    OTP("کد ورود ساختگی", "DEMO-LOGIN", "این نمونه رمز واقعی نیست: DEMO-CODE.");
    public final String label, sender, body;
    DemoMessage(String label, String sender, String body) { this.label=label; this.sender=sender; this.body=body; }
    public String render(String time, String id) {
        return "📨 Delta SMS Forwarder | TEST\n\nفرستنده: " + sender + "\nزمان: " + time + "\nشناسه تست: " + id + "\n\n" + body + "\n\n⚠️ پیام ساختگی؛ از صندوق پیامک دریافت نشده است.";
    }
}
