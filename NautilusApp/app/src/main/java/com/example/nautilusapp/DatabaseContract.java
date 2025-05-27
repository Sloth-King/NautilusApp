package com.example.nautilusapp;

import static android.provider.BaseColumns._ID;

import android.provider.BaseColumns;

public class DatabaseContract {
    public static final  int    DATABASE_VERSION = 1;
    public static final  String DATABASE_NAME = "database.db";
    private static final String Mail_Type = " Varchar(320)";
    private static final String NameType = " Varchar(40)";
    private static final String pictureType = " LongBlob";
    private static final String COMMA_SEP = ",";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DatabaseContract() {}

    public static abstract class Simplified_User implements BaseColumns {
        public static final String TABLE_NAME = "Simplified_User";
        public static final String COLUMN_NAME_COL1 = "mailAdress";
        public static final String COLUMN_NAME_COL2 = "firstName";
        public static final String COLUMN_NAME_COL3 = "lastName";
        public static final String COLUMN_NAME_COL4 = "university";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + Mail_Type + " Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + NameType + COMMA_SEP +
                COLUMN_NAME_COL3 + NameType + COMMA_SEP +
                COLUMN_NAME_COL4 + " Varchar(150)" + COMMA_SEP +
                "Primary Key ("+ COLUMN_NAME_COL1 +") )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "User";
        public static final String COLUMN_NAME_COL1 = "mailAdress";
        public static final String COLUMN_NAME_COL2 = "password";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + Mail_Type + " Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Varchar(60) Not Null" + COMMA_SEP +
                "Primary Key ("+ COLUMN_NAME_COL1 +")" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL1 +") References Simplified_User("+ COLUMN_NAME_COL1 +") )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Friend implements BaseColumns {
        public static final String TABLE_NAME = "Friend";
        public static final String ID1 = "Id1";
        public static final String ID2 = "Id2";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                ID1 + Mail_Type + " Not Null" + COMMA_SEP +
                ID2 + Mail_Type + " Not Null" + COMMA_SEP +
                "Primary Key ("+ ID1+","+ID2 +")" + COMMA_SEP +
                "Foreign Key ("+ ID1 +") References Simplified_User(mailAdress)" + COMMA_SEP +
                "Foreign Key ("+ ID2 +") References Simplified_User(mailAdress) )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Picture implements BaseColumns {
        public static final String TABLE_NAME = "Picture";
        public static final String COLUMN_NAME_COL1 = "picture";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + pictureType + " Not Null )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Discussion implements BaseColumns {
        public static final String TABLE_NAME = "Discussion";
        public static final String COLUMN_NAME_COL1 = "Name";
        public static final String COLUMN_NAME_COL2 = "idPicture";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + NameType + " Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Integer Not Null" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL2 +") References Picture("+_ID+") )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Talk_in implements BaseColumns {
        public static final String TABLE_NAME = "Talk_in";
        public static final String COLUMN_NAME_COL1 = "mailAdress";
        public static final String COLUMN_NAME_COL2 = "idDiscussion";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + Mail_Type + " Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Int Not Null" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL1 +") References Simplified_User("+COLUMN_NAME_COL1+")" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL2 +") References Discussion("+_ID+")" + COMMA_SEP +
                "Primary Key ("+ COLUMN_NAME_COL1+","+ COLUMN_NAME_COL2+") )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Message implements BaseColumns {
        public static final String TABLE_NAME = "Message";
        public static final String COLUMN_NAME_COL1 = "text";
        public static final String COLUMN_NAME_COL2 = "date";
        public static final String COLUMN_NAME_COL3 = "hour";
        public static final String COLUMN_NAME_COL4 = "Author";
        public static final String COLUMN_NAME_COL5 = "idDiscussion";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + " Varchar(300) Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Date" + COMMA_SEP +
                COLUMN_NAME_COL3 + " Float" + COMMA_SEP +
                COLUMN_NAME_COL4 + Mail_Type + COMMA_SEP +
                COLUMN_NAME_COL5 + " Int" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL4 +") References Simplified_User(mailAdress)" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL5 +") References Discussion("+_ID+")" + ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Species implements BaseColumns {
        public static final String TABLE_NAME = "Species";
        public static final String COLUMN_NAME_COL1 = "scientific_name";
        public static final String COLUMN_NAME_COL2 = "parent";

        public static final String COLUMN_NAME_COL3 = "environment";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + NameType + COMMA_SEP +
                COLUMN_NAME_COL2 + " Int Not Null" + COMMA_SEP +
                COLUMN_NAME_COL3 + " Varchar(150)" + ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Observation implements BaseColumns {
        public static final String TABLE_NAME = "Observation";
        public static final String COLUMN_NAME_COL1 = "geolocalisation";
        public static final String COLUMN_NAME_COL2 = "idUser";
        public static final String COLUMN_NAME_COL3 = "aphiaId";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + " Varchar(50)" + COMMA_SEP +
                COLUMN_NAME_COL2 + Mail_Type + COMMA_SEP +
                COLUMN_NAME_COL3 + " Int Not Null" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL2 +") References Simplified_User(mailAdress)" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL3 +") References Species("+_ID+")" + ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Note implements BaseColumns {
        public static final String TABLE_NAME = "Note";
        public static final String COLUMN_NAME_COL1 = "title";
        public static final String COLUMN_NAME_COL2 = "text";
        public static final String COLUMN_NAME_COL3 = "idObs";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_COL1 + " Varchar(150)" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Varchar(500)" + COMMA_SEP + /*This reduce the maximal size of a text note to 500 characters*/
                COLUMN_NAME_COL3 + " Int Not Null" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL3 +") References Observation("+_ID+")" + ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class is_in_observation implements BaseColumns {
        public static final String TABLE_NAME = "is_in_observation";
        public static final String COLUMN_NAME_COL1 = "idPicture";
        public static final String COLUMN_NAME_COL2 = "idObs";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + " Int Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Int Not Null" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL1 +") References Picture("+_ID+")" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL2 +") References Observation("+_ID+")" + COMMA_SEP +
                "Primary Key ("+ COLUMN_NAME_COL1+","+ COLUMN_NAME_COL2+") )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class is_in_species_page implements BaseColumns {
        public static final String TABLE_NAME = "is_in_species_page";
        public static final String COLUMN_NAME_COL1 = "idPicture";
        public static final String COLUMN_NAME_COL2 = "aphiaId";


        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME_COL1 + " Int Not Null" + COMMA_SEP +
                COLUMN_NAME_COL2 + " Int Not Null" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL1 +") References Picture("+_ID+")" + COMMA_SEP +
                "Foreign Key ("+ COLUMN_NAME_COL2 +") References Species("+_ID+")" + COMMA_SEP +
                "Primary Key ("+ COLUMN_NAME_COL1+","+ COLUMN_NAME_COL2+") )";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
