package susankyatech.com.hisabkitab;

public class CurrentExpensesUserDataModel {

    public int amount;
    public String product_name;

    public CurrentExpensesUserDataModel(){ }

    public CurrentExpensesUserDataModel(int amount, String product_name) {
        this.amount = amount;
        this.product_name = product_name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }
}
