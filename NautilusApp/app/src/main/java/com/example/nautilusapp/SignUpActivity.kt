package com.example.nautilusapp

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.nautilusapp.Common.connected
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.port
import com.example.nautilusapp.Common.servorIpAdress
import com.example.nautilusapp.DatabaseContract.Simplified_User
import com.example.nautilusapp.DatabaseContract.User
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.Scanner
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

class sendDataToServor(val socket: Socket,val writer: OutputStream,val firstName: String,val lastName: String,val password: String,val university: String) : Runnable{

    fun padding(msg: String,n: Int): String {
        if(n > 0){
            var res = "0"+msg
            return padding(res,n-1)
        }
        else{
            return msg
        }
    }

    override fun run(){
        var msg = (firstName).toByteArray(US_ASCII)
        var padding = padding(msg.size.toString(),8-msg.size.toString().length)
        Log.d("socket",padding)
        writer.write(padding.toByteArray((US_ASCII)))
        writer.write(msg)

        msg = (lastName).toByteArray(US_ASCII)
        writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray((US_ASCII)))
        writer.write(msg)

        //TODO crypting the password
        msg = (password).toByteArray(US_ASCII)
        writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray((US_ASCII)))
        writer.write(msg)

        msg = (university).toByteArray(US_ASCII)
        writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray((US_ASCII)))
        writer.write(msg)

        socket.close()
    }
}

class SignUpActivity : AppCompatActivity() {

    fun padding(msg: String,n: Int): String {
        if(n > 0){
            var res = "0"+msg
            return padding(res,n-1)
        }
        else{
            return msg
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        val dbHelper = DatabaseHelper(this)
        var db = dbHelper.readableDatabase

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = findViewById<Button>(R.id.submitButton)
        button.setOnClickListener(View.OnClickListener {
            val firstName = findViewById<EditText>(R.id.firstNameInput)
            val lastName = findViewById<EditText>(R.id.lastNameInput)
            val mailAdress = findViewById<EditText>(R.id.emailInput)//TODO email size has to be between 1 and 320
            val password = findViewById<EditText>(R.id.passwordInput)
            val university = findViewById<EditText>(R.id.universityInput)

            //before inserting the data we need to check if the id doesn't already exist

            // Define a projection that specifies which columns from the database we will use after the query
            val projection = arrayOf(Simplified_User.COLUMN_NAME_COL1)

            // Filter results WHERE "title" = 'My Title'
            val selection = "${Simplified_User.COLUMN_NAME_COL1} = ?"
            val selectionArgs = arrayOf(mailAdress.text.toString())

            val cursor = db.query(
                Simplified_User.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
            )

            if(cursor.moveToNext()){
                cursor.close()
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Ratéééééééé")
                builder.create()
                builder.show()

            }
            else{
                cursor.close()
                //if they don't exist already then we need to check with the server
                thread{
                    val socket = Socket(servorIpAdress,port)
                    val writer: OutputStream = socket.getOutputStream()
                    Log.d("My tag","I am here")
                    writer.write(padding("0",3-("0").length).toByteArray(US_ASCII))

                    var msg = (mailAdress.text.toString()).toByteArray(US_ASCII)
                    val size = msg.size.toString()
                    Log.d("socket1",size)
                    var padding = padding(size,8-size.length)
                    Log.d("socket1",padding.toString())
                    var finalSize: ByteArray = padding.toByteArray((US_ASCII))

                    writer.write(finalSize)
                    writer.write(msg)

                    val reader = socket.getInputStream()
                    val isAlreadyUsed= ByteArray(1)
                    reader.read(isAlreadyUsed,0,1)

                    if(isAlreadyUsed.toString(US_ASCII).toInt() == 1){
                        //if there is no account with that email in the server then we can create the account
                        Log.d("Creation Account","The account will be created")


                        db = dbHelper.writableDatabase

                        var values = ContentValues().apply {
                            put(Simplified_User.COLUMN_NAME_COL1, mailAdress.text.toString())
                            put(Simplified_User.COLUMN_NAME_COL2, firstName.text.toString())
                            put(Simplified_User.COLUMN_NAME_COL3,lastName.text.toString())
                            put(Simplified_User.COLUMN_NAME_COL4,university.text.toString())
                        }

                        db.insert(Simplified_User.TABLE_NAME, null, values)

                        values = ContentValues().apply {
                            put(User.COLUMN_NAME_COL1,mailAdress.text.toString())
                            put(User.COLUMN_NAME_COL2,password.text.toString())
                        }
                        db.insert(User.TABLE_NAME, null, values)

                        //Then we send the data to the server
                        val runnable = sendDataToServor(socket,writer,firstName.text.toString(),lastName.text.toString(),password.text.toString(),university.text.toString())
                        var thread = Thread(runnable)
                        thread.start()

                        //Update the value in Common
                        me = mailAdress.text.toString()
                        connected = true
                        val startServiceIntent: Intent = Intent(this, ComServer::class.java)
                        startService(startServiceIntent)

                        //We can finaly go to the next page
                        val bundle = Bundle()
                        bundle.putString("id", mailAdress.text.toString())
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    else{
                        socket.close()
                        /*val builder = AlertDialog.Builder(this)
                        builder.setMessage("Il y a déjà un compte avec cette adresse")
                        builder.create()
                        builder.show()*/
                    }

                }
            }

        })
    }
}