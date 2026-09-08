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
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private data class Palette(
        val bg: Int,
        val surface: Int,
        val inputBg: Int,
        val primary: Int,
        val primaryDark: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val hint: Int,
        val border: Int,
        val outgoing: Int,
        val incoming: Int,
        val chip: Int,
        val toolbar: Int,
        val security: Int,
        val soft: Int
    )

    private lateinit var p: Palette

    private var loggedUser = "bartek"

    private enum class Screen { LOGIN, CONVERSATIONS, CHAT }
    private var currentScreen = Screen.LOGIN
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
        val savedMode = getSharedPreferences("settings", MODE_PRIVATE)
            .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedMode)

        super.onCreate(savedInstanceState)

        p = palette()

        // Android 15+ uses edge-to-edge for targetSdk 35. We explicitly manage
        // system-bar, display-cutout and keyboard insets. App content lives strictly
        // inside the safe rectangle between Android's top and bottom system areas.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Hardware/system Back: navigate inside the app instead of closing it.
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentScreen) {
                    Screen.CHAT -> showConversations()
                    Screen.CONVERSATIONS -> showLogin()
                    Screen.LOGIN -> finish()
                }
            }
        })

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLightTheme()
            isAppearanceLightNavigationBars = isLightTheme()
        }

        showLogin()
    }

    private fun palette() = Palette(
        bg = color(R.color.background),
        surface = color(R.color.surface),
        inputBg = color(R.color.surface_alt),
        primary = color(R.color.primary),
        primaryDark = color(R.color.primary_dark),
        textPrimary = color(R.color.text_primary),
        textSecondary = color(R.color.text_secondary),
        hint = color(R.color.text_hint),
        border = color(R.color.border),
        outgoing = color(R.color.outgoing),
        incoming = color(R.color.incoming),
        chip = color(R.color.chip),
        toolbar = color(R.color.toolbar),
        security = color(R.color.security),
        soft = color(R.color.soft)
    )

    private fun color(id: Int) = ContextCompat.getColor(this, id)

    private fun isLightTheme(): Boolean =
        (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) !=
                android.content.res.Configuration.UI_MODE_NIGHT_YES

    private fun applySafeInsets(root: View, baseLeft: Int, baseTop: Int, baseRight: Int, baseBottom: Int) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Use the largest safe inset on each edge. This prevents any app island
            // from entering the status bar, camera cutout, navigation bar or keyboard.
            val safeLeft = maxOf(bars.left, cutout.left)
            val safeTop = maxOf(bars.top, cutout.top)
            val safeRight = maxOf(bars.right, cutout.right)
            val safeBottom = maxOf(bars.bottom, cutout.bottom, ime.bottom)

            view.setPadding(
                baseLeft + safeLeft,
                baseTop + safeTop,
                baseRight + safeRight,
                baseBottom + safeBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun showLogin() {
        currentScreen = Screen.LOGIN
        val root = vertical(p.bg, 20)
        root.gravity = Gravity.CENTER_HORIZONTAL
        applySafeInsets(root, 20, 18, 20, 20)

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 0.13f))

        val logo = text("B&E", 42f, p.primary, true).apply {
            gravity = Gravity.CENTER
        }
        root.addView(logo, matchWrap())

        val title = text("B&E Messenger", 28f, p.textPrimary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 0)
        }
        root.addView(title, matchWrap())

        val subtitle = text("Prywatny komunikator", 17f, p.textSecondary, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 22)
        }
        root.addView(subtitle, matchWrap())

        val card = island(p.surface, 22f).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        root.addView(card, LinearLayout.LayoutParams(-1, -2))

        val login = editField("Użytkownik", false)
        card.addView(login, LinearLayout.LayoutParams(-1, 58).apply { bottomMargin = 10 })

        val password = editField("Hasło", true)
        card.addView(password, LinearLayout.LayoutParams(-1, 58))

        val button = actionView("ZALOGUJ", 16f, Color.WHITE, p.primary, 18f, true)
        card.addView(button, LinearLayout.LayoutParams(-1, 58).apply { topMargin = 16 })

        val note = text("TRYB DEMONSTRACYJNY  •  BEZ SERWERA", 12f, p.textSecondary, true).apply {
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
        currentScreen = Screen.CONVERSATIONS
        val root = vertical(p.bg, 10)
        applySafeInsets(root, 10, 14, 10, 12)

        // One real container (“island”) owns the complete header. The title is
        // WRAP_CONTENT, never a fixed-height TextView, so Android cannot clip it.
        val headerIsland = island(p.surface, 22f).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 10, 10, 10)
            elevation = 2f
        }

        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = 74
        }
        val title = text("Rozmowy", 30f, p.textPrimary, true).apply {
            includeFontPadding = true
            setPadding(0, 2, 0, 2)
        }
        titleBlock.addView(title, matchWrap())
        val account = text("Zalogowano jako  $loggedUser", 15f, p.textSecondary, false).apply {
            includeFontPadding = true
            setPadding(0, 1, 0, 1)
        }
        titleBlock.addView(account, matchWrap())
        headerIsland.addView(titleBlock, LinearLayout.LayoutParams(0, -2, 1f))

        val settings = iconButton("⚙", 24f)
        headerIsland.addView(settings, LinearLayout.LayoutParams(58, 58).apply { rightMargin = 7 })
        settings.setOnClickListener { showSettings() }

        val add = iconButton("+", 28f)
        headerIsland.addView(add, LinearLayout.LayoutParams(58, 58))
        add.setOnClickListener { showNewConversationDialog() }

        root.addView(headerIsland, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 16 })

        val card = island(p.surface, 20f).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 13, 12, 13)
            elevation = 2f
            isClickable = true
            isFocusable = true
        }

        card.addView(avatar("E", 52), LinearLayout.LayoutParams(52, 52))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 0, 10, 0)
        }
        val name = text("Emilka", 20f, p.textPrimary, true).apply {
            includeFontPadding = true
        }
        val preview = text("Super, działa!", 15f, p.textSecondary, false).apply {
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            includeFontPadding = true
            setPadding(0, 3, 0, 0)
        }
        info.addView(name, matchWrap())
        info.addView(preview, matchWrap())
        card.addView(info, LinearLayout.LayoutParams(0, -2, 1f))

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        right.addView(text("10:42", 11f, p.textSecondary, false).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-2, 24))
        right.addView(actionView("1", 12f, p.primary, p.chip, 12f, true), LinearLayout.LayoutParams(34, 32))
        card.addView(right, LinearLayout.LayoutParams(42, 56))
        card.setOnClickListener { showChat() }

        root.addView(card, matchWrap().apply { bottomMargin = 24 })

        val section = text("PRYWATNE ROZMOWY", 12f, p.textSecondary, true).apply {
            setPadding(5, 0, 0, 9)
        }
        root.addView(section, matchWrap())

        val empty = text(
            "Każda rozmowa 1:1 jest oddzielnym prywatnym pokojem.\n" +
                    "Wersja demonstracyjna nie łączy się jeszcze z serwerem.",
            15f, p.textSecondary, false
        ).apply {
            gravity = Gravity.CENTER
            includeFontPadding = true
            setPadding(20, 12, 20, 0)
            setLineSpacing(1f, 1.15f)
        }
        root.addView(empty, matchWrap())

        root.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))

        val privacy = text("🔒  E2E • prywatność projektowana od początku", 12f, p.textSecondary, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 10)
        }
        root.addView(privacy, matchWrap())

        val logout = actionView("WYLOGUJ", 14f, p.textPrimary, p.soft, 17f, true)
        root.addView(logout, LinearLayout.LayoutParams(-1, 56))
        logout.setOnClickListener { showLogin() }

        setContentView(root)
    }

    private fun showChat() {
        currentScreen = Screen.CHAT
        attachedUri = null

        val root = vertical(p.bg, 0)
        applySafeInsets(root, 6, 8, 6, 8)

        // Chat header is also one self-contained island. Both lines are WRAP_CONTENT
        // and the island has generous vertical padding/minHeight instead of a clipped
        // fixed TextView height.
        val top = island(p.toolbar, 20f).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 9, 8, 9)
            elevation = 2f
        }

        val back = iconButton("‹", 31f).apply { contentDescription = "Wstecz" }
        top.addView(back, LinearLayout.LayoutParams(60, 60))

        top.addView(avatar("E", 52), LinearLayout.LayoutParams(52, 52).apply { leftMargin = 7 })

        val userInfo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = 74
            setPadding(12, 2, 8, 2)
        }
        userInfo.addView(text("Rozmowa z Emilką", 20f, p.textPrimary, true).apply {
            includeFontPadding = true
            setPadding(0, 1, 0, 2)
        }, matchWrap())
        userInfo.addView(text("online  •  prywatna rozmowa 1:1", 13f, p.textSecondary, false).apply {
            includeFontPadding = true
        }, matchWrap())
        top.addView(userInfo, LinearLayout.LayoutParams(0, -2, 1f))

        val menu = iconButton("⋮", 27f).apply { contentDescription = "Menu rozmowy" }
        top.addView(menu, LinearLayout.LayoutParams(60, 60))

        back.setOnClickListener { showConversations() }
        menu.setOnClickListener { showChatMenu() }
        root.addView(top, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })

        val security = text("🔒  Szyfrowanie end-to-end • tryb demonstracyjny", 12f, p.textSecondary, true).apply {
            gravity = Gravity.CENTER
            includeFontPadding = true
            setPadding(10, 8, 10, 8)
            background = rounded(p.security, 0f)
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
            "Teraz każda wiadomość jest osobną wysepką. Długi tekst zawija się automatycznie, " +
                    "więc nic nie powinno być ucięte."
        )

        val composerArea = island(p.surface, 20f).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 10)
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
                emojiPanel.addView(b, LinearLayout.LayoutParams(48, 50))
            }
        emojiRow.addView(emojiPanel)
        composerArea.addView(emojiRow, LinearLayout.LayoutParams(-1, 50))

        attachmentPreview = text("", 13f, p.textSecondary, true).apply {
            visibility = View.GONE
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(10, 4, 10, 7)
            background = rounded(p.soft, 12f)
        }
        composerArea.addView(attachmentPreview, matchWrap())

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val emoji = iconButton("☺", 24f)
        emoji.setOnClickListener {
            emojiRow.visibility = if (emojiRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        bar.addView(emoji, LinearLayout.LayoutParams(52, 62))

        val attach = iconButton("+", 28f)
        attach.setOnClickListener {
            pickAttachment.launch(
                arrayOf("image/*", "application/pdf", "text/plain", "text/csv", "application/zip", "application/octet-stream")
            )
        }
        bar.addView(attach, LinearLayout.LayoutParams(52, 62))

        input = EditText(this).apply {
            hint = "Napisz wiadomość…"
            textSize = 17f
            setTextColor(p.textPrimary)
            setHintTextColor(p.hint)
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(false)
            minLines = 1
            maxLines = 4
            gravity = Gravity.CENTER_VERTICAL
            setPadding(17, 10, 17, 10)
            background = rounded(p.inputBg, 22f, p.border)
            includeFontPadding = true
        }
        bar.addView(input, LinearLayout.LayoutParams(0, 66, 1f).apply {
            leftMargin = 5
            rightMargin = 6
        })

        val send = iconButton("➤", 25f).apply {
            setTextColor(Color.WHITE)
            background = rounded(p.primary, 22f)
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
        bar.addView(send, LinearLayout.LayoutParams(62, 62))
        composerArea.addView(bar, LinearLayout.LayoutParams(-1, 70))

        root.addView(composerArea, LinearLayout.LayoutParams(-1, -2).apply {
            leftMargin = 6
            rightMargin = 6
            topMargin = 6
            bottomMargin = 8
        })

        setContentView(root)
    }

    private fun showSettings() {
        val choices = arrayOf("Systemowy", "Jasny", "Ciemny")
        val current = getSharedPreferences("settings", MODE_PRIVATE)
            .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val checked = when (current) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("Wygląd aplikacji")
            .setSingleChoiceItems(choices, checked) { dialog, which ->
                val mode = when (which) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                getSharedPreferences("settings", MODE_PRIVATE)
                    .edit().putInt("theme_mode", mode).apply()
                dialog.dismiss()
                AppCompatDelegate.setDefaultNightMode(mode)
            }
            .setNegativeButton("ANULUJ", null)
            .show()
    }

    private fun showNewConversationDialog() {
        val inputUser = EditText(this).apply {
            hint = "Nazwa użytkownika"
            setTextColor(p.textPrimary)
            setHintTextColor(p.hint)
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            background = rounded(p.inputBg, 14f, p.border)
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

    private fun showChatMenu() {
        val options = arrayOf(
            "Informacje o rozmowie",
            "Wyszukaj w rozmowie",
            "Wyczyść rozmowę",
            "Wycisz powiadomienia",
            "Zablokuj użytkownika",
            "Wygląd aplikacji"
        )
        AlertDialog.Builder(this)
            .setTitle("Rozmowa z Emilką")
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
                    5 -> showSettings()
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
            setTextColor(p.textPrimary)
            setHintTextColor(p.hint)
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(16, 0, 16, 0)
            background = rounded(p.inputBg, 14f, p.border)
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
    private fun addOutgoingAttachment(uri: Uri) = addBubble("Załącznik\n${displayName(uri)}", true, uri)

    private fun addBubble(message: String, outgoing: Boolean, uri: Uri?) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (outgoing) Gravity.END else Gravity.START
            setPadding(0, 2, 0, 4)
        }

        val author = text(if (outgoing) loggedUser else "Emilka", 14f, p.textSecondary, true).apply {
            includeFontPadding = true
            setPadding(10, 2, 10, 6)
        }
        wrapper.addView(author, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            background = rounded(if (outgoing) p.outgoing else p.incoming, 20f, if (outgoing) p.outgoing else p.border)
            elevation = 1.5f
        }

        val body = text(message, 17f, if (outgoing) Color.WHITE else p.textPrimary, false).apply {
            setLineSpacing(3f, 1.10f)
            includeFontPadding = true
            minLines = 1
            maxWidth = (resources.displayMetrics.widthPixels * 0.82f).toInt()
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
                }
                bubble.addView(image, LinearLayout.LayoutParams(280, 190).apply { topMargin = 9 })
            } catch (_: Exception) {}
        }

        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(if (outgoing) 28 else 4, 1, if (outgoing) 4 else 28, 12)
        }
        wrapper.addView(bubble, lp)
        messageList.addView(wrapper)
        messageList.post { (messageList.parent as? ScrollView)?.fullScroll(View.FOCUS_DOWN) }
    }

    private fun displayName(uri: Uri): String {
        var name = uri.lastPathSegment ?: "plik"
        try {
            contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) name = cursor.getString(0)
            }
        } catch (_: Exception) {}
        return name
    }

    private fun editField(hint: String, password: Boolean): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 16f
        setTextColor(p.textPrimary)
        setHintTextColor(p.hint)
        setSingleLine(true)
        inputType = if (password) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else InputType.TYPE_CLASS_TEXT
        setPadding(16, 0, 16, 0)
        background = rounded(p.inputBg, 16f, p.border)
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
        minHeight = 0
    }

    private fun vertical(color: Int, padding: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(padding, padding, padding, padding)
        setBackgroundColor(color)
    }

    private fun island(color: Int, radius: Float) = LinearLayout(this).apply {
        background = rounded(color, radius, p.border)
        isFocusable = false
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
        background = rounded(p.primary, 100f)
    }

    private fun iconButton(label: String, size: Float) = actionView(
        label, size, p.textPrimary, p.soft, 17f, true
    )
}
