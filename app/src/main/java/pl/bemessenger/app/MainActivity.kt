package pl.bemessenger.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(246, 247, 251)
    private val surface = Color.WHITE
    private val inputBg = Color.rgb(238, 240, 247)
    private val primary = Color.rgb(82, 84, 224)
    private val primaryDark = Color.rgb(60, 62, 190)
    private val textPrimary = Color.rgb(25, 27, 38)
    private val textSecondary = Color.rgb(92, 98, 120)
    private val hintColor = Color.rgb(125, 130, 150)
    private val border = Color.rgb(220, 223, 234)
    private val outgoingBubble = Color.rgb(82, 84, 224)
    private val incomingBubble = Color.WHITE
    private val chipBg = Color.rgb(237, 238, 255)

    private var loggedUser = "bartek"
    private var attachedUri: Uri? = null
    private lateinit var messageList: LinearLayout
    private lateinit var input: EditText
    private lateinit var attachmentPreview: TextView
    private lateinit var emojiRow: HorizontalScrollView

    private val pickAttachment = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            attachedUri = uri
            attachmentPreview.text = "Załącznik: ${displayName(uri)}"
            attachmentPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep the application inside the safe system-window area.
        // This avoids the clipped header/status-bar problem visible on the test device.
        window.statusBarColor = bg
        window.navigationBarColor = bg
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        showLogin()
    }

    private fun showLogin() {
        val root = vertical(bg, 20)
        root.gravity = Gravity.CENTER_HORIZONTAL

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 0.16f))

        val logo = text("B&E", 42f, primary, true).apply {
            gravity = Gravity.CENTER
        }
        root.addView(logo, matchWrap())

        val title = text("B&E Messenger", 28f, textPrimary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 0)
        }
        root.addView(title, matchWrap())

        val subtitle = text("Prywatny komunikator", 17f, textSecondary, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 22)
        }
        root.addView(subtitle, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = rounded(surface, 22f, border)
            elevation = 3f
        }
        root.addView(card, LinearLayout.LayoutParams(-1, -2))

        val login = editField("Użytkownik", false)
        card.addView(login, LinearLayout.LayoutParams(-1, 58).apply {
            bottomMargin = 10
        })

        val password = editField("Hasło", true)
        card.addView(password, LinearLayout.LayoutParams(-1, 58))

        // TextView is used instead of the platform Button so the label is always
        // rendered with our explicit foreground/background colors.
        val button = actionView("ZALOGUJ", 16f, Color.WHITE, primary, 18f, true)
        card.addView(button, LinearLayout.LayoutParams(-1, 58).apply {
            topMargin = 16
        })

        val note = text("TRYB DEMONSTRACYJNY  •  BEZ SERWERA", 12f, textSecondary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        root.addView(note, matchWrap())
        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 0.45f))

        button.setOnClickListener {
            val value = login.text.toString().trim()
            loggedUser = if (value.isEmpty()) "bartek" else value
            showConversations()
        }

        setContentView(root)
    }

    private fun showConversations() {
        val root = vertical(bg, 14)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(2, 6, 2, 4)
        }

        val title = text("Rozmowy", 30f, textPrimary, true).apply {
            includeFontPadding = true
        }
        header.addView(title, LinearLayout.LayoutParams(0, 58, 1f))

        val add = iconButton("+", 27f)
        header.addView(add, LinearLayout.LayoutParams(54, 54))
        add.setOnClickListener { showNewConversationDialog() }
        root.addView(header, LinearLayout.LayoutParams(-1, 64))

        val account = text("Zalogowano jako  $loggedUser", 15f, textSecondary, false).apply {
            setPadding(4, 0, 4, 14)
        }
        root.addView(account, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 12, 12, 12)
            background = rounded(surface, 20f, border)
            elevation = 2f
            isClickable = true
            isFocusable = true
        }

        card.addView(avatar("E", 52), LinearLayout.LayoutParams(52, 52))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 0, 10, 0)
        }

        val name = text("Emilka", 19f, textPrimary, true)
        val preview = text("Super, działa!", 15f, textSecondary, false).apply {
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(0, 3, 0, 0)
        }
        info.addView(name, matchWrap())
        info.addView(preview, matchWrap())
        card.addView(info, LinearLayout.LayoutParams(0, -2, 1f))

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        right.addView(text("10:42", 11f, textSecondary, false).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-2, 22))

        right.addView(actionView("1", 12f, primary, chipBg, 12f, true).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(32, 30))

        card.addView(right, LinearLayout.LayoutParams(42, 52))
        card.setOnClickListener { showChat() }

        root.addView(card, matchWrap().apply { bottomMargin = 18 })

        val section = text("PRYWATNE ROZMOWY", 11f, textSecondary, true).apply {
            setPadding(5, 0, 0, 8)
        }
        root.addView(section, matchWrap())

        val empty = text(
            "Każda rozmowa 1:1 jest oddzielnym prywatnym pokojem.\n" +
                    "Wersja demonstracyjna nie łączy się jeszcze z serwerem.",
            15f, textSecondary, false
        ).apply {
            gravity = Gravity.CENTER
            setPadding(20, 12, 20, 0)
            setLineSpacing(1f, 1.15f)
        }
        root.addView(empty, matchWrap())

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))

        val privacy = text("🔒  E2E • prywatność projektowana od początku", 12f, textSecondary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 10)
        }
        root.addView(privacy, matchWrap())

        val logout = actionView("WYLOGUJ", 14f, textPrimary, Color.rgb(232, 234, 241), 17f, true)
        root.addView(logout, LinearLayout.LayoutParams(-1, 56))
        logout.setOnClickListener { showLogin() }

        setContentView(root)
    }

    private fun showNewConversationDialog() {
        val inputUser = EditText(this).apply {
            hint = "Nazwa użytkownika"
            setTextColor(textPrimary)
            setHintTextColor(hintColor)
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            background = rounded(inputBg, 14f)
            setPadding(16, 0, 16, 0)
        }

        val pad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(26, 4, 26, 0)
            addView(inputUser, LinearLayout.LayoutParams(-1, 58))
        }

        AlertDialog.Builder(this)
            .setTitle("Nowa prywatna rozmowa")
            .setMessage("W wersji demonstracyjnej otworzymy przykładową rozmowę 1:1.")
            .setView(pad)
            .setNegativeButton("ANULUJ", null)
            .setPositiveButton("OTWÓRZ") { _, _ -> showChat() }
            .show()
    }

    private fun showChat() {
        attachedUri = null

        val root = vertical(bg, 0)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 6, 8, 6)
            setBackgroundColor(surface)
            elevation = 2f
        }

        val back = iconButton("‹", 29f)
        top.addView(back, LinearLayout.LayoutParams(52, 52))

        top.addView(avatar("E", 46), LinearLayout.LayoutParams(46, 46).apply {
            leftMargin = 6
        })

        val userInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 0, 6, 0)
        }

        userInfo.addView(text("Emilka", 19f, textPrimary, true), matchWrap())
        userInfo.addView(text("online  •  rozmowa prywatna 1:1", 12f, textSecondary, false), matchWrap())
        top.addView(userInfo, LinearLayout.LayoutParams(0, 60, 1f))

        val menu = iconButton("⋮", 25f)
        top.addView(menu, LinearLayout.LayoutParams(52, 52))

        back.setOnClickListener { showConversations() }
        menu.setOnClickListener { showChatMenu() }

        root.addView(top, LinearLayout.LayoutParams(-1, 68))

        val security = text("🔒  Szyfrowanie end-to-end • tryb demonstracyjny", 11f, textSecondary, true).apply {
            gravity = Gravity.CENTER
            setPadding(8, 7, 8, 7)
            background = rounded(Color.rgb(239, 240, 248), 0f)
        }
        root.addView(security, matchWrap())

        val scroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = false
            setPadding(10, 8, 10, 8)
        }

        messageList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(messageList, ViewGroup.LayoutParams(-1, -2))
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addIncoming("Cześć!")
        addOutgoing("Hej, to jest test naszego komunikatora.")
        addIncoming("Super, działa!")
        addOutgoing(
            "Teraz każda wiadomość jest osobną wysepką. " +
                    "Długi tekst zawija się automatycznie, więc nic nie powinno być ucięte."
        )

        val composerArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 6, 8, 8)
            setBackgroundColor(surface)
            elevation = 5f
        }

        emojiRow = HorizontalScrollView(this).apply {
            visibility = View.GONE
            isHorizontalScrollBarEnabled = false
        }

        val emojiPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        listOf("😀", "😂", "❤️", "👍", "😍", "😎", "🤣", "🔥", "😘", "🎉", "🤝", "😉", "👏", "🥰", "😄")
            .forEach { emojiValue ->
                val b = TextView(this).apply {
                    text = emojiValue
                    textSize = 25f
                    gravity = Gravity.CENTER
                    setPadding(5, 0, 5, 0)
                    isClickable = true
                    setOnClickListener {
                        val pos = input.selectionStart.coerceAtLeast(0)
                        input.text.insert(pos, emojiValue)
                        input.requestFocus()
                    }
                }
                emojiPanel.addView(b, LinearLayout.LayoutParams(48, 48))
            }

        emojiRow.addView(emojiPanel)
        composerArea.addView(emojiRow, LinearLayout.LayoutParams(-1, 50))

        attachmentPreview = text("", 13f, textSecondary, true).apply {
            visibility = View.GONE
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(10, 3, 10, 7)
            background = rounded(Color.rgb(245, 246, 250), 12f)
        }
        composerArea.addView(attachmentPreview, matchWrap())

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val emoji = iconButton("☺", 23f)
        emoji.setOnClickListener {
            emojiRow.visibility =
                if (emojiRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        bar.addView(emoji, LinearLayout.LayoutParams(48, 58))

        val attach = iconButton("+", 27f)
        attach.setOnClickListener {
            pickAttachment.launch(
                arrayOf(
                    "image/*",
                    "application/pdf",
                    "text/plain",
                    "text/csv",
                    "application/zip",
                    "application/octet-stream"
                )
            )
        }
        bar.addView(attach, LinearLayout.LayoutParams(48, 58))

        input = EditText(this).apply {
            hint = "Napisz wiadomość…"
            textSize = 16f
            setTextColor(textPrimary)
            setHintTextColor(hintColor)
            inputType =
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(false)
            minLines = 1
            maxLines = 4
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
            background = rounded(inputBg, 20f, border)
            includeFontPadding = true
        }

        bar.addView(input, LinearLayout.LayoutParams(0, 58, 1f).apply {
            leftMargin = 4
            rightMargin = 5
        })

        val send = iconButton("➤", 23f).apply {
            setTextColor(Color.WHITE)
            background = rounded(primary, 19f)
        }

        send.setOnClickListener {
            val msg = input.text.toString().trim()
            if (msg.isNotEmpty() || attachedUri != null) {
                if (msg.isNotEmpty()) addOutgoing(msg)
                attachedUri?.let { addOutgoingAttachment(it) }
                input.text.clear()
                attachedUri = null
                attachmentPreview.visibility = View.GONE
                emojiRow.visibility = View.GONE
            }
        }

        bar.addView(send, LinearLayout.LayoutParams(56, 58))
        composerArea.addView(bar, LinearLayout.LayoutParams(-1, 62))
        root.addView(composerArea, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
    }

    private fun showChatMenu() {
        val options = arrayOf(
            "Informacje o rozmowie",
            "Wyszukaj w rozmowie",
            "Wyczyść rozmowę",
            "Wycisz powiadomienia",
            "Zablokuj użytkownika"
        )

        AlertDialog.Builder(this)
            .setTitle("Emilka")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showInfoDialog()
                    1 -> showSearchDialog()
                    2 -> {
                        messageList.removeAllViews()
                        Toast.makeText(this, "Rozmowa wyczyszczona (demo)", Toast.LENGTH_SHORT).show()
                    }
                    3 -> Toast.makeText(this, "Powiadomienia wyciszone (demo)", Toast.LENGTH_SHORT).show()
                    4 -> Toast.makeText(this, "Użytkownik zablokowany (demo)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ANULUJ", null)
            .show()
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Informacje o rozmowie")
            .setMessage(
                "Emilka\n\n" +
                        "Typ: prywatna rozmowa 1:1\n" +
                        "Szyfrowanie: E2E (planowane w warstwie komunikacyjnej)\n" +
                        "Serwer: brak połączenia w tej wersji\n\n" +
                        "W przyszłej wersji klucze szyfrowania będą należeć do urządzeń użytkowników."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSearchDialog() {
        val search = EditText(this).apply {
            hint = "Szukaj wiadomości"
            setTextColor(textPrimary)
            setHintTextColor(hintColor)
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(16, 0, 16, 0)
            background = rounded(inputBg, 14f)
        }

        val box = LinearLayout(this).apply {
            setPadding(24, 6, 24, 0)
            addView(search, LinearLayout.LayoutParams(-1, 56))
        }

        AlertDialog.Builder(this)
            .setTitle("Szukaj w rozmowie")
            .setView(box)
            .setNegativeButton("ANULUJ", null)
            .setPositiveButton("SZUKAJ") { _, _ ->
                Toast.makeText(this, "Wyszukiwanie: ${search.text}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun addIncoming(message: String) = addBubble(message, false, null)
    private fun addOutgoing(message: String) = addBubble(message, true, null)
    private fun addOutgoingAttachment(uri: Uri) =
        addBubble("Załącznik\n${displayName(uri)}", true, uri)

    private fun addBubble(message: String, outgoing: Boolean, uri: Uri?) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (outgoing) Gravity.END else Gravity.START
            setPadding(0, 2, 0, 4)
        }

        val author = text(
            if (outgoing) loggedUser else "Emilka",
            12f,
            textSecondary,
            true
        ).apply {
            setPadding(10, 0, 10, 4)
        }
        wrapper.addView(author, LinearLayout.LayoutParams(-1, 22))

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            background = rounded(
                if (outgoing) outgoingBubble else incomingBubble,
                20f,
                if (outgoing) outgoingBubble else border
            )
            elevation = 1.5f
        }

        val body = text(
            message,
            17f,
            if (outgoing) Color.WHITE else textPrimary,
            false
        ).apply {
            setLineSpacing(2f, 1.08f)
            includeFontPadding = true
            maxWidth = (resources.displayMetrics.widthPixels * 0.80f).toInt()
            breakStrategy = android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
            hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NORMAL
        }

        bubble.addView(body, LinearLayout.LayoutParams(-2, -2))

        if (uri != null && contentResolver.getType(uri)?.startsWith("image/") == true) {
            try {
                val image = ImageView(this).apply {
                    setImageURI(uri)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                    background = rounded(Color.TRANSPARENT, 16f)
                }
                bubble.addView(
                    image,
                    LinearLayout.LayoutParams(280, 190).apply {
                        topMargin = 9
                    }
                )
            } catch (_: Exception) {}
        }

        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                if (outgoing) 34 else 2,
                1,
                if (outgoing) 2 else 34,
                9
            )
        }

        wrapper.addView(bubble, lp)
        messageList.addView(wrapper)

        messageList.post {
            (messageList.parent as? ScrollView)?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun displayName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "plik"
        try {
            contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) name = cursor.getString(0)
            }
        } catch (_: Exception) {}
        return name
    }

    private fun editField(hint: String, password: Boolean): EditText =
        EditText(this).apply {
            this.hint = hint
            textSize = 16f
            setTextColor(textPrimary)
            setHintTextColor(hintColor)
            setSingleLine(true)
            inputType =
                if (password) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT
                }
            setPadding(16, 0, 16, 0)
            background = rounded(inputBg, 16f)
        }

    private fun actionView(
        label: String,
        size: Float,
        foreground: Int,
        backgroundColor: Int,
        radius: Float,
        bold: Boolean
    ): TextView = TextView(this).apply {
        text = label
        textSize = size
        setTextColor(foreground)
        gravity = Gravity.CENTER
        includeFontPadding = true
        if (bold) typeface = Typeface.DEFAULT_BOLD
        background = rounded(backgroundColor, radius)
        isClickable = true
        isFocusable = true
        minimumHeight = 0
    }

    private fun vertical(color: Int, padding: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(padding, padding, padding, padding)
        setBackgroundColor(color)
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = true
        }

    private fun matchWrap() =
        LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun rounded(
        color: Int,
        radius: Float,
        stroke: Int = Color.TRANSPARENT
    ): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
        if (stroke != Color.TRANSPARENT) setStroke(1, stroke)
    }

    private fun avatar(letter: String, size: Int) = TextView(this).apply {
        text = letter
        textSize = if (size >= 50) 21f else 19f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        background = rounded(primary, 100f)
    }

    private fun iconButton(label: String, size: Float) =
        actionView(
            label,
            size,
            textPrimary,
            Color.rgb(232, 234, 241),
            17f,
            true
        )
}
