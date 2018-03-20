package tw.inspect.obfuscatedpreferences

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ObfuscatedPreferencesInstrumentedTest {

    private val appContext = InstrumentationRegistry.getTargetContext()
    private val obfuscatedPreferences = ObfuscatedPreferences(appContext, "test", Context.MODE_PRIVATE)
    private val key = "KEY"

    @Test
    fun testInt() {
        val expect = 0
        val defValue = -1
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getInt(key, defValue))

        obfuscatedPreferences.edit().putInt(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getInt(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }


    @Test
    fun testBoolean() {
        val expect = true
        val defValue = false
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getBoolean(key, defValue))

        obfuscatedPreferences.edit().putBoolean(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getBoolean(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }


    @Test
    fun testLong() {
        val expect = 0L
        val defValue = -1L
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getLong(key, defValue))

        obfuscatedPreferences.edit().putLong(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getLong(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testFloat() {
        val expect = 0f
        val defValue = -1f
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getFloat(key, defValue))

        obfuscatedPreferences.edit().putFloat(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getFloat(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testString1() {
        val expect = ""
        val defValue = " "
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getString(key, defValue))

        obfuscatedPreferences.edit().putString(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getString(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testString2() {
        val expect = ""
        val defValue = null
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getString(key, defValue))

        obfuscatedPreferences.edit().putString(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getString(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testString3() {
        val expect = null
        val defValue = ""
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getString(key, defValue))

        obfuscatedPreferences.edit().putString(key, expect).apply()

        //IF EXPECT==NULL, SHOULD RETURN DEFVALUE, BECAUSE PUTSTRING(KEY, NULL) MEANS REMOVE(KEY)
        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValue, obfuscatedPreferences.getString(key, defValue))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testStringSet1() {
        val expect: MutableSet<String?> = mutableSetOf("1", "2", "3")
        val defValues = mutableSetOf<String?>("4", "5", "6")
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValues, obfuscatedPreferences.getStringSet(key, defValues))

        obfuscatedPreferences.edit().putStringSet(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getStringSet(key, defValues))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testStringSet2() {
        val expect: MutableSet<String?> = mutableSetOf("1", "2", "3")
        val defValues = null
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValues, obfuscatedPreferences.getStringSet(key, defValues))

        obfuscatedPreferences.edit().putStringSet(key, expect).apply()

        assertTrue(obfuscatedPreferences.contains(key))
        assertEquals(expect, obfuscatedPreferences.getStringSet(key, defValues))

        obfuscatedPreferences.edit().clear().apply()
    }

    @Test
    fun testStringSet3() {
        val expect = null
        val defValues = mutableSetOf<String?>("4", "5", "6")
        obfuscatedPreferences.edit().clear().apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValues, obfuscatedPreferences.getStringSet(key, defValues))

        obfuscatedPreferences.edit().putStringSet(key, expect).apply()

        assertFalse(obfuscatedPreferences.contains(key))
        assertEquals(defValues, obfuscatedPreferences.getStringSet(key, defValues))

        obfuscatedPreferences.edit().clear().apply()
    }


    @Test
    fun testGetAll() {

        obfuscatedPreferences.edit().clear().apply()



        obfuscatedPreferences.edit()
                .putStringSet("0", mutableSetOf("1", "2", "3", null, "null"))
                .putInt("1", 1)
                .putFloat("2", 2.0f)
                .putLong("3", 3L)
                .putString("4", "4")
                .putBoolean("5", false)
                .apply()

        val map = obfuscatedPreferences.all
        assertTrue(map["0"] is MutableSet<*> && map["0"]!! == mutableSetOf("1", "2", "3", null, "null"))
        assertTrue(map["1"] is Int && map["1"] == 1)
        assertTrue(map["2"] is Float && map["2"] == 2.0f)
        assertTrue(map["3"] is Long && map["3"] == 3L)
        assertTrue(map["4"] is String && map["4"] == "4")
        assertTrue(map["5"] is Boolean && map["5"] == false)


        //val a: Any = Gson().fromJson(Gson().toJson(object : TypeToken<String>(){}), Any::class.java)
        //Log.e("test", a.toString())
        //Log.e("test", Gson().toJson(object : TypeToken<Set<Map<Int, String>>>(){}.rawType))


    }



}
