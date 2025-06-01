package com.example.nautilusapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.nautilusapp.Common.connected
import com.example.nautilusapp.Common.me

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dbHelper = DatabaseHelper(view.context)
        var db = dbHelper.readableDatabase

        var cursor = db.rawQuery("Select firstName, lastName, university From Simplified_User Where mailAdress='$me'",null,null)
        if(cursor.moveToNext()){
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL2))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL3))
            val university = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Simplified_User.COLUMN_NAME_COL4))
            cursor.close()

            view.findViewById<TextView>(R.id.text_email).text = me
            view.findViewById<TextView>(R.id.text_first_name).text = firstName
            view.findViewById<TextView>(R.id.text_last_name).text = lastName
            view.findViewById<TextView>(R.id.text_university).text = university

            val disconnect = view.findViewById<TextView>(R.id.button_disconnect)
            disconnect.setOnClickListener(View.OnClickListener{
                me = ""
                connected = false
                val intent = Intent(view.context, HomeActivity::class.java)
                startActivity(intent)
            })
        }
        else{
            cursor.close()
            Log.d("Filling profil","the User doesn't exist in local bd")
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}