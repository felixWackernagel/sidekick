package de.wackernagel.android.sidekick.annotations.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class Definition {

    protected final Types types;
    protected final Elements elements;
    protected final Messager log;

    public Definition( final Types types, final Elements elements, final Messager log ) {
        this.types = types;
        this.elements = elements;
        this.log = log;
    }

    protected String formatNameForSQL( final String originName ) {
        final StringBuilder sb = new StringBuilder(originName);
        final int length = sb.length();
        int offset = 0;
        for (int index = 0; index < length; index++) {
            if (index > 0 && Character.isUpperCase(originName.charAt(index))) {
                sb.insert(index + offset, "_");
                offset++;
            }
        }
        return sb.toString().toLowerCase();
    }
}