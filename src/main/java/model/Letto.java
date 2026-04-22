package model;

import utils.Validators;

public class Letto {

    private final String codiceUnivoco;

    public Letto(String codiceUnivoco) {

        Validators.validaStringa(codiceUnivoco, "Codice Univoco Letto");

        this.codiceUnivoco = codiceUnivoco;

    }

    public String getCodiceUnivoco() { return codiceUnivoco; }

}
