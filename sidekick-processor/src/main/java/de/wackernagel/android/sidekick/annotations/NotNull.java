package de.wackernagel.android.sidekick.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static de.wackernagel.android.sidekick.annotations.ConflictClause.NONE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * A marker interface to make a SQLite columns NOT NULL inside a {@link Contract} annotated class.
 */
@Documented
@Retention( CLASS )
@Target( FIELD )
public @interface NotNull {

    ConflictClause onConflict() default NONE;

}
