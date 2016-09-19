package de.wackernagel.android.sidekick.annotations.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public abstract class Definition {

    protected final Types types;
    protected final Elements elements;
    protected final Messager log;

    public Definition(Types types, Elements elements, Messager log) {
        this.types = types;
        this.elements = elements;
        this.log = log;
    }
}
