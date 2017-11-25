package styx.mobile.elxpos.application;

/**
 * Created by amalg on 16-11-2017.
 */

public class Constants {
    public static String dbStorageKey = "privateStorage";

    public interface BundleKeys {
        String DeviceName = "DeviceName";
        String PersistedEntry = "PersistedEntry";
        String Entry = "Entry";
    }

    public interface RequestCodes {
        int SelectDevice = 0;
    }

    public interface DataBaseStorageKeys {
        String Device = "Device";
        String LastPrintedReceipt = "LastPrintedReceipt";
        String ContactNumber= "ContactNumber";
    }

    public interface DateFormat {
        String PrintDate = "d-mmm-yyyy h:m:s a";
    }
}
