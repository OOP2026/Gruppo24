package model;

import java.time.LocalDate;

public class Paziente {

    private int idPaziente;
    private String codiceFiscale;
    private String nome;
    private String cognome;
    private LocalDate dataNascita;

    public Paziente() {}

    public Paziente(int idPaziente, String codiceFiscale, String nome,
                    String cognome, LocalDate dataNascita) {
        this.idPaziente = idPaziente;
        this.codiceFiscale = codiceFiscale;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
    }

    public int getIdPaziente() { return idPaziente; }
    public void setIdPaziente(int idPaziente) { this.idPaziente = idPaziente; }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public LocalDate getDataNascita() { return dataNascita; }
    public void setDataNascita(LocalDate dataNascita) { this.dataNascita = dataNascita; }

    @Override
    public String toString() {
        return nome + " " + cognome + " (" + codiceFiscale + ")";
    }
}
