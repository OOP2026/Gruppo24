package model;

import utils.Validators;

public abstract class Utente {

    private final String login;
    private String password;

    public Utente(String login, String password) {

        Validators.validaStringa(login, "Login");
        Validators.validaStringa(password, "Password");

        this.login = login;
        this.password = password;

    }

    public String getLogin() { return login; }

    public String getPassword() { return password; }

    public void setPassword(String password) {

        Validators.validaStringa(password, "Nuova Password");

        this.password = password;

    }

}