package fr.manu.sprinvoice.dto;

import java.util.ArrayList;
import java.util.List;

public class QuoteFormDTO {

    private int id;
    private String designation;
    private int customerId;
    private String expiresAt; // format yyyy-MM-dd
    private String status;
    private List<RowFormDTO> rows = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<RowFormDTO> getRows() { return rows; }
    public void setRows(List<RowFormDTO> rows) { this.rows = rows; }
}
