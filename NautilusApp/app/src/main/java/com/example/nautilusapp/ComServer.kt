package com.example.nautilusapp

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.os.Handler
import java.net.Socket
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.provider.BaseColumns
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.nautilusapp.Common.connected
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.port
import com.example.nautilusapp.Common.servorIpAdress
import com.example.nautilusapp.DatabaseContract.Friend
import com.example.nautilusapp.DatabaseContract.Simplified_User
import com.example.nautilusapp.DatabaseContract.User
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Scanner
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

class MySevorSocket(val address: String,val port: Int){

    fun run(){
        val socket = Socket(address,port)
        val writer: OutputStream = socket.getOutputStream()
        var msg = ("lucien.bouby@gmail.com").toByteArray(Charset.defaultCharset())
        val size = msg.size.toString()
        var finalSize: ByteArray
        if (size.isEmpty()) finalSize = ("000").toByteArray(US_ASCII)
        else{
            if (size.length == 1) finalSize = ("00$size").toByteArray(US_ASCII)
            else{
                if (size.length == 2) finalSize = ("0$size").toByteArray(US_ASCII)
                else{
                    if (size.length == 3) finalSize = (size).toByteArray(US_ASCII)
                    else{
                        //the size of the mmailing address is to big to be a true mailing address
                        Log.d("email size problem","the email size is bigger than 320 char it is not normal")
                        //TODO take care of this situation
                        finalSize = ("000").toByteArray(US_ASCII)
                    }
                }
            }
        }
        writer.write(finalSize)
        writer.write(msg)

        socket.close()
    }
    
}

class PingServor : Runnable{

    override fun run() {
        //We send messages to the server until the phone is turned off
        val ptnJenAiMarre = MySevorSocket(servorIpAdress,port)
        while (connected){
            ptnJenAiMarre.run()
            sleep(3600)
        }

    }
}

class ComServer : Service(){

    //TODO make this run every time someone connect to his account and stop every time they disconnect
    //Pour les messages non envoyés, on peut mettre un cache de 1j dans lequel ils sont stockés en attendant que l'autre type se connecte a sont compte

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        /* Start up the thread running the service.  Note that we create a separate thread because the service normally runs in the process's  main thread, which we don't want
        to block.  We also make it background priority so CPU-intensive work will not disrupt our UI.*/
        var runnable = PingServor()
        var thread = Thread(runnable)
        thread.start()

        thread{
            val server = ServerSocket(1895)
            while (true){
                val client = server.accept()
                thread{
                    Log.d("socketIp",client.inetAddress.toString())
                    //Here we handle every communications between two accounts
                    val reader =  client.getInputStream()
                    val msg= ByteArray(1)
                    reader.read(msg,0,1)
                    //1 is friend request is received
                    //2 someone answered your friend request
                    if(msg.toString(US_ASCII).toInt() == 1){
                        val dbHelper = DatabaseHelper(this)
                        val db = dbHelper.writableDatabase

                        //TODO handle the friend request and put it somewhere (for exemple in the list) wait for andrew to do his shit
                        val sizeAddr = ByteArray(8)
                        reader.read(sizeAddr,0,8)
                        val mail = ByteArray(sizeAddr.toString(US_ASCII).toInt())
                        reader.read(mail,0,sizeAddr.toString(US_ASCII).toInt())
                        Log.d("addr",mail.toString(US_ASCII))

                        //fill the BD for a new discussion


                        var size = ByteArray(8)
                        reader.read(size,0,8)
                        val firstName = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(firstName,0,size.toString(US_ASCII).toInt())
                        size = ByteArray(8)
                        reader.read(size,0,8)
                        val lastName = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(lastName,0,size.toString(US_ASCII).toInt())
                        size = ByteArray(8)
                        reader.read(size,0,8)
                        val university = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(university,0,size.toString(US_ASCII).toInt())

                        var values = ContentValues().apply {
                            put(Simplified_User.COLUMN_NAME_COL1, mail.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL2, firstName.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL3,lastName.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL4,university.toString(US_ASCII))
                        }

                        try {
                            db.insert(Simplified_User.TABLE_NAME, null, values)
                        }
                        catch (e : SQLiteConstraintException){
                            Log.d("BD error","the user already is in the database")
                        }

                        values = ContentValues().apply {
                            put(Friend.ID1,me)
                            put(Friend.ID2,mail.toString(US_ASCII))
                        }
                        db.insert(Friend.TABLE_NAME, null, values)

                        //TODO ajouter le fragment de message

                        var textMessage = firstName.toString(US_ASCII)+" is sending you a friend request"
                        Log.d("Message",textMessage)
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        var current = LocalDate.now().format(formatter)
                        Log.d("did it worked ??",current.toString())

                        //new we try to get the hour
                        val zoneId = systemDefault()
                        val currentZonedDateTime = ZonedDateTime.now(zoneId)
                        val currentTimestamp: Long = currentZonedDateTime.toInstant().toEpochMilli()
                        Log.d("Time ?",currentTimestamp.toString())
                        val hour = currentTimestamp.mod(3600*1000*24)/(3600*1000)+2
                        val min = (currentTimestamp.mod(3600*1000))/(60*1000)
                        val sec = currentTimestamp.mod(60*1000)/1000
                        Log.d("Time ?", "$hour:$min:$sec")
                        values = ContentValues().apply {
                            put(DatabaseContract.Discussion.COLUMN_NAME_COL1,firstName.toString(US_ASCII))
                            put(DatabaseContract.Discussion.COLUMN_NAME_COL2,-1)
                        }

                        db.insert(DatabaseContract.Discussion.TABLE_NAME, null, values)

                        val dbRead = dbHelper.readableDatabase
                        var cursor = dbRead.rawQuery("Select Max(_id) From Discussion Where name='"+firstName.toString(US_ASCII)+"';",null,null)

                        cursor.moveToNext()
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow("Max(_id)"))
                        cursor.close()

                        values = ContentValues().apply {
                            put(DatabaseContract.Message.COLUMN_NAME_COL1, textMessage)
                            put(DatabaseContract.Message.COLUMN_NAME_COL2, current.toString())
                            put(DatabaseContract.Message.COLUMN_NAME_COL3, "$hour:$min:$sec")
                            put(DatabaseContract.Message.COLUMN_NAME_COL5, id)
                        }

                        db.insert(DatabaseContract.Message.TABLE_NAME, null, values)

                        values = ContentValues().apply{
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL1, me)
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL2, id)
                        }

                        db.insert(DatabaseContract.Talk_in.TABLE_NAME, null, values)

                        values = ContentValues().apply{
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL1, mail.toString(US_ASCII))
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL2, id)
                        }

                        try{
                            db.insert(DatabaseContract.Talk_in.TABLE_NAME, null, values)
                        }
                        catch (e : SQLiteConstraintException){
                            Log.d("BD error","Can't create discussion with myself")
                        }



                    }
                    if(msg.toString(US_ASCII).toInt() == 2){

                        val dbHelper = DatabaseHelper(this)
                        val db = dbHelper.writableDatabase

                        var size = ByteArray(3)
                        reader.read(size,0,3)
                        Log.d("ComSocket",size.toString(US_ASCII))
                        val mailAdress = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(mailAdress,0,size.toString(US_ASCII).toInt())
                        size = ByteArray(3)
                        reader.read(size,0,3)
                        val firstName = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(firstName,0,size.toString(US_ASCII).toInt())
                        size = ByteArray(3)
                        reader.read(size,0,3)
                        val lastName = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(lastName,0,size.toString(US_ASCII).toInt())
                        size = ByteArray(3)
                        reader.read(size,0,3)
                        val university = ByteArray(size.toString(US_ASCII).toInt())
                        reader.read(university,0,size.toString(US_ASCII).toInt())

                        var values = ContentValues().apply {
                            put(Simplified_User.COLUMN_NAME_COL1, mailAdress.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL2, firstName.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL3,lastName.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL4,university.toString(US_ASCII))
                        }

                        db.insert(Simplified_User.TABLE_NAME, null, values)

                        values = ContentValues().apply {
                            put(Friend.ID1,me)
                            put(Friend.ID2,mailAdress.toString(US_ASCII))
                        }
                        db.insert(Friend.TABLE_NAME, null, values)

                        //TODO ajouter le fragment de message

                        var textMessage = firstName.toString(US_ASCII)+" added you as a friend ! <3"
                        Log.d("Message",textMessage)
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        var current = LocalDate.now().format(formatter)
                        Log.d("did it worked ??",current.toString())

                        //new we try to get the hour
                        val zoneId = systemDefault()
                        val currentZonedDateTime = ZonedDateTime.now(zoneId)
                        val currentTimestamp: Long = currentZonedDateTime.toInstant().toEpochMilli()
                        Log.d("Time ?",currentTimestamp.toString())
                        val hour = currentTimestamp.mod(3600*1000*24)/(3600*1000)+2
                        val min = (currentTimestamp.mod(3600*1000))/(60*1000)
                        val sec = currentTimestamp.mod(60*1000)/1000
                        Log.d("Time ?", "$hour:$min:$sec")
                        values = ContentValues().apply {
                            put(DatabaseContract.Discussion.COLUMN_NAME_COL1,firstName.toString(US_ASCII))
                            put(DatabaseContract.Discussion.COLUMN_NAME_COL2,-1)
                        }

                        db.insert(DatabaseContract.Discussion.TABLE_NAME, null, values)

                        val dbRead = dbHelper.readableDatabase
                        var cursor = dbRead.rawQuery("Select Max(_id) From Discussion Where name='"+firstName.toString(US_ASCII)+"';",null,null)

                        cursor.moveToNext()
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow("Max(_id)"))
                        cursor.close()

                        values = ContentValues().apply {
                            put(DatabaseContract.Message.COLUMN_NAME_COL1, textMessage)
                            put(DatabaseContract.Message.COLUMN_NAME_COL2, current.toString())
                            put(DatabaseContract.Message.COLUMN_NAME_COL3, "$hour:$min:$sec")
                            put(DatabaseContract.Message.COLUMN_NAME_COL5, id)
                        }

                        db.insert(DatabaseContract.Message.TABLE_NAME, null, values)

                        values = ContentValues().apply{
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL1, me)
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL2, id)
                        }

                        db.insert(DatabaseContract.Talk_in.TABLE_NAME, null, values)

                        values = ContentValues().apply{
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL1, mailAdress.toString(US_ASCII))
                            put(DatabaseContract.Talk_in.COLUMN_NAME_COL2, id)
                        }

                        db.insert(DatabaseContract.Talk_in.TABLE_NAME, null, values)

                    }
                }
            }
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }
}