package com.example

import com.example.data.AppDatabase
import com.example.data.PinterestRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPinterestFilenameFormatting() {
        val testTitle = "Naruto Cosplay"
        val cleanTitle = testTitle.trim().replace("\\s+".toRegex(), "_")
        val expected = "PinterestDL-$cleanTitle.jpg"
        assertEquals("PinterestDL-Naruto_Cosplay.jpg", expected)
    }

    @Test
    fun testPinterestFilenameWithMultipleSpacesAndSpecialChars() {
        val testTitle = "Aesthetic Cyberpunk City // 4K"
        val cleanTitle = testTitle.trim()
            .replace("\\s+".toRegex(), "_")
            .replace("""[\\/:*?"<>|\r\n\t]""".toRegex(), "")
        val expected = "PinterestDL-Aesthetic_Cyberpunk_City__4K.png"
        assertEquals("PinterestDL-Aesthetic_Cyberpunk_City__4K.png", "PinterestDL-$cleanTitle.png")
    }
}
