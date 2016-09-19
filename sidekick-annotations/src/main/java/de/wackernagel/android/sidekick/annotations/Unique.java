package de.wackernagel.android.sidekick.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * A marker interface to make a SQLite columns UNIQUE inside a {@link Contract} annotated class.
 */
@Documented
@Retention( CLASS )
@Target( FIELD )
public @interface Unique {

    OnConflict onConflict() default OnConflict.NOTHING;

    enum OnConflict {

        ROLLBACK( "ROLLBACK" ),
        ABORT( "ABORT" ),
        FAIL( "FAIL" ),
        IGNORE( "IGNORE" ),
        REPLACE( "REPLACE" ),
        NOTHING( "" );

        private String asSql;

        OnConflict( String asSql ) {
            this.asSql = asSql;
        }

        public String getAsSql() {
            return asSql;
        }

    }

}
