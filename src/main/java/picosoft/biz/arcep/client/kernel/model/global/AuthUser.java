package picosoft.biz.arcep.client.kernel.model.global;



public class AuthUser {
    private String username;
    private String password;


    public AuthUser() {
        super();
    }

    public AuthUser(String username, String password) {
        super();
        this.username = username;
        this.password = password;

    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username.trim();
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username.trim();
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password.trim();
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password.trim();
    }


}
