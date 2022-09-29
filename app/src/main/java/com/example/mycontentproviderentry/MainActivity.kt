package com.example.mycontentproviderentry

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn: Button = findViewById(R.id.btnSave)
        val txtStudentName: EditText = findViewById(R.id.studentName)
        val txtStudentAge: EditText = findViewById(R.id.studentAge)

        btn.setOnClickListener {
            if (txtStudentName.text.isNullOrEmpty()){
                txtStudentName.setError("Enter Student Name")
            }else if(txtStudentAge.text.isNullOrEmpty()){
                txtStudentAge.setError("Enter Student Age")
            }else{
                val contentValue: ContentValues = ContentValues()
                contentValue.put(MyContentProvider.NAME,txtStudentName.text.toString())
                contentValue.put(MyContentProvider.AGE,txtStudentAge.text.toString())
                val uri = contentResolver.insert(MyContentProvider.URI,contentValue)
                Toast.makeText(getBaseContext(),
                    uri.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}