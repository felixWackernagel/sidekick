package de.wackernagel.android.sidekick.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * A marker interface to set a SQLite columns as Foreign Key inside a {@link Contract} annotated class.
 */
@Documented
@Retention( CLASS )
@Target( FIELD )
public @interface ForeignKey {

    Action onDelete() default Action.NONE;

    Action onUpdate() default Action.NONE;

    Relation relation() default Relation.AUTO;

    enum Action {
        NONE,
        SET_NULL,
        SET_DEFAULT,
        CASCADE,
        RESTRICT,
        NO_ACTION;

        @Override
        public String toString() {
            return super.toString().replace( '_', ' ' );
        }
    }

    enum Relation {
        AUTO,
        ONE_ONE,
        ONE_MANY,
        MANY_MANY;
    }

}
