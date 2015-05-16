package org.coolfrood.winky;

public class NfcTag {
    public int id;
    public String name;
    public boolean ignored;
    public byte[] deviceId;

    public NfcTag(int id, String name, boolean ignored, byte[] deviceId) {
        this.id = id;
        this.name = name;
        this.ignored = ignored;
        this.deviceId = deviceId;
    }

    public String getDisplayName() {
        if (name != null)
            return name;
        return byteArrayToHex(this.deviceId);
    }

    public static String byteArrayToHex(byte[] b) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[b.length * 3 - 1];
        int v;
        for ( int j = 0; j < b.length; j++ ) {
            v = b[j] & 0xFF;
            hexChars[j*3] = hexArray[v/16];
            hexChars[j*3 + 1] = hexArray[v%16];
            if (j < b.length - 1) {
                hexChars[j*3 + 2] = ':';
            }
        }
        return new String(hexChars);
    }
}
