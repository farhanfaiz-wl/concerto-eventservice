package ai.concerto.event.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneUtils {

  public static String standardizedPhoneNumber(String phoneNumber) {
    phoneNumber = phoneNumber.trim();
    if (phoneNumber.startsWith("+"))
      return phoneNumber;

    return "+" + phoneNumber;
  }

  public static String standardizedPhoneNumberWithCountryCode(String phoneNumber,
      String countryCode) {
    phoneNumber = phoneNumber.trim();
    phoneNumber = phoneNumber.replaceAll("\\s", "").replace("(", "").replace(")", "");
    if (phoneNumber.startsWith("+")) {
      return phoneNumber;
    } else if (phoneNumber.length() > 10) {
      return "+" + phoneNumber;
    } else if (countryCode.startsWith("+")) {
      return countryCode + phoneNumber;
    } else {
      return "+" + countryCode + phoneNumber;
    }

  }

  public static PhoneNumber getPhoneNumber(String phoneNumber) throws NumberParseException {
    return PhoneNumberUtil.getInstance().parse(phoneNumber, "");
  }
}
