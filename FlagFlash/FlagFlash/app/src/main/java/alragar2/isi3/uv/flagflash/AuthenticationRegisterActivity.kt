package alragar2.isi3.uv.flagflash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class AuthenticationRegisterActivity: AppCompatActivity(){

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var name: EditText
    private lateinit var register: Button
    private lateinit var userScoreManager: UserScoreManager
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication_register)

        register = findViewById(R.id.register)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        name = findViewById(R.id.name)

        // Initialize Firestore
        userScoreManager = UserScoreManager()
        userPreferences = UserPreferences(this)

        //Analytics Event
        setup()
    }

    private fun setup(){
        title = "Register"

        register.setOnClickListener{
            if(email.text.isNotEmpty() && password.text.isNotEmpty() && confirmPassword.text.isNotEmpty() && name.text.isNotEmpty()){
                if(password.text.toString() == confirmPassword.text.toString()){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if(userId != null){
                                userScoreManager.saveUserName(userId, name.text.toString(), {
                                    userPreferences.setScore(0)
                                    userPreferences.setUserName(name.text.toString())
                                    showDataRegister()
                                }, {
                                    showAlert()
                                })
                            }
                        } else {
                            showAlert()
                        }
                    }
                } else {
                    showPasswordMismatchAlert()
                }
            } else {
                showEmptyAlert()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Error al registrarse. Por favor, inténtelo de nuevo.")
        builder.setPositiveButton("Accept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showPasswordMismatchAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("La contraseña no coincide con la confirmación de la contraseña. Por favor, inténtelo de nuevo.")
        builder.setPositiveButton("Accept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showDataRegister() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Success")
        builder.setMessage("Registro correcto")
        builder.setPositiveButton("Accept") { _, _ ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showEmptyAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Por favor, rellene todos los campos")
        builder.setPositiveButton("Accept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}