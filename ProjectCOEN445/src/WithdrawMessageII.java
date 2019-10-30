public class WithdrawMessageII {

    final static public int WDII_CODE = Message.WITHDRAW_2_CODE;
    public String MT_NUMBER;

    public WithdrawMessageII(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public static int getWDII_CODE() {
        return WDII_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String printWithdrawIIMessage() {
        String output = "{" + this.getWDII_CODE() + " | " + this.getMT_NUMBER() + "}";
        return output;
    }
}
