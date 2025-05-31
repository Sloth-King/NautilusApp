package com.example.nautilusapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.nautilusapp.Common.me
import com.example.nautilusapp.Common.padding
import com.example.nautilusapp.Common.requestIdUsers
import java.io.OutputStream
import java.net.Socket
import kotlin.text.Charsets.US_ASCII

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendRequestChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendRequestChatFragment(val discussion: Int,val chatName: String) : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_request_chat, container, false)
    }
    //TODO drop the friend request message when accepting it

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val yes = view.findViewById<Button>(R.id.button_accept)
        yes.setOnClickListener(View.OnClickListener{
            //get the id of a User
            val dbHelper = DatabaseHelper(requireContext())
            var db = dbHelper.readableDatabase
            var cursor = db.rawQuery("Select mailAdress From Simplified_User As s Join Talk_in As t On s.mailAdress=t.mailAdress Where t.idDiscussion='$discussion';",null,null)
            var stop: Boolean = false
            var data: String = ""
            while (cursor.moveToNext() && !(stop) ){
                data = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL1))
                if(data != me){
                    stop = true
                }
            }
            cursor.close()

            //get the address of the User
            val addr = requestIdUsers(data)

            var client = Socket(addr,1895)
            val writer: OutputStream = client.getOutputStream()
            writer.write(("2").toByteArray(US_ASCII))

            var msg = (me).toByteArray(US_ASCII)
            var size = msg.size.toString()
            var padding = padding(size, 3 - size.length)
            var finalSize: ByteArray = padding.toByteArray((US_ASCII))
            writer.write(finalSize)
            writer.write(msg)

            cursor = db.rawQuery("Select firstName, LastName, university From Simplified_User Where mailAdress='$me';",null,null)
            cursor.moveToNext()
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL3))
            val university = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL4))
            cursor.close()

            msg = (firstName).toByteArray(US_ASCII)
            writer.write(padding(msg.size.toString(),3-msg.size.toString().length).toByteArray((US_ASCII)))
            writer.write(msg)

            msg = (lastName).toByteArray(US_ASCII)
            writer.write(padding(msg.size.toString(),3-msg.size.toString().length).toByteArray((US_ASCII)))
            writer.write(msg)


            msg = (university).toByteArray(US_ASCII)
            writer.write(padding(msg.size.toString(),3-msg.size.toString().length).toByteArray((US_ASCII)))
            writer.write(msg)

            client.close()
        })

    }

}