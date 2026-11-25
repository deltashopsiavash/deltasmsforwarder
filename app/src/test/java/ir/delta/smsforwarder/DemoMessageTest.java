package ir.delta.smsforwarder;
import org.junit.Test;
import static org.junit.Assert.*;
public class DemoMessageTest {
    private static final String TIME="2026-11-25 12:00:00 +0330", ID="00000000-0000-0000-0000-000000000000";
    @Test public void demoMessagesRemainSynthetic(){for(DemoMessage d:DemoMessage.values()){if(d.isNotice())continue;String s=d.render(TIME,ID);assertTrue(s.contains("TEST"));assertTrue(d.sender.startsWith("DEMO-"));assertTrue(s.contains("پیام ساختگی"));}}
    @Test public void otpIsPlaceholder(){assertEquals("DEMO-LOGIN",DemoMessage.OTP.sender);assertTrue(DemoMessage.OTP.body.contains("DEMO-CODE"));}
    @Test public void exactOrderNoticeAllowed(){assertEquals(DemoMessage.ORDER_READY,DemoMessage.fromSharedText("سفارش شما آماده تحویل است."));assertEquals(DemoMessage.ORDER_SHIPPED,DemoMessage.fromSharedText("Your order has been shipped."));assertEquals(DemoMessage.ORDER_DELIVERED,DemoMessage.fromSharedText("Your order has been delivered."));assertEquals(DemoMessage.ORDER_CANCELLED,DemoMessage.fromSharedText("سفارش شما لغو شد."));}
    @Test public void arbitraryAndSensitiveMessagesRejected(){for(String s:new String[]{"رمز ورود: 123456","Your verification code is 654321","Your order is ready for pickup. 654321","سفارش شما آماده تحویل است.\nرمز: 123456","https://example.com/token","سفارش شما آماده تحویل است. ۱۲۳۴۵۶","سفارش شماره 123 آماده است."})assertNull(DemoMessage.fromSharedText(s));assertNull(DemoMessage.fromSharedText(null));}
    @Test public void onlyCanonicalTextRendered(){DemoMessage d=DemoMessage.fromSharedText("سفارش شما آماده تحویل است.");String s=d.render(TIME,ID);assertTrue(s.contains("سفارش آماده تحویل است."));assertFalse(s.contains("سفارش شما آماده تحویل است."));assertTrue(s.contains("زمان تایید ارسال"));}
}
