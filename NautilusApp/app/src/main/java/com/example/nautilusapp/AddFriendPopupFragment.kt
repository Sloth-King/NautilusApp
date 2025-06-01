package com.example.nautilusapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.requestIdUsers
import org.w3c.dom.Text
import java.io.OutputStream
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.text.Charsets.US_ASCII

class AddFriendPopupFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_friend_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val closeButton = view.findViewById<ImageView>(R.id.button_close_popup)
        val emailEditText = view.findViewById<EditText>(R.id.edittext_friend_email)
        val addButton = view.findViewById<Button>(R.id.button_add_friend)

        closeButton.setOnClickListener {
            dismiss()
        }

        addButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                thread{
                    Log.d("Address requested",emailEditText.text.toString())
                    val addr = requestIdUsers(emailEditText.text.toString())

                    if(addr != ""){
                        var client = Socket(addr,1895)
                        val writer: OutputStream = client.getOutputStream()
                        writer.write(("1").toByteArray(US_ASCII))

                        var msg = (me).toByteArray(US_ASCII)
                        var size = msg.size.toString()
                        var padding = padding(size, 8 - size.length)
                        var finalSize: ByteArray = padding.toByteArray((US_ASCII))
                        writer.write(finalSize)
                        writer.write(msg)

                        val dbHelper = DatabaseHelper(requireContext())
                        var db = dbHelper.readableDatabase

                        var cursor = db.rawQuery("Select firstName, LastName, university From Simplified_User Where mailAdress='$me';",null,null)
                        cursor.moveToNext()
                        val firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2))
                        val lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL3))
                        val university = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL4))
                        cursor.close()

                        msg = (firstName).toByteArray(US_ASCII)
                        writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray((US_ASCII)))
                        writer.write(msg)

                        msg = (lastName).toByteArray(US_ASCII)
                        writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray((US_ASCII)))
                        writer.write(msg)


                        msg = (university).toByteArray(US_ASCII)
                        writer.write(padding(msg.size.toString(),8-msg.size.toString().length).toByteArray((US_ASCII)))
                        writer.write(msg)

                        client.close()
                        dismiss()
                    }
            }}
            else {
                Toast.makeText(requireContext(), "Please enter an email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
