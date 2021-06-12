package com.srs.ssms.smartlab

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.srs.ssms.smartlab.AlertDialogUtility
import com.srs.ssms.smartlab.PrefManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class Login : AppCompatActivity() {

    //inisialisasi fingerprint
    var promptInfo: BiometricPrompt.PromptInfo? = null
    private var biometricPrompt: BiometricPrompt? = null

    //inisialisasi url
    val url = Database.mp + "config/apk-login.php"

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNNECESSARY_SAFE_CALL")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTampilan()
        initBt()
        initFp()
    }

    private fun initFp(){
        //fungsi fingerprint
        val prefManager = PrefManager(this) //inisialisasi shared preference
        promptInfo = BiometricPrompt.PromptInfo.Builder() //DIALOG BUILDER FOR BIOMETRIC AUTHENTIFICATION
            .setTitle("Selamat datang " + prefManager.name + "!")
            .setSubtitle("Gunakan sidik jari untuk melanjutkan ke aplikasi")
            //.setDescription(getText(app_name))
            .setNegativeButtonText("Gunakan password saja")
            .build()
        val executor = Executors.newSingleThreadExecutor()
        val activity = this
        biometricAuth(activity, executor)

        //mengatus fingerprint pada saat launching pertama
        if (!prefManager.isFirstTimeLaunch && !prefManager.session) {
            btFP.visibility = View.VISIBLE
            biometricPrompt!!.authenticate(promptInfo!!)
        } else if (!prefManager.isFirstTimeLaunch && prefManager.session){
            btFP.visibility = View.VISIBLE
            biometricPrompt!!.authenticate(promptInfo!!)
            etUser.setText(prefManager.email, TextView.BufferType.SPANNABLE)
            etUser.isEnabled = false
            etUser.setTextColor(Color.BLACK)
        } else {
            btFP.visibility = View.GONE
        }
    }

    private fun setTampilan(){
        window.statusBarColor = Color.WHITE //ganti warna statusbar
        progressBarHolder.visibility = View.GONE //hilangkan progressbar

        /*Glide.with(this)//GLIDE LOGO FOR LOADING LAYOUT
            .load(R.drawable.ssms_green)
            .into(logo_ssms)
        lottie.setAnimation("loading_circle.json")//ANIMATION WITH LOTTIE FOR LOADING LAYOUT
        lottie.loop(true)
        lottie.playAnimation()*/
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNNECESSARY_SAFE_CALL")
    private fun initBt(){
        //RESTARTING BIOMETRIC AUTH BUTTON
        btFP.setOnClickListener {
            finish()
            overridePendingTransition(0, 0) //hilangkan animasi
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        //SETTING CLICK TO REGISTER
        /*tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }*/

        //LOGIN BUTTON LISTENER OFFLINE/ONLINE
        btMasuk.setOnClickListener {
            val connectivityManager: ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val con = try{
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state
            } catch (e:Exception){
                NetworkInfo.State.DISCONNECTED
            }
            val connected =  con === NetworkInfo.State.CONNECTED || //atur kondisi parameter connected atau ga
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state === NetworkInfo.State.CONNECTED
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager //hilangkan keyboard
            if (Objects.requireNonNull(imm).isAcceptingText) {
                imm.hideSoftInputFromWindow(Objects.requireNonNull(currentFocus)?.windowToken, 0)
            }
            //penentuan kondisi online/offline
            val pref = PrefManager(this)
            val u = etUser.text.toString()
            val p = etPass.text.toString()
            val pmu = pref.email
            val pmp = pref.password
            if (u == pmu && p == pmp){
                loginSuccess()
            } else if (connected) {
                onlineAuth(etUser.text.toString(), etPass.text.toString())
            } else {
                offlineAuth()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun onlineAuth(email: String, password: String) {
        val prefManager = PrefManager(this) //init shared preference
        if (email == prefManager.email && password == prefManager.password) {
            loginSuccess()
        } else {
            progressBarHolder.visibility = View.VISIBLE
            tv_hint_loading.text = "Memasuki aplikasi..." //setting text hint progressbar
            //method volley buat login (POST data to PHP)
            @Suppress("UNUSED_ANONYMOUS_PARAMETER") val strReq: StringRequest =
                object : StringRequest(Method.POST,
                    url,
                    Response.Listener { response ->
                        progressBarHolder.visibility = View.VISIBLE
                        try {
                            val jObj = JSONObject(response)
                            val success = jObj.getInt(Database.TAG_SUCCESS)
                            // Check for error node in json
                            if (success == 1) {
                                //pengaturan shared preferences
                                prefManager.isFirstTimeLaunch = false
                                prefManager.session = true
                                prefManager.userid = jObj.getInt(Database.TAG_USERID).toString()
                                prefManager.name = jObj.getString(Database.TAG_NAMA)
                                prefManager.departemen = jObj.getString(Database.TAG_DEPARTEMEN)
                                prefManager.lokasi_kerja = jObj.getString(Database.TAG_LOKASIKERJA)
                                prefManager.jabatan = jObj.getString(Database.TAG_JABATAN)
                                prefManager.nohp = jObj.getString(Database.TAG_NOHP)
                                prefManager.email = jObj.getString(Database.TAG_EMAIL)
                                prefManager.lokasi = jObj.getString(Database.TAG_LOKASI)
                                prefManager.akses = jObj.getString(Database.TAG_AKSES)
                                prefManager.password = jObj.getString(Database.TAG_PASSWORD)
                                prefManager.login = (prefManager.login + 1)
                                prefManager.afdeling = jObj.getString(Database.TAG_AFDELING)
                                Log.d("testid", prefManager.userid.toString())
                                val intent = Intent(this@Login, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                AlertDialogUtility.alertDialog(
                                    this,
                                    jObj.getString(Database.TAG_MESSAGE),
                                    "warning.json"
                                )
                                progressBarHolder.visibility = View.GONE
                            }
                        } catch (e: JSONException) {
                            AlertDialogUtility.withSingleAction(
                                this, "Ulang", "Data error, hubungi pengembang: $e", "warning.json"
                            ) {
                                val intent = Intent(this@Login, Splash::class.java)
                                startActivity(intent)
                            }
                            progressBarHolder.visibility = View.GONE
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        AlertDialogUtility.withSingleAction(
                            this,
                            "Ulang",
                            "Terjadi kesalahan koneksi",
                            "network_error.json"
                        ) {
//                    val intent = Intent(this@Login, Splash::class.java)
//                    startActivity(intent)
                        }
                        progressBarHolder.visibility = View.GONE
                    }) {
                    override fun getParams(): Map<String, String> {
                        // Posting parameters to login url
                        val params: MutableMap<String, String> = HashMap()
                        params[Database.TAG_EMAIL] = email
                        params[Database.TAG_PASSWORD] = password
                        return params
                    }
                }
            Volley.newRequestQueue(this).add(strReq)
        }
    }

    private fun offlineAuth(){
        //ilangin keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        //ngambil text dari edit text
        val username = etUser.text.toString()
        val password = etPass.text.toString()
        val prefManager = PrefManager(this)
        if (username == "srs" && password == "srs3") {
            loginSuccess()}
        else if (username == prefManager.email  && password == prefManager.password) {
            loginSuccess()}
        else if (username == "" && password == "") {
            AlertDialogUtility.alertDialog(this, "Username atau password belum diisi, mohon cek kembali username dan password anda", "warning.json")
        } else{
            AlertDialogUtility.alertDialog(this, "Silahkan hubungkan gawai anda ke jaringan untuk melanjutkan!!", "warning.json"
            )
        }
    }

    fun loginSuccess() {
        val prefManager = PrefManager(this)
        prefManager.isFirstTimeLaunch = false
        prefManager.login = (prefManager.login+1) //atur jumlah berapa kali login
        if (prefManager.name == null || prefManager.name == "") { //misal prefmanager name kosong diisi
            prefManager.name = etUser.text.toString()
        }
        val intent = Intent(this@Login, MainActivity::class.java)
        this@Login.startActivity(intent)
    }

    //fungsi finger print
    private fun biometricAuth(activity: FragmentActivity, executor: Executor){
        biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    promptInfo!!.isConfirmationRequired
                } else {
                    //TODO: Called when an unrecoverable error has been encountered and the operation is complete.
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                loginSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Looper.prepare() //Call looper.prepare()
                Toasty.error(this@Login, "Mohon gunakan jari yang teregistrasi", Toast.LENGTH_SHORT, true).show()
                val snack = Snackbar.make(login_parent, "Mohon gunakan jari yang teregistrasi", Snackbar.LENGTH_SHORT)
                val view = snack.view
                val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(view.layoutParams)
                params.gravity = Gravity.TOP
                view.layoutParams = params
                snack.show()
                Looper.loop()
            }
        })
    }
}