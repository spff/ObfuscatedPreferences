package tw.inspect.obfuscatedpreferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class ObfuscatedPreferences(
        context: Context,
        string: String,
        mode: Int,
        private val aesKey: String = "01234567890123456789012345678901") : SharedPreferences {

    private val sharedPreferences = context.getSharedPreferences(string, mode)

    private fun encrypt(key: String, data: String): String {

        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        val keyspec = SecretKeySpec(key.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keyspec, IvParameterSpec(
                ByteArray(cipher.blockSize)))
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)

    }

    private fun decrypt(key: String, data: String): String {
        val encrypted1 = Base64.decode(data.toByteArray(), Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CFB/NoPadding")
        val keyspec = SecretKeySpec(key.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, keyspec, IvParameterSpec(
                ByteArray(cipher.blockSize)))
        val original = cipher.doFinal(encrypted1)
        return String(original, Charset.forName("UTF-8"))

    }

    private inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

    override fun contains(key: String?) = sharedPreferences.contains(encrypt(aesKey, Gson().toJson(key)))

    override fun getBoolean(key: String?, defValue: Boolean) =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as Boolean }
                    ?: defValue

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInt(key: String?, defValue: Int) =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as Int }
                    ?: defValue

    override fun getAll(): MutableMap<String, Any> =
            mutableMapOf<String, Any>().apply {
                sharedPreferences.all!!.filter { it.value is String }.forEach {
                    put(Gson().fromJson(decrypt(aesKey, it.key)),
                            unBox(decrypt(aesKey, it.value as String)))
                }
            }

    override fun edit() = Editor()

    override fun getLong(key: String?, defValue: Long) =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as Long }
                    ?: defValue

    override fun getFloat(key: String?, defValue: Float) =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as Float }
                    ?: defValue

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as MutableSet<String> }
                    ?: defValues

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getString(key: String?, defValue: String?) =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as String }
                    ?: defValue


    fun <T : Any> box(value: T) = (value::class.javaObjectType.toString() + "0" + Gson().toJson(value))
    private fun unBox(string: String): Any {
        val i = string.indexOf('0')
        val v = string.substring(i + 1, string.length)

        return when (string.substring(0, i)) {
            Int::class.javaObjectType.toString() -> Gson().fromJson<Int>(v)
            Float::class.javaObjectType.toString() -> Gson().fromJson<Float>(v)
            Boolean::class.javaObjectType.toString() -> Gson().fromJson<Boolean>(v)
            Long::class.javaObjectType.toString() -> Gson().fromJson<Long>(v)
            String::class.javaObjectType.toString() -> Gson().fromJson<String>(v)
            else -> Gson().fromJson<MutableSet<String>>(v)
        }

    }


    inner class Editor : SharedPreferences.Editor {

        private val editor = sharedPreferences.edit()

        override fun clear() = this.apply { editor.clear() }

        override fun putLong(key: String?, value: Long) = this.apply {
            editor.putString(encrypt(aesKey, Gson().toJson(key)), encrypt(aesKey, box(value)))
        }

        override fun putInt(key: String?, value: Int) = this.apply {
            editor.putString(encrypt(aesKey, Gson().toJson(key)), encrypt(aesKey, box(value)))
        }

        override fun remove(key: String?) = this.apply { editor.remove(encrypt(aesKey, Gson().toJson(key))) }

        override fun putBoolean(key: String?, value: Boolean) = this.apply {
            editor.putString(encrypt(aesKey, Gson().toJson(key)), encrypt(aesKey, box(value)))
        }

        override fun putStringSet(key: String?, values: MutableSet<String?>?) = this.also {

            editor.putString(
                    encrypt(aesKey, Gson().toJson(key)),
                    values?.run { encrypt(aesKey, box(values)) }
            )

        }

        override fun commit() = editor.commit()

        override fun putFloat(key: String?, value: Float) = this.apply {
            editor.putString(encrypt(aesKey, Gson().toJson(key)), encrypt(aesKey, box(value)))
        }

        override fun apply() = editor.apply()

        override fun putString(key: String?, value: String?) = this.also {

            editor.putString(
                    encrypt(aesKey, Gson().toJson(key)),
                    value?.run { encrypt(aesKey, box(value)) }
            )

        }

    }
}


