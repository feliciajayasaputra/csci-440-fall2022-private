package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;

    public Album() {
    }

    private Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }

    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) {
        artistId = artist.getArtistId();
    }

    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbum(Album album) {
        this.albumId = album.getAlbumId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (title == null || "".equals(title)) {
            addError("Title can't be null or blank!");
        }
        if (artistId == null || "".equals(artistId)) {
            addError("Artist can't be null or blank!");
        }
        return !hasErrors();
    }
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO albums (Title, ArtistId) VALUES (?, ?)")) {
                stmt.setString(1, this.getTitle());  // get whatever title of this current album object is and set that as the value we are going to insert into
                stmt.setLong(2, this.getArtistId());
                stmt.executeUpdate();
                albumId = DB.getLastID(conn);  // this is how we get the synthetic id that was generated when we inserted this album into this database
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE albums SET Title=?, ArtistId=? WHERE AlbumId=?")) {
                stmt.setString(1, this.getTitle());
                stmt.setLong(2, this.getArtistId());
                stmt.setLong(3, this.getAlbumId());
                int updatedCount = stmt.executeUpdate();
                /*if (updatedCount >= 1){
                    return true;
                }
                // how many rows are updated then return false
                else{
                    return false;
                }
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }*/

                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Album> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM albums LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            /*if(page>1){
                stmt.setInt(2, count*(page-1));
            }
            else{
                stmt.setInt(2, page);
            }*/
            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Album find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Album(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Album> getForArtist(Long artistId) {
        String query = "SELECT * FROM albums WHERE artistId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, artistId);
            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        // TODO implement
        //return Collections.emptyList();
    }

    public void delete() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, this.getAlbumId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
