package susankyatech.com.hisabkitab;

public class UserDataModel {

    public String user_id ,name, role, status;

    public UserDataModel() {}

    public UserDataModel(String user_id, String name, String role, String status) {
        this.user_id = user_id;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
