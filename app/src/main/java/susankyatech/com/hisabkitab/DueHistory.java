package susankyatech.com.hisabkitab;

public class DueHistory {
    public String userId, userName;
    public int dueAmount;

    public DueHistory(String userId, String userName, int dueAmount) {
        this.userId = userId;
        this.userName = userName;
        this.dueAmount = dueAmount;
    }
}
