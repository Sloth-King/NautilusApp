package com.example.nautilusapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.nautilusapp.Common.connected
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.port
import com.example.nautilusapp.Common.servorIpAdress
import com.example.nautilusapp.DatabaseContract.Simplified_User
import com.example.nautilusapp.DatabaseContract.User
import java.io.OutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

class LogIn : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button = findViewById<Button>(R.id.loginButton)
        button.setOnClickListener(View.OnClickListener {
            val logIn = findViewById<EditText>(R.id.emailInput)
            val password = findViewById<EditText>(R.id.passwordInput)

            val dbHelper = DatabaseHelper(this)
            var db = dbHelper.readableDatabase
            // Define a projection that specifies which columns from the database we will use after the query
            val projection = arrayOf(User.COLUMN_NAME_COL1)

            // Filter results WHERE "title" = 'My Title'
            val selection = "${User.COLUMN_NAME_COL1} = ? AND ${User.COLUMN_NAME_COL2} = ?"
            val selectionArgs = arrayOf(logIn.text.toString(),password.text.toString())
            val sortOrder = "${User.COLUMN_NAME_COL2} Desc"

            val cursor = db.query(
                User.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
            )
            if (cursor.moveToNext()) {
                //OUiiiii c'est le bon id et password
                cursor.close()

                //Update the value in Common
                me = logIn.text.toString()
                connected = true
                val startServiceIntent: Intent = Intent(this, ComServer::class.java)
                startService(startServiceIntent)

                val bundle = Bundle()
                bundle.putString("id", logIn.text.toString())
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                cursor.close()
                //send encrypted information to the server
                //TODO crypting the password
                thread {
                    val socket = Socket(servorIpAdress, port)
                    val writer: OutputStream = socket.getOutputStream()

                    var msg = (logIn.text.toString()).toByteArray(US_ASCII)
                    var size = msg.size.toString()
                    Log.d("socket1", size)
                    var padding = padding(size, 3 - size.length)
                    Log.d("socket1", padding.toString())
                    var finalSize: ByteArray = padding.toByteArray((US_ASCII))
                    writer.write(finalSize)
                    writer.write(msg)

                    writer.write(("5").toByteArray(US_ASCII))

                    msg = (password.text.toString()).toByteArray(US_ASCII)
                    writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray(US_ASCII))
                    writer.write(msg)

                    //Wait for the answer of the server (it has to be an int of one digit)
                    val reader = socket.getInputStream()
                    val answer= ByteArray(1)
                    reader.read(answer,0,1)
                    if(answer.toString(US_ASCII).toInt() > 1){
                        //if server sends 2 -> wrong password
                        /*val builder = AlertDialog.Builder(this)
                        builder.setMessage("Bro WTF !? You don't even have an account")
                        builder.create()
                        builder.show()*/
                        socket.close()
                    }
                    if(answer.toString(US_ASCII).toInt() ==1){
                        //if server sends 1 -> account doesn't exist
                        Log.d("LogIn","Account doesn't exist")
                        /*val builder = AlertDialog.Builder(this)
                        builder.show()
                        builder.setMessage("Wrong password")
                        builder.create()*/
                        socket.close()
                    }
                    if(answer.toString(US_ASCII).toInt() == 0){
                        //if server sends 0 -> then your in and it sends all you can ask for all the informations
                        //TODO get all the observations from the user and store them in the db
                        //Update the value in Common
                        me = logIn.text.toString()
                        connected = true
                        val startServiceIntent: Intent = Intent(this, ComServer::class.java)
                        startService(startServiceIntent)

                        //ask for complete info about the account
                        writer.write(("4").toByteArray(US_ASCII))
                        var msg = (me).toByteArray(US_ASCII)
                        size = msg.size.toString()
                        var padding = padding(size, 8 - size.length)
                        var finalSize: ByteArray = padding.toByteArray((US_ASCII))
                        writer.write(finalSize)
                        writer.write(msg)


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

                        socket.close()
                        db = dbHelper.writableDatabase

                        var values = ContentValues().apply {
                            put(Simplified_User.COLUMN_NAME_COL1, me)
                            put(Simplified_User.COLUMN_NAME_COL2, firstName.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL3,lastName.toString(US_ASCII))
                            put(Simplified_User.COLUMN_NAME_COL4,university.toString(US_ASCII))
                        }

                        db.insert(Simplified_User.TABLE_NAME, null, values)

                        val bundle = Bundle()
                        bundle.putString("id", logIn.text.toString())
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }


                }
            }


        })
    }
}