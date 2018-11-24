package University.Models;

import University.Info.MailServers;

public class User {
    private String password;
    private String username;
    private MailServers mailServers;

    public User(String username, String password, MailServers mailServers) {
        this.password = password;
        this.username = username;
        this.mailServers = mailServers;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public MailServers getMailServers() {
        return mailServers;
    }

    @Override
    public String toString() {
        return username;
    }
}
