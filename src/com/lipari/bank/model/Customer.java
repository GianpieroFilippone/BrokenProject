package com.lipari.bank.model;

public class Customer {

    private final String fiscalCode;
    private String firstName;
    private String lastName;
    private CustomerType customerType;

    public Customer(String fiscalCode, String firstName, String lastName, CustomerType customerType) {
        this.fiscalCode = fiscalCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.customerType = customerType;
    }

    public String getFiscalCode()          { return fiscalCode; }
    public String getFirstName()           { return firstName; }
    public String getLastName()            { return lastName; }
    public CustomerType getCustomerType()  { return customerType; }

    public void setFirstName(String firstName)           { this.firstName = firstName; }
    public void setLastName(String lastName)             { this.lastName = lastName; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " [" + fiscalCode + "] - " + customerType.getLabel();
    }
}
