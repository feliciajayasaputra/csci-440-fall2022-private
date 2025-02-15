package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Invoice extends Model {

    Long invoiceId;
    Long customerId;
    String invoiceDate;
    String billingAddress;
    String billingCity;
    String billingState;
    String billingCountry;
    String billingPostalCode;
    BigDecimal total;




    public Invoice() {
        // new employee for insert
    }

    private Invoice(ResultSet results) throws SQLException {
        invoiceId = results.getLong("InvoiceId");
        customerId = results.getLong("CustomerId");
        invoiceDate = results.getString("InvoiceDate");
        billingAddress = results.getString("BillingAddress");
        billingCity = results.getString("BillingCity");
        billingState = results.getString("BillingState");
        billingCountry = results.getString("BillingCountry");
        billingPostalCode = results.getString("BillingPostalCode");
        total = results.getBigDecimal("Total");
    }



    public List<InvoiceItem> getInvoiceItems(){
        return InvoiceItem.getInvoiceItem(invoiceId);
        //TODO implement
        //return Collections.emptyList();
    }
    public Customer getCustomer() {
        return null;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public Long getCustomerId(){ return customerId;}

    public String getInvoiceDate(){ return invoiceDate;}

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public static List<Invoice> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Invoice> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM invoices LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Invoice> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Invoice(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Invoice find(long invoiceId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM invoices WHERE InvoiceId=?")) {
            stmt.setLong(1, invoiceId);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Invoice(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Invoice> getInvoicesToCustomers(Long customerId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT customers.*, invoices.*, invoices.InvoiceId as InvoiceId, invoices.CustomerId as CustomerID, invoices.InvoiceDate as InvoiceDate, invoices.BillingAddress as BillingAddress, invoices.BillingCity as BillingCity, invoices.BillingState as BillingState, invoices.BillingCountry as BillingCountry, invoices.BillingPostalCode as BillingPostalCode, invoices.Total as Total FROM invoices\n" +
                             "INNER JOIN customers ON invoices.CustomerId = customers.CustomerId\n" +
                             "WHERE customers.CustomerId = ? \n;"

             )) {
            stmt.setLong(1, customerId);
            ResultSet results = stmt.executeQuery();
            List<Invoice> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Invoice(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


}
