package fr.manu.sprinvoice.dto;

public class InvoiceFormDTO {

    private int id;
    private String designation;
    private int customerId;
    private String invoicedAt; // format yyyy-MM-dd, vide si non renseigné
    private String paidAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getInvoicedAt() { return invoicedAt; }
    public void setInvoicedAt(String invoicedAt) { this.invoicedAt = invoicedAt; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
}
