package ir.delta.smsforwarder;
import org.junit.Test;
import static org.junit.Assert.*;
public class DemoMessageTest {
    @Test public void allMessagesAreSynthetic(){for(DemoMessage d:DemoMessage.values()){String s=d.render("2026-11-25 12:00:00 +0330","00000000-0000-0000-0000-000000000000");assertTrue(s.contains("TEST"));assertTrue(d.sender.startsWith("DEMO-"));assertTrue(s.contains("پیام ساختگی"));}}
    @Test public void otpIsPlaceholder(){assertEquals("DEMO-LOGIN",DemoMessage.OTP.sender);assertTrue(DemoMessage.OTP.body.contains("DEMO-CODE"));}
}
