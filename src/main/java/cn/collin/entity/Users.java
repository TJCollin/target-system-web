package cn.collin.entity;

import java.io.Serializable;

/**
 * Created by Collin on 2017/4/14.
 */
public class Users implements Serializable{

    private static final long serialVersionUID = 5469888592132780819L;
    private String valA;
    private String valB;
    private String valC;
    private String valD;
    private String valE;

    private String queryUser;



    private String invokeA;
    private String invokeB;
    private String amount;

    public Users(){

    }
    //init chaincode
    public Users(String valA, String valB, String valC, String valD, String valE) {
        this.valA = valA;
        this.valB = valB;
        this.valC = valC;
        this.valD = valD;
        this.valE = valE;
    }

    //query chaincode
    public Users(String queryUser) {
        this.queryUser = queryUser;
    }

    //invoke chaincode
    public Users(String valA, String invokeA, String invokeB, String amount) {
        this.valA = valA;
        this.invokeA = invokeA;
        this.invokeB = invokeB;
        this.amount = amount;
    }


    /******************************getters and setters**************************************/
    public String getInvokeA() {
        return invokeA;
    }

    public void setInvokeA(String invokeA) {
        this.invokeA = invokeA;
    }

    public String getInvokeB() {
        return invokeB;
    }

    public void setInvokeB(String invokeB) {
        this.invokeB = invokeB;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getValA() {
        return valA;
    }

    public void setValA(String valA) {
        this.valA = valA;
    }

    public String getValB() {
        return valB;
    }


    public void setValB(String valB) {
        this.valB = valB;
    }

    public String getValC() {
        return valC;
    }

    public void setValC(String valC) {
        this.valC = valC;
    }

    public String getValD() {
        return valD;
    }

    public void setValD(String valD) {
        this.valD = valD;
    }

    public String getValE() {
        return valE;
    }

    public void setValE(String valE) {
        this.valE = valE;
    }

    public String getQueryUser() {
        return queryUser;
    }

    public void setQueryUser(String queryUser) {
        this.queryUser = queryUser;
    }
    /******************************getters and setters**************************************/




}
