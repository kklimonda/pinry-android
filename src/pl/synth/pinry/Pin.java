package pl.synth.pinry;

import java.util.Date;

class Pin {
    private int id;
    private String localPath;
    private String sourceUrl;
    private String thumbnailPath;
    private String description;
    private String imageUrl;
    private long publishedDate;

    public Pin(int id, String sourceUrl, String localPath, String thumbnailPath, String description, String imageUrl, long publishedDate) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.localPath = localPath;
        this.thumbnailPath = thumbnailPath;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publishedDate = publishedDate;
    }

    public int getId() {
        return id;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getPublishedDate() {
        return publishedDate;
    }
}
