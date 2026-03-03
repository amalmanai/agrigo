package Utils;

public class SmsUtilTest {
    public static void main(String[] args) {
        String to = null;
        String text = null;
        if (args.length >= 1) to = args[0];
        if (args.length >= 2) text = args[1];
        if (to == null || to.isEmpty()) {
            to = System.getenv("TEST_SMS_TO");
        }
        if (text == null || text.isEmpty()) {
            text = System.getenv("TEST_SMS_TEXT");
        }
        if (to == null || to.isEmpty()) {
            System.err.println("Usage: java Utils.SmsUtilTest <phone> [message]  OR set TEST_SMS_TO and TEST_SMS_TEXT env vars");
            System.exit(2);
        }
        if (text == null || text.isEmpty()) text = "Test SMS depuis SmsUtilTest";

        try {
            boolean ok = SmsUtil.sendSms(to, text);
            System.out.println("SmsUtilTest: result=" + ok);
        } catch (Exception ex) {
            System.err.println("SmsUtilTest: exception: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(3);
        }
    }
}

