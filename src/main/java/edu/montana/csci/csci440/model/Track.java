package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;

    private String composer;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    private String albumTitle;
    private String artistName;

    public String mediaTypeName;

    public String genreName;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    private Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        albumTitle = results.getString("Title");
        artistName = results.getString("ArtistName");
        composer = results.getString("Composer");
        mediaTypeName = results.getString("Name");
        genreName = results.getString("Name");
    }

    public static Track find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT tracks.* , artists.Name AS ArtistName, albums.Title AS Title, media_types.Name AS Name FROM tracks\n" +
                     "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                     "INNER JOIN media_types ON tracks.MediaTypeId = media_types.MediaTypeId\n" +
                     "INNER JOIN genres ON tracks.GenreId = genres.GenreId\n" +
                     "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                     "WHERE tracks.TrackId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Long count() {   // if we cache it, we don't issue this query the second time. Only the first time
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        String cacheVal = redisClient.get(REDIS_CACHE_KEY);
        if (cacheVal == null){
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks")) {
                ResultSet results = stmt.executeQuery();
                if (results.next()) {
                    long count = results.getLong("Count");
                    redisClient.set(REDIS_CACHE_KEY, String.valueOf(count));
                    return count;
                } else {
                    throw new IllegalStateException("Should find a count!");
                }
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        }
        else{    // put the value in the cache and return it
            long longCache = Long.valueOf(cacheVal);
            return longCache;
        }

    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return MediaType.find(mediaTypeId);
    }

    public Genre getGenre() {
        return Genre.find(genreId);
    }
    public List<Playlist> getPlaylists(){
        return Playlist.getPlaylistForTrack(trackId);
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }
    public String getComposer(){ return composer;}

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public void setMediaType(MediaType mediaType) {
        mediaTypeId = mediaType.getMediaTypeId();
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public void setGenre(Genre genre) {
        genreId = genre.getGenreId();
    }

    public void setGenre(Track track) {
        genreId = track.getGenreId();
    }

    public String getArtistName() {

        return artistName;
    }

    public String getAlbumTitle() {

        return albumTitle;
    }

    public String getMediaTypeName(){
        return mediaTypeName;
    }

    public String getGenreName(){
        return genreName;
    }
    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.* , tracks.Milliseconds AS Milliseconds, artists.Name AS ArtistName, albums.Title AS Title FROM tracks\n " +
                "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                "WHERE tracks.name LIKE ?";
        args.add("%" + search + "%");



        // Here is an example of how to conditionally
        if (artistId != null) {
            query += " AND artists.ArtistId=? ";
            args.add(artistId);
        }

        if (albumId != null) {
            query += " AND albums.AlbumId=? ";
            args.add(albumId);
        }

        if (maxRuntime != null){
            query += " AND tracks.Milliseconds <= ?";
            args.add(maxRuntime);
        }

        if (minRuntime != null){
            query += " AND tracks.Milliseconds >= ?";
            args.add(minRuntime);
        }

        //  include the limit (you should include the page too :)
        query += " LIMIT ? OFFSET ?;";
        args.add(count);
        args.add(count*(page-1));


        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT tracks.*, artists.Name AS ArtistName, albums.Title AS Title FROM tracks\n " +
                "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                "WHERE tracks.name LIKE ?\n" +
                "ORDER BY "+ orderBy +" LIMIT ? OFFSET ?";
        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setInt(2, count);
            stmt.setInt(3, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
        /*
        String query = "SELECT * FROM tracks WHERE name LIKE ? LIMIT ?";
        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setInt(2, count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }*/
    }

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT * FROM tracks WHERE AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.* , artists.Name AS ArtistName, albums.Title AS Title FROM tracks\n" +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                             "ORDER BY "+ orderBy + ",trackId" +
                             " LIMIT ? OFFSET ?;"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO tracks (Name, AlbumId, MediaTypeId, GenreId, Composer, Milliseconds, Bytes, UnitPrice) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getAlbumId());
                stmt.setLong(3, this.getMediaTypeId());
                stmt.setLong(4, this.getGenreId());
                stmt.setString(5, this.getComposer());
                stmt.setLong(6, this.getMilliseconds());
                stmt.setLong(7, this.getBytes());
                stmt.setBigDecimal(8, this.getUnitPrice());

                stmt.executeUpdate();
                Jedis redisClient = new Jedis();
                redisClient.del(REDIS_CACHE_KEY);
                trackId = DB.getLastID(conn);
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }


    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("Name can't be null or blank!");
        }
        if (albumId == null || "".equals(albumId)) {
            addError("AlbumId can't be null!");
        }
        return !hasErrors();
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET Name=?, AlbumId=? WHERE TrackId=?")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getAlbumId());
                stmt.setLong(3, this.getTrackId());

                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }
    public static List<Track> getTrackinPlaylist(long id){
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.*, playlists.*, artists.Name as artistName, albums.Title as Title FROM tracks\n" +
                             "INNER JOIN playlist_track ON tracks.TrackId = playlist_track.TrackId\n" +
                             "INNER JOIN playlists ON playlist_track.PlaylistId = playlists.PlaylistId\n" +
                             "INNER JOIN albums ON tracks.AlbumId = albums.AlbumId\n" +
                             "INNER JOIN artists ON albums.ArtistId = artists.ArtistId\n" +
                             "WHERE playlists.PlaylistId = ? \n" +
                             "ORDER BY tracks.Name;"

             )) {
            stmt.setLong(1, id);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public void delete() {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM tracks WHERE TrackId=?")) {
                stmt.setLong(1, this.getTrackId());
                stmt.executeUpdate();
                Jedis redisClient = new Jedis();
                redisClient.del(REDIS_CACHE_KEY);
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
    }



}
