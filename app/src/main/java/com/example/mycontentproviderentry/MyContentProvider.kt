package com.example.mycontentproviderentry

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import java.lang.IllegalArgumentException

class MyContentProvider : ContentProvider() {
    companion object{
        const val AUTHORITY:String = "com.example.broadcastreceiverdemo.MyContentProvider"
        val URI = Uri.parse("content://$AUTHORITY/Students")
        val ID = "id"
        val NAME = "studentName"
        val AGE = "studentAge"
        private const val STUDENTS = 1
        private const val STUDENT_ID = 1

        private lateinit var STUDENT_PROJECTION_MAP:HashMap<String,String>

        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        init {
            MATCHER.addURI(
                AUTHORITY,
                "Students",
                STUDENTS
            )
            MATCHER.addURI(
                AUTHORITY,
                "Students/#",
                STUDENT_ID
            )
        }

    }
    private lateinit var db: SQLiteDatabase
    private val DATABASE_NAME = "Colleges"
    private val TABLE_NAME = "Students"
    private val DATABASE_VERSION = 1
    private val CREATE_DB = " CREATE TABLE " + TABLE_NAME +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " $NAME TEXT NOT NULL, " +
            " $AGE TEXT NOT NULL);"

    inner class DBHelper(mContext: Context): SQLiteOpenHelper(mContext,DATABASE_NAME,null,DATABASE_VERSION){

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(CREATE_DB)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        var rowId = 0L
        try{
            rowId = db.insert(TABLE_NAME, "", values)
            Log.i("MyException",rowId.toString())
        }   catch (ex:Exception){
            Log.i("MyException",ex.message.toString())
        }
        if(rowId>0){
            val newUri = ContentUris.withAppendedId(URI,rowId)
            context!!.contentResolver.notifyChange(newUri,null)
            return newUri
        }
        throw SQLException("Failed to add a record into " + uri);
    }

    override fun onCreate(): Boolean {
        val dbHelper = context?.let { DBHelper(it) }
        if (dbHelper != null) {
            db = dbHelper.writableDatabase
        }else{
            return false
        }
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val qb = SQLiteQueryBuilder()
        qb.tables = TABLE_NAME
        when(MATCHER.match(uri)){
            STUDENTS -> {
                qb.projectionMap = STUDENT_PROJECTION_MAP
            }
            STUDENT_ID -> {
                qb.appendWhere( "$STUDENT_ID =" + uri.getPathSegments().get(1));
            }
        }
        lateinit var cursor:Cursor
        if (sortOrder.isNullOrEmpty()){
            cursor = qb.query(db,projection,selection,selectionArgs,null,null, NAME)
        }else{
            cursor = qb.query(db,projection,selection,selectionArgs,null,null, sortOrder)
        }
        cursor.setNotificationUri(context!!.contentResolver,uri)
        return cursor
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count:Int = 0
        when(MATCHER.match(uri)){
            STUDENTS -> {
                count = db.delete(TABLE_NAME,selection,selectionArgs)
            }
            STUDENT_ID -> {
                val studentId=uri.pathSegments.get(1)
                count = db.delete(TABLE_NAME,"$ID = $studentId "+(selection.isNullOrEmpty()),selectionArgs)
            }
            else ->{
                throw IllegalArgumentException("Unknown student Id ");

            }
        }
        context!!.contentResolver.notifyChange(uri,null)
        return count
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var count:Int = 0
        when(MATCHER.match(uri)){
            STUDENTS -> {
                count = db.update(TABLE_NAME,values,selection,selectionArgs)
            }
            STUDENT_ID -> {
                val studentId=uri.pathSegments.get(1)
                count = db.update(TABLE_NAME,values,"$ID = $studentId "+(selection.isNullOrEmpty()),selectionArgs)
            }
            else ->{
                throw IllegalArgumentException("Unknown student Id ");

            }
        }
        context!!.contentResolver.notifyChange(uri,null)
        return count
    }

    override fun getType(uri: Uri): String? {
        when(MATCHER.match(uri)){
            STUDENTS -> {
                return "vnd.android.cursor.dir/vnd.example.broadcastreceiverdemo"
            }
            STUDENT_ID -> {
                return "vnd.android.cursor.item/vnd.example.broadcastreceiverdemo"
            }
            else ->{
                throw IllegalArgumentException("Unknown URI ");

            }
        }
    }
}