package tw.inspect.obfuscatedpreferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class ObfuscatedPreferences(
        context: Context,
        string: String,
        mode: Int,
        private val aesKey: String = "01234567890123456789012345678901") : SharedPreferences {

    private val sharedPreferences = context.getSharedPreferences(string, mode)


    companion object {
        /**
         * Put Listeners in companion object
         *
         * The mListeners is stored as non-static variable in SharedPreferences, and
         * ContextImpl caches the result of getSharedPreferences() into a static cache.
         *
         * That means, the listeners is listening the same SharedPreferences file's change,
         * and there is going to be only one instance per same SharedPreferences file.
         *
         * */

        private val listeners = mutableMapOf<
                SharedPreferences,
                WeakHashMap<
                        SharedPreferences.OnSharedPreferenceChangeListener,
                        SharedPreferences.OnSharedPreferenceChangeListener>>()
    }

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

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        //synchronizing listeners instead of this is because there might be different
        //ObfuscatedPreferences which are associated with the same SharedPreferences
        synchronized(listeners) {
            listeners[sharedPreferences]?.also {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(it[listener])
                it.remove(listener)
                if (it.isEmpty()) {
                    listeners.remove(sharedPreferences)
                }
            }
        }

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

    override fun getStringSet(key: String?, defValues: MutableSet<String?>?): MutableSet<String?>? =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as MutableSet<String?> }
                    ?: defValues

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        //synchronizing listeners instead of this is because there might be different
        //ObfuscatedPreferences which are associated with the same SharedPreferences
        synchronized(listeners) {

            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                WeakReference<SharedPreferences.OnSharedPreferenceChangeListener>(listener).get()
                        ?.onSharedPreferenceChanged(sharedPreferences, Gson().fromJson(decrypt(aesKey, key)))
            }.also {


                if (listeners[sharedPreferences] == null) {
                    listeners[sharedPreferences] = WeakHashMap()
                }
                if (listeners[sharedPreferences]!!.contains(listener)) {
                    return
                }

                sharedPreferences.registerOnSharedPreferenceChangeListener(it)
                listeners[sharedPreferences]!![listener] = it
            }
        }
    }

    override fun getString(key: String?, defValue: String?) =
            sharedPreferences.getString(encrypt(aesKey, Gson().toJson(key)), null)
                    ?.run { unBox(decrypt(aesKey, this)) as String }
                    ?: defValue


    fun <T : Any> box(value: T) = (value::class.java.name + "0" + Gson().toJson(value))
    private fun unBox(string: String): Any = string.indexOf('0').run {

        Gson().fromJson(string.substring(this + 1, string.length), Class.forName(string.substring(0, this)))

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


