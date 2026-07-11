package model;

public class Amministratore {

    private int idAdmin;
    private String login;
    private String password;

    public Amministratore(int idAdmin, String login, String password) {
        this.idAdmin = idAdmin;
        this.login = login;
        this.password = password;
    }

    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int idAdmin) { this.idAdmin = idAdmin; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "Amministratore{id=" + idAdmin + ", login='" + login + "'}";
    }
}
