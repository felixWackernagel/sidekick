package de.wackernagel.android.example.sidekick.provider;

import android.database.Cursor;

import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectCreator;

public class ArticleModel {

    public static final ObjectCreator<ArticleModel> FACTORY = new ObjectCreator<ArticleModel>() {
        @Override
        public ArticleModel createFromCursor(Cursor cursor) {
            return new ArticleModel( cursor.getLong(0), cursor.getString(1) );
        }
    };

    private final long id;
    private final String title;

    ArticleModel( final long id, final String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Article(" + id + ", " + title + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleModel that = (ArticleModel) o;

        if (id != that.id) return false;
        return title.equals(that.title);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + title.hashCode();
        return result;
    }

}
