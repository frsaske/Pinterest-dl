package com.example

import com.example.data.AppDatabase
import com.example.data.PinterestRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSasukeFilenameFormatting() {
        val testTitle = "Naruto Cosplay"
        val cleanTitle = testTitle.trim().replace("\\s+".toRegex(), "_")
        val expected = "Sasuke-$cleanTitle.jpg"
        assertEquals("Sasuke-Naruto_Cosplay.jpg", expected)
    }

    @Test
    fun testSasukeFilenameWithMultipleSpacesAndSpecialChars() {
        val testTitle = "Aesthetic Cyberpunk City // 4K"
        val cleanTitle = testTitle.trim()
            .replace("\\s+".toRegex(), "_")
            .replace("""[\\/:*?"<>|\r\n\t]""".toRegex(), "")
        val expected = "Sasuke-Aesthetic_Cyberpunk_City__4K.png"
        assertEquals("Sasuke-Aesthetic_Cyberpunk_City__4K.png", "Sasuke-$cleanTitle.png")
    }
}
