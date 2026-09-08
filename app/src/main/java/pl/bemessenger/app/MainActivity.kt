package pl.bemessenger.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val bg = Color.rgb(246, 247, 251)
    private val surface = Color.WHITE
    private val primary = Color.rgb(91, 92, 226)
    private val textPrimary = Color.rgb(23, 25, 35)
    private val textSecondary = Color.rgb(112, 117, 138)

    private var loggedUser = "bartek"
    private var attachedUri: Uri? = null
    private lateinit var messageList: LinearLayout
    private lateinit var input: EditText

    private val pickAttachment = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            attachedUri = uri
            showAttachmentPreview(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        val root = vertical(bg, 24)
        val spaceTop = Space(this)
        root.addView(spaceTop, LinearLayout.LayoutParams(1, 0, 0.25f))

        val logo = TextView(this).apply {
            text = "B&E"
            textSize = 38f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primary)
            gravity = Gravity.CENTER
        }
        root.addView(logo, matchWrap())

        val title = text("B&E Messenger", 28f, textPrimary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 0)
        }
        root.addView(title, matchWrap())

        val subtitle = text("Prywatny komunikator", 15f, textSecondary, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 28)
        }
        root.addView(subtitle, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 20)
            background = rounded(surface, 20f)
            elevation = 5f
        }
        root.addView(card, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 0) })

        val login = EditText(this).apply {
            hint = "Użytkownik"
            setSingleLine(true)
            textSize = 16f
            setPadding(16, 0, 16, 0)
            background = rounded(Color.rgb(242,243,248), 14f)
        }
        card.addView(login, LinearLayout.LayoutParams(-1, 58).apply { setMargins(0, 0, 0, 10) })

        val password = EditText(this).apply {
            hint = "Hasło"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
            textSize = 16f
            setPadding(16, 0, 16, 0)
            background = rounded(Color.rgb(242,243,248), 14f)
        }
        card.addView(password, LinearLayout.LayoutParams(-1, 58))

        val button = primaryButton("Zaloguj")
        card.addView(button, LinearLayout.LayoutParams(-1, 56).apply { setMargins(0, 18, 0, 0) })

        val note = text(
            "Tryb demonstracyjny • bez połączenia z serwerem",
            12f, textSecondary, false
        ).apply { gravity = Gravity.CENTER; setPadding(0, 18, 0, 0) }
        root.addView(note, matchWrap())

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 0.5f))
        setContentView(root)

        button.setOnClickListener {
            val value = login.text.toString().trim()
            loggedUser = if (value.isEmpty()) "bartek" else value
            showConversations()
        }
    }

    private fun showConversations() {
        val root = vertical(bg, 18)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 8, 4, 12)
        }
        val title = text("Rozmowy", 30f, textPrimary, true)
        header.addView(title, LinearLayout.LayoutParams(0, 56, 1f))
        val add = iconButton("+")
        header.addView(add, LinearLayout.LayoutParams(50, 50))
        root.addView(header, matchWrap())

        val account = text("Zalogowano jako  $loggedUser", 14f, textSecondary, false)
        account.setPadding(4, 0, 4, 18)
        root.addView(account, matchWrap())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 12, 14, 12)
            background = rounded(surface, 18f)
            elevation = 2f
        }
        val avatar = avatar("E")
        card.addView(avatar, LinearLayout.LayoutParams(52, 52))
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(14,0,0,0) }
        val name = text("Emilka", 18f, textPrimary, true)
        val last = text("Super, działa!", 14f, textSecondary, false)
        info.addView(name, matchWrap())
        info.addView(last, matchWrap())
        card.addView(info, LinearLayout.LayoutParams(0, 64, 1f))
        val badge = text("1:1", 12f, primary, true).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(238,239,255), 10f)
        }
        card.addView(badge, LinearLayout.LayoutParams(46, 32))
        card.setOnClickListener { showChat() }
        root.addView(card, matchWrap().apply { setMargins(0, 0, 0, 12) })

        val empty = text(
            "Twoje rozmowy prywatne pojawią się tutaj.",
            14f, textSecondary, false
        ).apply { gravity = Gravity.CENTER; setPadding(0, 20, 0, 0) }
        root.addView(empty, matchWrap())

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))
        val logout = secondaryButton("Wyloguj")
        root.addView(logout, LinearLayout.LayoutParams(-1, 54))
        logout.setOnClickListener { showLogin() }

        setContentView(root)
    }

    private fun showChat() {
        val root = vertical(bg, 0)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 10, 12, 8)
            setBackgroundColor(surface)
            elevation = 2f
        }
        val back = iconButton("‹")
        top.addView(back, LinearLayout.LayoutParams(50, 50))
        val av = avatar("E")
        top.addView(av, LinearLayout.LayoutParams(44, 44).apply { setMargins(4,0,0,0) })
        val userInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 0, 0)
        }
        userInfo.addView(text("Emilka", 18f, textPrimary, true), matchWrap())
        userInfo.addView(text("Prywatna rozmowa 1:1", 12f, textSecondary, false), matchWrap())
        top.addView(userInfo, LinearLayout.LayoutParams(0, 56, 1f))
        val menu = iconButton("⋮")
        top.addView(menu, LinearLayout.LayoutParams(44, 50))
        root.addView(top, LinearLayout.LayoutParams(-1, 70))

        val scroll = ScrollView(this).apply {
            setFillViewport(true)
            setPadding(12, 14, 12, 10)
        }
        messageList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(messageList, ViewGroup.LayoutParams(-1, -2))
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addIncoming("Cześć!")
        addOutgoing("Hej, to jest test naszego komunikatora.")
        addIncoming("Super, działa!")
        addOutgoing("Teraz wygląda dużo lepiej.")

        val composerArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 8, 10, 10)
            setBackgroundColor(surface)
            elevation = 4f
        }

        val emojiRow = HorizontalScrollView(this).apply {
            visibility = View.GONE
        }
        val emojiPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        listOf("😀","😂","❤️","👍","😍","😎","🤣","🔥","😘","🎉","🤝","😉").forEach { emoji ->
            val b = TextView(this).apply {
                text = emoji
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(10, 0, 10, 0)
                setOnClickListener {
                    val pos = input.selectionStart.coerceAtLeast(0)
                    input.text.insert(pos, emoji)
                }
            }
            emojiPanel.addView(b, LinearLayout.LayoutParams(50, 48))
        }
        emojiRow.addView(emojiPanel)
        composerArea.addView(emojiRow, LinearLayout.LayoutParams(-1, 48))

        val attachmentPreview = TextView(this).apply {
            tag = "attachmentPreview"
            visibility = View.GONE
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(10, 4, 10, 8)
        }
        composerArea.addView(attachmentPreview, matchWrap())

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val emoji = iconButton("☺")
        emoji.setOnClickListener {
            emojiRow.visibility = if (emojiRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        bar.addView(emoji, LinearLayout.LayoutParams(48, 52))

        val attach = iconButton("＋")
        attach.setOnClickListener {
            pickAttachment.launch(arrayOf("image/*", "application/pdf", "text/plain", "application/zip"))
        }
        bar.addView(attach, LinearLayout.LayoutParams(48, 52))

        input = EditText(this).apply {
            hint = "Napisz wiadomość…"
            textSize = 16f
            setSingleLine(false)
            maxLines = 4
            setPadding(16, 0, 16, 0)
            background = rounded(Color.rgb(242,243,248), 18f)
        }
        bar.addView(input, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(4,0,4,0) })

        val send = iconButton("➤").apply {
            setTextColor(Color.WHITE)
            background = rounded(primary, 18f)
        }
        send.setOnClickListener {
            val msg = input.text.toString().trim()
            if (msg.isNotEmpty() || attachedUri != null) {
                if (msg.isNotEmpty()) addOutgoing(msg)
                attachedUri?.let { addOutgoingAttachment(it) }
                input.text.clear()
                attachedUri = null
                attachmentPreview.visibility = View.GONE
            }
        }
        bar.addView(send, LinearLayout.LayoutParams(54, 52))
        composerArea.addView(bar, LinearLayout.LayoutParams(-1, 60))

        root.addView(composerArea, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        back.setOnClickListener { showConversations() }
        setContentView(root)
    }

    private fun showAttachmentPreview(uri: Uri) {
        // The actual preview is found recursively by tag.
        val tagged = findViewByTag(window.decorView, "attachmentPreview") as? TextView
        tagged?.apply {
            text = "Załącznik: ${displayName(uri)}"
            visibility = View.VISIBLE
        }
    }

    private fun findViewByTag(view: View, wanted: String): View? {
        if (view.tag == wanted) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findViewByTag(view.getChildAt(i), wanted)
                if (found != null) return found
            }
        }
        return null
    }

    private fun addIncoming(message: String) {
        addBubble(message, false, null)
    }

    private fun addOutgoing(message: String) {
        addBubble(message, true, null)
    }

    private fun addOutgoingAttachment(uri: Uri) {
        addBubble("Załącznik\n${displayName(uri)}", true, uri)
    }

    private fun addBubble(message: String, outgoing: Boolean, uri: Uri?) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (outgoing) Gravity.END else Gravity.START
            setPadding(4, 4, 4, 4)
        }
        val author = text(if (outgoing) loggedUser else "Emilka", 11f, textSecondary, true)
        author.setPadding(10, 0, 10, 3)
        wrapper.addView(author, LinearLayout.LayoutParams(-1, 22))

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 10, 14, 10)
            background = rounded(if (outgoing) primary else surface, 18f)
            elevation = 1.5f
        }
        val body = text(message, 16f, if (outgoing) Color.WHITE else textPrimary, false)
        body.setLineSpacing(0f, 1.1f)
        bubble.addView(body, LinearLayout.LayoutParams(-2, -2))

        if (uri != null && contentResolver.getType(uri)?.startsWith("image/") == true) {
            try {
                val image = ImageView(this).apply {
                    setImageURI(uri)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = rounded(Color.TRANSPARENT, 14f)
                }
                bubble.addView(image, LinearLayout.LayoutParams(220, 160).apply { setMargins(0, 8, 0, 0) })
            } catch (_: Exception) {}
        }

        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(if (outgoing) 64 else 4, 2, if (outgoing) 4 else 64, 8)
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
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) name = c.getString(0)
            }
        } catch (_: Exception) {}
        return name
    }

    private fun vertical(color: Int, padding: Int): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(color)
        }

    private fun text(value: String, size: Float, color: Int, bold: Boolean): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun rounded(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }

    private fun avatar(letter: String): TextView =
        TextView(this).apply {
            text = letter
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = rounded(primary, 100f)
        }

    private fun primaryButton(label: String): Button =
        Button(this).apply {
            text = label
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = rounded(primary, 16f)
            stateListAnimator = null
        }

    private fun secondaryButton(label: String): Button =
        Button(this).apply {
            text = label
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textPrimary)
            background = rounded(Color.rgb(232,233,240), 16f)
            stateListAnimator = null
        }

    private fun iconButton(label: String): TextView =
        TextView(this).apply {
            text = label
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(textPrimary)
            background = rounded(Color.rgb(232,233,240), 16f)
        }
}
