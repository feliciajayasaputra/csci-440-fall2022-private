package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class InvoiceItem extends Model {



    Long invoiceLineId;
    Long invoiceId;
    Long trackId;
    BigDecimal unitPrice;
    Long quantity;

    String artistName;

    String albumTitle;

    String trackName;
    private InvoiceItem(ResultSet results) throws SQLException {
        invoiceLineId = results.getLong("InvoiceLineId");
        invoiceId = results.getLong("InvoiceId");
        trackId = results.getLong("TrackId");
        unitPrice = results.getBigDecimal("UnitPrice");
        quantity = results.getLong("Quantity");
        trackName = results.getString("TrackName");
        albumTitle = results.getString("AlbumTitle");
        artistName = results.getString("ArtistName");
    }
    public String getArtistName() {

        return artistName;
    }

    public String getAlbumTitle() {

        return albumTitle;
    }

    public String getTrackName() {

        return trackName;
    }

    public Track getTrack() {
        return null;
    }
    public Invoice getInvoice() {
        return null;
    }

    public Long getInvoiceLineId() {
        return invoiceLineId;
    }

    public void setInvoiceLineId(Long invoiceLineId) {
        this.invoiceLineId = invoiceLineId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public static List<InvoiceItem> getInvoiceItem(Long invoiceId) {

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT invoices.*, invoice_items.*, tracks.Name as trackName, albums.Title as albumTitle, artists.Name as artistName FROM invoice_items\n" +
                             "INNER JOIN tracks ON invoice_items.TrackId = tracks.TrackId\n" +
                             "INNER JOIN invoices on invoice_items.InvoiceId = invoices.InvoiceId\n" +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                             "WHERE invoices.InvoiceId = ? \n;"

             )) {
            stmt.setLong(1, invoiceId);
            ResultSet results = stmt.executeQuery();
            List<InvoiceItem> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new InvoiceItem(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        // TODO implement
        //return Collections.emptyList();
    }
/*
    public static List<InvoiceItem> getInvoice(Long invoiceId){
        String query = "SELECT invoice_items.*, invoices.*, tracks.Name as trackName, albums.Title as albumTitle, artists.Name as artistName FROM invoice_items \n" +
                "INNER JOIN tracks ON invoice_items.TrackId = tracks.TrackId\n" +
                "INNER JOIN invoices on invoice_items.InvoiceId = invoices.InvoiceId\n" +
                "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                "WHERE invoices.InvoiceId = ?;";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, invoiceId);
            ResultSet results = stmt.executeQuery();
            List<InvoiceItem> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new InvoiceItem(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        // TODO implement
        //return Collections.emptyList();
    }*/
}
