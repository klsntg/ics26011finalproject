package com.example.ics26011finalproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class SignInActivity : AppCompatActivity() {

    private lateinit var dbHandler: DatabaseHandler
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        dbHandler = DatabaseHandler(this)
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)


        val btnLogIn = findViewById<Button>(R.id.btnLogIn)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogIn.setOnClickListener {

            val usernameEditText = findViewById<EditText>(R.id.username)
            val passwordEditText = findViewById<EditText>(R.id.password)

            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {

                val success = dbHandler.loginUser(username, password)

                if (success) {
                    val user = dbHandler.getUserInfo(username)

                    if (user != null) {
                        saveUserEmailToSharedPreferences(user.email)

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()

                        // Move the intent creation and start here
                        val i = Intent(this, NavbarActivity::class.java)
                        i.putExtra("USERNAME", user.username)
                        i.putExtra("FIRST_NAME", user.firstName)
                        i.putExtra("LAST_NAME", user.lastName)
                        i.putExtra("EMAIL", user.email)
                        startActivity(i)

                        usernameEditText.text.clear()
                        passwordEditText.text.clear()

                        finish()
                    } else {
                        Toast.makeText(this, "User information not found", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login Unsuccessful. Check your credentials.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Username or Password cannot be blank", Toast.LENGTH_LONG)
                    .show()
            }
        }

        tvRegister.setOnClickListener {

            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)

        }
    }
    private fun saveUserEmailToSharedPreferences(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("email_address", email)
        editor.apply()
    }
}
