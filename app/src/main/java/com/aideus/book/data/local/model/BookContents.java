package com.aideus.book.data.local.model;

import android.net.Uri;

import java.io.File;
import java.util.List;

public class BookContents {

    private static final String FILE_BOOK_LOCAL_ASSET_URI = "file:///android_asset/book/";

    private List<BookContents.Chapter> chapters;

    private File baseDir = null;

    public int getChaptersCount() {
        return(chapters.size());
    }

    public String getChapterTitle(final int position) {
        return(chapters.get(position).title);
    }

    public void setBaseDir(final File baseDir) {
        this.baseDir = baseDir;
    }

    public String getChapterPath(final int position) {
        String file = getChapterFile(position);
        if (baseDir == null) {
            return (FILE_BOOK_LOCAL_ASSET_URI + file);
        }
        return (Uri.fromFile(new File(baseDir, file)).toString());
    }

    private String getChapterFile(final int position) {
        return(chapters.get(position).file);
    }

    private static class Chapter {

        String file;

        String title;

    }
}
