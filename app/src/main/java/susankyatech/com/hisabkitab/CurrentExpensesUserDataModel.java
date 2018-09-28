package susankyatech.com.hisabkitab;

public class CurrentExpensesUserDataModel {

    public int amount;
    public String product_name, id, date;

    public CurrentExpensesUserDataModel(){ }

    public CurrentExpensesUserDataModel(int amount, String product_name, String id, String date) {
        this.amount = amount;
        this.product_name = product_name;
        this.id = id;
        this.date = date;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
