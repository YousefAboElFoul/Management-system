public class WithdrawMessageI {

    final static public int WDI_CODE = Message.WITHDRAW_1_CODE;
    public String MT_NUMBER;
    public String IP_ADDRESS;

    public WithdrawMessageI(String MT_NUMBER, String IP_ADDRESS) {
        this.MT_NUMBER = MT_NUMBER;
        this.IP_ADDRESS = IP_ADDRESS;
    }

    public static int getWDI_CODE() {
        return WDI_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String getIP_ADDRESS() {
        return IP_ADDRESS;
    }

    public void setIP_ADDRESS(String IP_ADDRESS) {
        this.IP_ADDRESS = IP_ADDRESS;
    }

    public String printWithdrawIMessage() {
        String output = "{" + this.getWDI_CODE() + " | " + this.getMT_NUMBER() + " | " + this.getIP_ADDRESS() + "}";
        return output;
    }
}
