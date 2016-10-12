package de.wackernagel.android.example.sidekick.provider;

import android.database.Cursor;

import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectCreator;

public class ArticleModel implements ObjectCreator<ArticleModel> {

    public static final ArticleModel FACTORY = new ArticleModel();

    private final long id;
    private final String title;

    private ArticleModel() {
        this.id = 0;
        this.title = "";
    }

    public ArticleModel( final long id, final String title) {
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

    @Override
    public ArticleModel createFromCursor(Cursor c) {
        return new ArticleModel( c.getLong(0), c.getString(1) );
    }
}
