package susankyatech.com.hisabkitab;

public class UserDataModel {

    private String user_name;

    public UserDataModel(){

    }

    public UserDataModel(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}
