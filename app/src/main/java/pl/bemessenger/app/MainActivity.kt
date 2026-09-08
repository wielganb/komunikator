package pl.bemessenger.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(246, 247, 251)
    private val surface = Color.WHITE
    private val inputBg = Color.rgb(238, 240, 247)
    private val primary = Color.rgb(82, 84, 224)
    private val primaryDark = Color.rgb(59, 61, 181)
    private val textPrimary = Color.rgb(25, 27, 38)
    private val textSecondary = Color.rgb(92, 98, 120)
    private val hintColor = Color.rgb(125, 130, 150)
    private val border = Color.rgb(222, 224, 234)

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
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
            attachedUri = uri
            attachmentPreview.text = "Załącznik: ${displayName(uri)}"
            attachmentPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
        showLogin()
    }

    private fun installInsets(view: View, top: Boolean = true, bottom: Boolean = true) {
        val baseLeft = view.paddingLeft
        val baseTop = view.paddingTop
        val baseRight = view.paddingRight
        val baseBottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = baseLeft,
                top = baseTop + if (top) bars.top else 0,
                right = baseRight,
                bottom = baseBottom + if (bottom) bars.bottom else 0
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    private fun showLogin() {
        val root = vertical(bg, 22)
        installInsets(root)

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 0.18f))

        val logo = text("B&E", 42f, primary, true).apply { gravity = Gravity.CENTER }
        root.addView(logo, matchWrap())

        val title = text("B&E Messenger", 28f, textPrimary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 0)
        }
        root.addView(title, matchWrap())

        val subtitle = text("Prywatny komunikator", 16f, textSecondary, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 24)
        }
        root.addView(subtitle, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = rounded(surface, 22f, border)
            elevation = 3f
        }
        root.addView(card, LinearLayout.LayoutParams(-1, -2))

        val login = editField("Użytkownik", false)
        card.addView(login, LinearLayout.LayoutParams(-1, 58).apply { setMargins(0, 0, 0, 12) })

        val password = editField("Hasło", true)
        card.addView(password, LinearLayout.LayoutParams(-1, 58))

        val button = primaryButton("ZALOGUJ")
        card.addView(button, LinearLayout.LayoutParams(-1, 58).apply { setMargins(0, 18, 0, 0) })

        val note = text("TRYB DEMONSTRACYJNY  •  BEZ SERWERA", 12f, textSecondary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 0)
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
        val root = vertical(bg, 16)
        installInsets(root)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(2, 10, 2, 4)
        }
        val title = text("Rozmowy", 31f, textPrimary, true)
        header.addView(title, LinearLayout.LayoutParams(0, 58, 1f))
        val add = iconButton("+", 28f)
        header.addView(add, LinearLayout.LayoutParams(54, 54))
        add.setOnClickListener { showNewConversationDialog() }
        root.addView(header, matchWrap())

        val account = text("Zalogowano jako  $loggedUser", 15f, textSecondary, false)
        account.setPadding(4, 0, 4, 18)
        root.addView(account, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 13, 14, 13)
            background = rounded(surface, 20f, border)
            elevation = 2f
            isClickable = true
            isFocusable = true
        }
        card.addView(avatar("E", 52), LinearLayout.LayoutParams(52, 52))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 0, 12, 0)
        }
        info.addView(text("Emilka", 19f, textPrimary, true), matchWrap())
        info.addView(text("Super, działa!", 15f, textSecondary, false), matchWrap())
        card.addView(info, LinearLayout.LayoutParams(0, -2, 1f))

        val badge = text("1:1", 12f, primary, true).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(237, 238, 255), 12f)
        }
        card.addView(badge, LinearLayout.LayoutParams(48, 34))
        card.setOnClickListener { showChat() }
        root.addView(card, matchWrap().apply { setMargins(0, 0, 0, 18) })

        val empty = text("Twoje prywatne rozmowy pojawią się tutaj.", 15f, textSecondary, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 0)
        }
        root.addView(empty, matchWrap())
        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))

        val logout = secondaryButton("WYLOGUJ")
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
        }
        val pad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 4, 28, 0)
            addView(inputUser, LinearLayout.LayoutParams(-1, 58))
        }
        AlertDialog.Builder(this)
            .setTitle("Nowa rozmowa")
            .setMessage("W wersji demonstracyjnej możesz otworzyć przykładową rozmowę 1:1.")
            .setView(pad)
            .setNegativeButton("ANULUJ", null)
            .setPositiveButton("OTWÓRZ") { _, _ -> showChat() }
            .show()
    }

    private fun showChat() {
        val root = vertical(bg, 0)
        installInsets(root, top = true, bottom = true)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 6, 10, 6)
            setBackgroundColor(surface)
            elevation = 2f
        }
        val back = iconButton("‹", 29f)
        top.addView(back, LinearLayout.LayoutParams(52, 52))
        top.addView(avatar("E", 46), LinearLayout.LayoutParams(46, 46).apply { setMargins(6, 0, 0, 0) })

        val userInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 8, 0)
        }
        userInfo.addView(text("Emilka", 19f, textPrimary, true), matchWrap())
        userInfo.addView(text("Prywatna rozmowa 1:1", 12f, textSecondary, false), matchWrap())
        top.addView(userInfo, LinearLayout.LayoutParams(0, 58, 1f))

        val menu = iconButton("⋮", 26f)
        top.addView(menu, LinearLayout.LayoutParams(52, 52))
        menu.setOnClickListener { showChatMenu(menu) }
        back.setOnClickListener { showConversations() }
        root.addView(top, LinearLayout.LayoutParams(-1, 68))

        val scroll = ScrollView(this).apply {
            setFillViewport(true)
            clipToPadding = false
            setPadding(10, 12, 10, 12)
        }
        messageList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(messageList, ViewGroup.LayoutParams(-1, -2))
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addIncoming("Cześć!")
        addOutgoing("Hej, to jest test naszego komunikatora.")
        addIncoming("Super, działa!")
        addOutgoing("Teraz wygląda dużo lepiej. Każda wiadomość jest osobną wysepką i cały tekst powinien być widoczny.")

        val composerArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 7, 8, 8)
            setBackgroundColor(surface)
            elevation = 5f
        }

        emojiRow = HorizontalScrollView(this).apply {
            visibility = View.GONE
            isHorizontalScrollBarEnabled = false
        }
        val emojiPanel = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("😀", "😂", "❤️", "👍", "😍", "😎", "🤣", "🔥", "😘", "🎉", "🤝", "😉", "👏", "🥰", "😄").forEach { emoji ->
            val b = TextView(this).apply {
                text = emoji
                textSize = 25f
                gravity = Gravity.CENTER
                setPadding(8, 0, 8, 0)
                setOnClickListener {
                    val pos = input.selectionStart.coerceAtLeast(0)
                    input.text.insert(pos, emoji)
                    input.requestFocus()
                }
            }
            emojiPanel.addView(b, LinearLayout.LayoutParams(48, 48))
        }
        emojiRow.addView(emojiPanel)
        composerArea.addView(emojiRow, LinearLayout.LayoutParams(-1, 50))

        attachmentPreview = text("", 13f, textSecondary, true).apply {
            visibility = View.GONE
            setPadding(10, 3, 10, 7)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        composerArea.addView(attachmentPreview, matchWrap())

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val emoji = iconButton("☺", 23f)
        emoji.setOnClickListener {
            emojiRow.visibility = if (emojiRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        bar.addView(emoji, LinearLayout.LayoutParams(48, 58))

        val attach = iconButton("+", 27f)
        attach.setOnClickListener {
            pickAttachment.launch(arrayOf("image/*", "application/pdf", "text/plain", "text/csv", "application/zip", "application/octet-stream"))
        }
        bar.addView(attach, LinearLayout.LayoutParams(48, 58))

        input = EditText(this).apply {
            hint = "Napisz wiadomość…"
            textSize = 16f
            setTextColor(textPrimary)
            setHintTextColor(hintColor)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(false)
            minLines = 1
            maxLines = 4
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
            background = rounded(inputBg, 20f, border)
            includeFontPadding = true
            isVerticalScrollBarEnabled = true
        }
        bar.addView(input, LinearLayout.LayoutParams(0, 58, 1f).apply { setMargins(4, 0, 5, 0) })

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

    private fun showChatMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add("Wyczyść rozmowę")
            menu.add("Informacje o rozmowie")
            menu.add("Zablokuj użytkownika")
            setOnMenuItemClickListener {
                Toast.makeText(this@MainActivity, "Tryb demonstracyjny — opcja: ${it.title}", Toast.LENGTH_SHORT).show()
                true
            }
        }.show()
    }

    private fun addIncoming(message: String) = addBubble(message, false, null)
    private fun addOutgoing(message: String) = addBubble(message, true, null)
    private fun addOutgoingAttachment(uri: Uri) = addBubble("Załącznik\n${displayName(uri)}", true, uri)

    private fun addBubble(message: String, outgoing: Boolean, uri: Uri?) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (outgoing) Gravity.END else Gravity.START
            setPadding(2, 3, 2, 5)
        }

        val author = text(if (outgoing) loggedUser else "Emilka", 12f, textSecondary, true).apply {
            setPadding(10, 0, 10, 4)
        }
        wrapper.addView(author, LinearLayout.LayoutParams(-1, 22))

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            background = rounded(if (outgoing) primary else surface, 20f, if (outgoing) primary else border)
            elevation = 1.5f
        }
        val body = text(message, 17f, if (outgoing) Color.WHITE else textPrimary, false).apply {
            setLineSpacing(1f, 1.08f)
            includeFontPadding = true
            maxWidth = (resources.displayMetrics.widthPixels * 0.78f).toInt()
        }
        bubble.addView(body, LinearLayout.LayoutParams(-2, -2))

        if (uri != null && contentResolver.getType(uri)?.startsWith("image/") == true) {
            try {
                val image = ImageView(this).apply {
                    setImageURI(uri)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = rounded(Color.TRANSPARENT, 16f)
                    adjustViewBounds = true
                }
                bubble.addView(image, LinearLayout.LayoutParams(260, 190).apply { setMargins(0, 9, 0, 0) })
            } catch (_: Exception) { }
        }

        val lp = LinearLayout.LayoutParams(-2, -2).apply {
            setMargins(if (outgoing) 48 else 2, 1, if (outgoing) 2 else 48, 9)
        }
        wrapper.addView(bubble, lp)
        messageList.addView(wrapper)
        messageList.post { (messageList.parent as? ScrollView)?.fullScroll(View.FOCUS_DOWN) }
    }

    private fun displayName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "plik"
        try {
            contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) name = c.getString(0)
            }
        } catch (_: Exception) { }
        return name
    }

    private fun editField(hint: String, password: Boolean): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 16f
        setTextColor(textPrimary)
        setHintTextColor(hintColor)
        setSingleLine(true)
        inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT
        setPadding(16, 0, 16, 0)
        background = rounded(inputBg, 16f, Color.TRANSPARENT)
    }

    private fun vertical(color: Int, padding: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(padding, padding, padding, padding)
        setBackgroundColor(color)
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = true
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun rounded(color: Int, radius: Float, stroke: Int = Color.TRANSPARENT): GradientDrawable = GradientDrawable().apply {
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

    private fun primaryButton(label: String) = Button(this).apply {
        text = label
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        background = rounded(primary, 17f)
        stateListAnimator = null
        minHeight = 0
        minimumHeight = 0
    }

    private fun secondaryButton(label: String) = Button(this).apply {
        text = label
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(textPrimary)
        background = rounded(Color.rgb(232, 234, 241), 17f)
        stateListAnimator = null
        minHeight = 0
        minimumHeight = 0
    }

    private fun iconButton(label: String, size: Float) = TextView(this).apply {
        text = label
        textSize = size
        gravity = Gravity.CENTER
        setTextColor(textPrimary)
        background = rounded(Color.rgb(232, 234, 241), 17f)
        isClickable = true
        isFocusable = true
    }
}
