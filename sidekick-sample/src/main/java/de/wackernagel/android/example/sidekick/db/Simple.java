package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = Constants.AUTHORITY )
public class Simple {

    @Column
    short aShort;

    @Column
    long aLong;

    @Column
    double aDouble;

    @Column
    int aInt;

    @Column
    String aString;

    @Column
    boolean aBoolean;

    @Column
    float aFloat;

    @Column
    byte[] aByteArray;

    @Column
    byte aByte;

    @Column
    Short bShort;

    @Column
    Long bLong;

    @Column
    Double bDouble;

    @Column
    Integer bInt;

    @Column
    Boolean bBoolean;

    @Column
    Float bFloat;

    @Column
    Byte[] bByteArray;

    @Column
    Byte bByte;

}
