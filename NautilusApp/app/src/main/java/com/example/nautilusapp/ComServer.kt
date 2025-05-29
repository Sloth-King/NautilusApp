package com.example.nautilusapp

import android.app.Service
import android.content.Intent
import android.os.Handler
import java.net.Socket
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.Scanner
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

class MySevorSocket(address: String,port: Int){
    private val socket = Socket(address,port)
    private val writer: OutputStream = socket.getOutputStream()

    fun run(){
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

    }
    
}

class PingServor : Runnable{
    val servorIpAdress = "192.168.43.135"
    val port = 5052

    override fun run() {
        //We send messages to the server until the phone is turned off
        val ptnJenAiMarre = MySevorSocket(servorIpAdress,port)
        while (true){
            ptnJenAiMarre.run()
            sleep(3600)
        }

    }
}

class ComServer : Service(){

    //TODO make this run every time someone connect to his account and stop every time they disconnect
    //Pour les messages non envoyés, on peut mettre un cache de 1j dans lequel ils sont stockés en attendant que l'autre type se connecte a sont compte

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
                    //Here we handle every communications between two accounts
                    val reader =  client.getInputStream()
                    val msg= ByteArray(1)
                    reader.read(msg,0,1)
                    //1 is friend request is received
                    //2 someone answered your friend request
                    if(msg.toString(US_ASCII).toInt() == 1){
                        //TODO handle the friend request and put it somewhere (for exemple in the list)
                    }
                    if(msg.toString(US_ASCII).toInt() == 2){
                        //TODO you change the last message to ... as added you as close friend
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