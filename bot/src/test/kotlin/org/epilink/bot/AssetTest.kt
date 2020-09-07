package org.epilink.bot

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.epilink.bot.config.ResourceAssetConfig
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.*

class AssetTest {
    @Test
    fun `Test load asset url`() {
        val asset = ResourceAssetConfig(url = "This is a URL")
        val realAsset = runBlocking { loadAsset(asset, "TestAsset", Paths.get("")) }
        assertTrue(realAsset is ResourceAsset.Url)
        assertEquals("This is a URL", realAsset.url)
    }

    @Test
    fun `Test load asset file`() {
        // Temporary file
        val tmpFile = createTempFile("tmp_el_test")
        tmpFile.writeBytes(byteArrayOf(1, 2, 3))
        val asset = ResourceAssetConfig(file = tmpFile.toPath().toAbsolutePath().toString(), contentType = "text/plain")
        val realAsset = runBlocking { loadAsset(asset, "thing", Paths.get("")) }
        assertTrue(realAsset is ResourceAsset.File)
        assertEquals(ContentType.Text.Plain, realAsset.contentType)
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(realAsset.contents))
    }

    @Test
    fun `Test load nothing`() {
        val a = runBlocking { loadAsset(ResourceAssetConfig(), "E", Paths.get("")) }
        assertEquals(ResourceAsset.None, a)
    }
}