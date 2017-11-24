package styx.mobile.elxpos.application;

/**
 * Created by amalg on 16-11-2017.
 */

public class Constants {
    public static String dbStorageKey = "privateStorage";

    public static final long InvalidInt = -1L;
    public static final float InvalidFloat = -1.0f;

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


    }

    public interface DateFormat {
        String PrintDate = "DD-MMM-YYYY h:m:s a";
    }
}
