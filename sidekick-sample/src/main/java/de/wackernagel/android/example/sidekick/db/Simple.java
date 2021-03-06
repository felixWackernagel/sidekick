package de.wackernagel.android.example.sidekick.db;

import java.util.Date;

import de.wackernagel.android.example.sidekick.provider.ArticleProvider;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = ArticleProvider.AUTHORITY )
public class Simple {
    short aShort;
    long aLong;
    double aDouble;
    int aInt;
    String aString;
    boolean aBoolean;
    float aFloat;
    byte[] aByteArray;
    byte aByte;
    Short bShort;
    Long bLong;
    Double bDouble;
    Integer bInt;
    Boolean bBoolean;
    Float bFloat;
    Byte bByte;
    Date aDate;
    int[] aIntArray;
}
