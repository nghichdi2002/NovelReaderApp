package com.namnh.novelreaderapp.user

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.namnh.novelreaderapp.R
import com.namnh.novelreaderapp.databinding.ActivityChapterDetailBinding
import com.namnh.novelreaderapp.item.Chapter
import com.namnh.novelreaderapp.item.Story
import androidx.core.content.edit


class ChapterDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChapterDetailBinding
    private lateinit var userRef: DatabaseReference
    private var userId: String = ""
    private lateinit var story: Story
    private lateinit var chapter: Chapter
    private val chapterList: MutableList<Chapter> = ArrayList()
    private var currentChapterIndex: Int = 0

    companion object {
        private const val PREF_NAME = "reader_settings"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_TEXT_COLOR = "text_color"
        private const val KEY_FONT = "font"
        private const val KEY_BRIGHTNESS = "brightness"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapterDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Các phần khởi tạo dữ liệu và sự kiện
        story = intent.getSerializableExtra("story") as? Story ?: run {
            Toast.makeText(this, "Không tìm thấy dữ liệu truyện.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentChapterIndex = intent.getIntExtra("chapterIndex", 0)
        binding.tvStoryTitle.text = story.title

        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("reading_progress")
                .child(story.id)
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu tiến trình đọc.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        chapterList.addAll(story.chapters.values)
        sortChapters()
        loadChapter(currentChapterIndex)
        updateChapterNavigation()

        // **Cập nhật reading_progress ngay khi mở chapter đầu tiên**
        updateReadingProgress(currentChapterIndex, 0)

        // Khôi phục các thiết lập cá nhân
        restoreSettings()

        binding.ivPreviousChapter.setOnClickListener {
            if (currentChapterIndex > 0) {
                currentChapterIndex--
                loadChapter(currentChapterIndex)
                saveReadingProgress(currentChapterIndex, 0)
            }
        }

        binding.ivNextChapter.setOnClickListener {
            if (currentChapterIndex < chapterList.size - 1) {
                currentChapterIndex++
                loadChapter(currentChapterIndex)
                saveReadingProgress(currentChapterIndex, 0)
            }
        }

        binding.ivMenu.setOnClickListener { showMenu() }
    }

    private fun loadChapter(index: Int) {
        chapter = chapterList[index]
        binding.tvChapterTitle.text = chapter.title
        binding.tvChapterContent.text = chapter.content
        updateChapterNavigation()
    }

    private fun updateChapterNavigation() {
        binding.ivPreviousChapter.visibility = if (currentChapterIndex == 0) View.INVISIBLE else View.VISIBLE
        binding.ivNextChapter.visibility = if (currentChapterIndex == chapterList.size - 1) View.INVISIBLE else View.VISIBLE
    }

    private fun sortChapters() {
        chapterList.sortBy { chapter ->
            val titleNumber = chapter.title.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
            titleNumber
        }
    }

    private fun showMenu() {
        val popupMenu = PopupMenu(this, binding.ivMenu)
        popupMenu.menuInflater.inflate(R.menu.chapter_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_font_size -> {
                    changeFontSize()
                    true
                }
                R.id.menu_brightness -> {
                    adjustBrightness()
                    true
                }
                R.id.menu_background_color -> {
                    changeBackgroundColor()
                    true
                }
                R.id.menu_text_color -> {
                    changeTextColor()
                    true
                }
                R.id.menu_font -> {
                    changeFont()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun updateReadingProgress(chapterIndex: Int, lastPosition: Int) {
        userRef?.apply {
            child("last_chapter_read").setValue(chapterIndex)
            child("last_position").setValue(lastPosition)
        } ?: Log.e("ChapterDetailActivity", "userRef is null. Không thể lưu tiến trình đọc.")
    }

    private fun saveReadingProgress(chapterIndex: Int, lastPosition: Int) {
        userRef?.apply {
            child("last_chapter_read").setValue(chapterIndex)
            child("last_position").setValue(lastPosition)
        } ?: Log.e("ChapterDetailActivity", "userRef is null. Không thể lưu tiến trình đọc.")
    }

    // 1. Change Font Size dialog
    private fun changeFontSize() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Thay đổi kích cỡ chữ")

        val dialogView = layoutInflater.inflate(R.layout.dialog_fontsize, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBarFontSize)
        val tvSizePreview = dialogView.findViewById<android.widget.TextView>(R.id.tvFontSizePreview)

        // Giá trị mặc định
        val currentSize = binding.tvChapterContent.textSize / resources.displayMetrics.scaledDensity
        seekBar.max = 50
        seekBar.progress = currentSize.toInt()
        tvSizePreview.text = "${seekBar.progress}sp"
        tvSizePreview.textSize = seekBar.progress.toFloat()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = if (progress < 10) 10 else progress
                binding.tvChapterContent.textSize = size.toFloat()
                tvSizePreview.text = "${size}sp"
                tvSizePreview.textSize = size.toFloat()
                saveFontSize(size.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        builder.setView(dialogView)
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("Hủy", null)
        builder.show()
    }

    // 2. Change Background Color dialog
    private fun changeBackgroundColor() {
        val colors = arrayOf("Trắng", "Đen", "Sepia")
        val colorValues = arrayOf(
            Color.WHITE,
            Color.BLACK,
            0xFFFAF0E6.toInt() // Sepia
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn màu nền")
        builder.setItems(colors) { _, which ->
            binding.activityChapterDetail.setBackgroundColor(colorValues[which])
            saveBgColor(colorValues[which])
            // Đổi màu chữ cho phù hợp nền nếu muốn
            if (which == 1) binding.tvChapterContent.setTextColor(Color.WHITE)
            else binding.tvChapterContent.setTextColor(Color.BLACK)
        }
        builder.show()
    }

    // 3. Change Text Color dialog
    private fun changeTextColor() {
        val colors = arrayOf("Trắng", "Đen", "Xanh lá cây", "Nâu")
        val colorValues = arrayOf(
            Color.WHITE,
            Color.BLACK,
            Color.parseColor("#388E3C"), // Green
            Color.parseColor("#8D5524")  // Brown
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn màu chữ")
        builder.setItems(colors) { _, which ->
            binding.tvChapterContent.setTextColor(colorValues[which])
            saveTextColor(colorValues[which])
        }
        builder.show()
    }

    // 4. Adjust Brightness dialog
    private fun adjustBrightness() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Điều chỉnh độ sáng")

        val dialogView = layoutInflater.inflate(R.layout.dialog_brightness, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBarBrightness)
        seekBar.max = 100
        val lp = window.attributes
        seekBar.progress = ((lp.screenBrightness.takeIf { it >= 0 } ?: 1.0f) * 100).toInt()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val brightness = progress / 100f
                val params = window.attributes
                params.screenBrightness = brightness
                window.attributes = params
                saveBrightness(brightness)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        builder.setView(dialogView)
        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("Hủy", null)
        builder.show()
    }

    // 5. Change Font dialog
    private fun changeFont() {
        val fonts = arrayOf("Noto Serif", "Roboto", "Comfortaa")
        val fontFiles = arrayOf(
            "font/noto_serif.ttf",
            "font/roboto_regular.ttf",
            "font/comfortaa_regular.ttf"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn font chữ")
        builder.setItems(fonts) { _, which ->
            try {
                val typeface = Typeface.createFromAsset(assets, fontFiles[which])
                binding.tvChapterContent.typeface = typeface
                saveFont(fontFiles[which])
            } catch (e: Exception) {
                Toast.makeText(this, "Không thể đổi font: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }

    // --- SharedPreferences functions ---

    private fun saveFontSize(size: Float) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit() { putFloat(KEY_FONT_SIZE, size) }
    }

    private fun saveBgColor(color: Int) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit() { putInt(KEY_BG_COLOR, color) }
    }

    private fun saveTextColor(color: Int) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit() { putInt(KEY_TEXT_COLOR, color) }
    }

    private fun saveFont(fontFile: String) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit() { putString(KEY_FONT, fontFile) }
    }

    private fun saveBrightness(brightness: Float) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit() { putFloat(KEY_BRIGHTNESS, brightness) }
    }

    private fun restoreSettings() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Font Size
        val fontSize = prefs.getFloat(KEY_FONT_SIZE, 16f)
        binding.tvChapterContent.textSize = fontSize

        // Background color
        val bgColor = prefs.getInt(KEY_BG_COLOR, Color.WHITE)
        binding.activityChapterDetail.setBackgroundColor(bgColor)

        // Text color
        val textColor = prefs.getInt(KEY_TEXT_COLOR, Color.BLACK)
        binding.tvChapterContent.setTextColor(textColor)

        // Font
        val fontFile = prefs.getString(KEY_FONT, "fonts/noto_serif.ttf")
        try {
            val typeface = Typeface.createFromAsset(assets, fontFile!!)
            binding.tvChapterContent.typeface = typeface
        } catch (_: Exception) {}

        // Brightness
        val brightness = prefs.getFloat(KEY_BRIGHTNESS, -1f)
        if (brightness in 0.0f..1.0f) {
            val params = window.attributes
            params.screenBrightness = brightness
            window.attributes = params
        }
    }
}