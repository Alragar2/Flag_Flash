package alragar2.isi3.uv.flagflash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

class AuthenticationLoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var register: Button
    private lateinit var login: Button
    private lateinit var userScoreManager: UserScoreManager
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication_login)

        register = findViewById(R.id.register)
        login = findViewById(R.id.login)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        // Initialize Firestore
        userScoreManager = UserScoreManager()
        userPreferences = UserPreferences(this)

        //Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integrating Firebase with FlagFlash")
        analytics.logEvent("InitScreen", bundle)

        //Analytics Event
        setup()
    }

    private fun setup() {

        title = "Authentication"

        register.setOnClickListener {
            val intent = Intent(this, AuthenticationRegisterActivity::class.java)
            startActivity(intent)
        }

        login.setOnClickListener {
            if(email.text.isNotEmpty() && password.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener {
                    if(it.isSuccessful) {
                        userPreferences.getScore { score ->
                            userPreferences.setInitialScore(score)
                            showDataLogin()
                        }
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Error al iniciar sesión, compruebe los datos ingresados")
        builder.setPositiveButton("Accept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showDataLogin() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Success")
        builder.setMessage("Inicio de sesión correcto")
        builder.setPositiveButton("Accept") { _, _ ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}