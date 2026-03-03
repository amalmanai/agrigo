package Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class SmsService {

    public static final String ACCOUNT_SID = "TON_ACCOUNT_SID";
    public static final String AUTH_TOKEN = "TON_AUTH_TOKEN";
    public static final String FROM_NUMBER = "+123456789";

    public static void sendSMS(String toPhone, String code) {

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhone),
                new com.twilio.type.PhoneNumber(FROM_NUMBER),
                "Code de verification AGRIGO : " + code
        ).create();

        System.out.println("SMS envoyé : " + message.getSid());
    }
}