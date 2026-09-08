package pl.bemessenger.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        setContentView(R.layout.activity_main)
        root = findViewById(R.id.root)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val login = findViewById<EditText>(R.id.login).text.toString().trim()
            if (login.isEmpty()) {
                Toast.makeText(this, "Podaj użytkownika", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showConversations(login)
        }
    }

    private fun showConversations(user: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.rgb(247, 247, 250))
        }

        val header = TextView(this).apply {
            text = "Rozmowy"
            textSize = 28f
            setTextColor(Color.BLACK)
            setPadding(0, 20, 0, 4)
        }
        root.addView(header)

        val account = TextView(this).apply {
            text = "Zalogowano jako: $user"
            textSize = 14f
            setPadding(0, 0, 0, 24)
        }
        root.addView(account)

        val conversation = Button(this).apply {
            text = "Emilka\nPrywatna rozmowa 1:1"
            textSize = 16f
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setPadding(24, 0, 24, 0)
            setOnClickListener { showChat(user, "Emilka") }
        }
        root.addView(
            conversation,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                80
            )
        )

        val spacer = Space(this)
        root.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))

        val logout = Button(this).apply {
            text = "Wyloguj"
            setOnClickListener { showLogin() }
        }
        root.addView(logout)

        setContentView(root)
    }

    private fun showChat(user: String, contact: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 12)
            setBackgroundColor(Color.rgb(247, 247, 250))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = Button(this).apply {
            text = "‹"
            textSize = 28f
            setOnClickListener { showConversations(user) }
        }
        top.addView(back, LinearLayout.LayoutParams(60, 60))

        val name = TextView(this).apply {
            text = contact
            textSize = 21f
            setTextColor(Color.BLACK)
            setPadding(8, 0, 0, 0)
        }
        top.addView(name, LinearLayout.LayoutParams(0, 60, 1f))
        root.addView(top)

        val messages = TextView(this).apply {
            text = "Emilka: Cześć!\n\n$user: Hej, to jest test naszego komunikatora.\n\nEmilka: Super, działa!"
            textSize = 17f
            setPadding(12, 28, 12, 12)
        }
        root.addView(messages, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val input = EditText(this).apply {
            hint = "Napisz wiadomość…"
            setSingleLine(true)
        }
        composer.addView(input, LinearLayout.LayoutParams(0, 60, 1f))

        val send = Button(this).apply {
            text = "Wyślij"
            setOnClickListener {
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    messages.text = messages.text.toString() + "\n\n$user: $text"
                    input.text.clear()
                }
            }
        }
        composer.addView(send, LinearLayout.LayoutParams(110, 60))
        root.addView(composer)

        setContentView(root)
    }
}
