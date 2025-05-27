package com.example.nautilusapp

import android.content.ContentValues
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

class SignUpActivity : AppCompatActivity() {
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
            val mailAdress = findViewById<EditText>(R.id.emailInput)
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
                //TODO check for existing account bond to this email in server

                //if there is no account with that email in the server then we can create the account

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

                //We can finaly go to the next page
                val bundle = Bundle()
                bundle.putString("id", mailAdress.text.toString())
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

        })
    }
}