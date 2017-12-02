package styx.mobile.elxpos.application;

/**
 * Created by amalg on 16-11-2017.
 */

public class Constants {
    public static String dbStorageKey = "privateStorage";

    public interface BundleKeys {
        String PersistedEntry = "PersistedEntry";
        String Entry = "Entry";
        String firstTime = "firstTime";
    }

    public interface DataBaseStorageKeys {
        String LastPrintedReceipt = "LastPrintedReceipt";
        String ContactNumber = "ContactNumber";
        String inputTitle1 = "inputTitle1";
        String inputTitle2 = "inputTitle2";
        String inputFooter1 = "inputFooter1";
        String inputFooter2 = "inputFooter2";
        String inputFooter3 = "inputFooter3";
        String inputFooter4 = "inputFooter4";
        String inputFooter5 = "inputFooter5";
    }

    public interface DateFormat {
        String PrintDate = "d-MMM-yyyy h:m:ss a";
    }
}
