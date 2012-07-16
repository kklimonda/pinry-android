package pl.synth.pinry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

class Pin {
    private static final String TAG = "Pin";

    private Context context;
    private int id;
    private String localPath;
    private String sourceUrl;
    private String thumbnailPath;
    private String description;
    private String imageUrl;
    private long publishedDate;

    public Pin(Context context, int id, String sourceUrl, String localPath, String description, String imageUrl, long publishedDate) {
        this.context = context;
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.localPath = localPath;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publishedDate = publishedDate;

        processImage();
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

    private void processImage() {
        String fileName = Tools.last(localPath.split("/"));
        File thumbnailPath = context.getExternalFilesDir("thumbnails");
        File thumbnailFile = new File(thumbnailPath, fileName);

        if (thumbnailFile.exists()) {
            this.thumbnailPath = thumbnailFile.getAbsolutePath();
            return;
        }

        final int IMAGE_MAX_SIZE = 200000;
        /* first just get the image size */
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localPath, opts);


        int scale = 1;
        while ((opts.outWidth * opts.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
            scale *= 2;
        }

        Bitmap thumbnail;
        if (scale > 1) {
            scale--;
            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;
            thumbnail = BitmapFactory.decodeFile(localPath, opts);

            int width = thumbnail.getWidth();
            int height = thumbnail.getHeight();

            double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
            double x = (y / height) * width;

            thumbnail = Bitmap.createScaledBitmap(thumbnail, (int) x, (int) y, true);

            Bitmap.CompressFormat compress;
            compress = Bitmap.CompressFormat.JPEG;

            if(opts.outMimeType == "image/png") {
                compress = Bitmap.CompressFormat.PNG;
            } else if (opts.outMimeType == "image/jpeg") {
                compress = Bitmap.CompressFormat.JPEG;
            }

            try {
                OutputStream out = new FileOutputStream(thumbnailFile.getAbsolutePath());
                thumbnail.compress(compress, 90, out);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Could not save thumbnail: " + e.getMessage());
                return;
            }
            this.thumbnailPath = thumbnailFile.getAbsolutePath();
            return;
        }

        this.thumbnailPath = localPath;
    }
}
