package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playlist extends Model {

    Long playlistId;
    String name;

    public Playlist() {
    }

    private Playlist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        playlistId = results.getLong("PlaylistId");
    }

    public static List<Playlist> getPlaylistForTrack(Long trackId) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.*, playlists.*, artists.Name as artistName, albums.Title as albumTitle FROM tracks\n" +
                             "INNER JOIN playlist_track ON tracks.TrackId = playlist_track.TrackId\n" +
                             "INNER JOIN playlists ON playlist_track.PlaylistId = playlists.PlaylistId\n" +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                             "WHERE tracks.TrackId = ? \n" +
                             "GROUP BY playlists.PlaylistId;"

             )) {
            stmt.setLong(1, trackId);
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Playlist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }


    public List<Track> getTracks(){
        // TODO implement, order by track name
        return Track.getTrackinPlaylist(playlistId);
    }

    public Long getPlaylistId() {return playlistId;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Playlist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Playlist> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM playlists LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Playlist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Playlist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Playlist find(int i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM playlists WHERE PlaylistId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Playlist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public void delete() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM playlists WHERE PlaylistId=?")) {
            stmt.setLong(1, this.getPlaylistId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

}
