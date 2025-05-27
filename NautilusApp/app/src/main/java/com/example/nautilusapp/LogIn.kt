package com.example.nautilusapp

import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.nautilusapp.DatabaseContract.Simplified_User
import com.example.nautilusapp.DatabaseContract.User

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
                val bundle = Bundle()
                bundle.putString("id", logIn.text.toString())
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                cursor.close()
                //send crypted information to the server
                //if server sends 2 -> account doesn't exist
                //if server sends 1 -> wrong password
                //if server sends 0 -> then your in and it sends all you can ask for all the informations
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Ratéééééééé")
                builder.create()
                builder.show()
            }


        })
    }
}