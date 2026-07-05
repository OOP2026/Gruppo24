package model;

public class Medico {

    private int idMedico;
    private String login;
    private String password;
    private String matricola;
    private String nome;
    private String cognome;
    private int idReparto;

    public Medico() {}

    public Medico(int idMedico, String login, String password, String matricola,
                  String nome, String cognome, int idReparto) {
        this.idMedico = idMedico;
        this.login = login;
        this.password = password;
        this.matricola = matricola;
        this.nome = nome;
        this.cognome = cognome;
        this.idReparto = idReparto;
    }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMatricola() { return matricola; }
    public void setMatricola(String matricola) { this.matricola = matricola; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public int getIdReparto() { return idReparto; }
    public void setIdReparto(int idReparto) { this.idReparto = idReparto; }

    @Override
    public String toString() {
        return "Dr. " + nome + " " + cognome + " (" + matricola + ")";
    }
}
