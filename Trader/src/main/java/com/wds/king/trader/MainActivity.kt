package com.wds.king.trader

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.wds.king.trader.model.InterestSubject
import com.wds.king.trader.rx.AutoClearedDisposable
import com.wds.king.trader.ui.fragment.*
import com.wds.king.trader.ui.util.PermissionChecks
import com.yuanta.smartnet.intrf.ISmartNetInitListener
import com.yuanta.smartnet.intrf.ISmartNetLoginListener
import com.yuanta.smartnet.proc.SmartNetMng
import java.io.Serializable
import java.util.*

var disposables: AutoClearedDisposable? = null
var viewDisposables: AutoClearedDisposable? = null

@SuppressLint("NewApi")
class MainActivity : AppCompatActivity(), ISmartNetInitListener, ISmartNetLoginListener, Serializable {

    var instance: MainActivity? = null

    internal var YUANTA_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)

    internal var strUserID: String? = null

    internal var accountList: ArrayList<String>? = null
    internal var accountNameList: ArrayList<String>? = null
    internal var accountCodeList: ArrayList<String>? = null

    private var dlgProgress: ProgressDialog? = null

    init {
        instance = this
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        disposables = AutoClearedDisposable(this)
        viewDisposables = AutoClearedDisposable(this, alwaysClearOnStop = false)

        accountList = ArrayList()
        accountNameList = ArrayList()
        accountCodeList = ArrayList()
        showIntroView()

        val checker = PermissionChecks(this)

        if (checker.lacksPermissions(*YUANTA_PERMISSION)) {
            Toast.makeText(this, getString(R.string.msg_request_permission), Toast.LENGTH_SHORT).show()
            requestPermissions()

        } else {

            SmartNetMng.initSmartNet(this)
            SmartNetMng.getInstance().setInitListener(this)
            SmartNetMng.getInstance().setLoginListener(this)
        }
    }

    private fun requestPermissions() {

        val arrPermission = arrayOfNulls<String>(3)
        for (i in 0..2) {
            arrPermission[i] = YUANTA_PERMISSION[i]
        }

        this.requestPermissions(arrPermission, 1234)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1234 -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmartNetMng.initSmartNet(this)

                    SmartNetMng.getInstance().setInitListener(this)
                    SmartNetMng.getInstance().setLoginListener(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (accountList != null) {
            accountList!!.clear()
            accountList = null
        }

        if (accountNameList != null) {
            accountNameList!!.clear()
            accountNameList = null
        }

        if (accountNameList != null) {
            accountCodeList!!.clear()
            accountCodeList = null
        }

        instance = null

        SmartNetMng.releaseSmartNet()
    }

    override fun onSessionConnecting() {
        Toast.makeText(this, getString(R.string.msg_session_connection), Toast.LENGTH_SHORT).show()
    }

    override fun onSessionConnected(isSuccess: Boolean, strErrorMsg: String) {

        if (isSuccess == true) {
            Toast.makeText(this, strErrorMsg, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, strErrorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAppVersionState(isDone: Boolean) {
        Toast.makeText(this, getString(R.string.msg_library_version_check), Toast.LENGTH_SHORT).show()
    }

    override fun onMasterDownState(isDone: Boolean) {
        Toast.makeText(this, getString(R.string.msg_master_file_download), Toast.LENGTH_SHORT).show()
    }

    override fun onMasterLoadState(isDone: Boolean) {
        Toast.makeText(this, getString(R.string.msg_master_file_loading), Toast.LENGTH_SHORT).show()
    }

    override fun onInitFinished() {
        showLoginView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequiredRefresh() {
        Toast.makeText(baseContext, getString(R.string.msg_session_reconnection), Toast.LENGTH_SHORT).show()
    }

    override fun onLoginStarted() {
        showProgress(getString(R.string.msg_on_login))
    }

    override fun onLoginResult(isSuccess: Boolean, strErrorMsg: String?) {

        hideProgress()

        if (isSuccess) {
            Toast.makeText(baseContext, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show()

            (supportFragmentManager.findFragmentByTag(LOGIN_TAG) as LoginFragment).run {
                strUserID = userId
                showHomeView()
            }
        } else {
            Toast.makeText(baseContext, strErrorMsg ?: "", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showIntroView() {

        val introFragment = IntroFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, introFragment, INTRO_TAG)
                .addToBackStack(null)
                .commit()
    }

    private fun showLoginView() {

        val loginFragment = LoginFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, loginFragment, LOGIN_TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss()
    }

    private fun showHomeView() {

        val homeFragment = HomeFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, homeFragment, HOME_TAG)
                .addToBackStack(null)
                .commit()
    }

    fun showDataView(interSub : InterestSubject?) {

        val dataFragment = DataFragment()
        val bundle = Bundle()
        bundle.putSerializable("item", interSub)
        bundle.putSerializable("main", this)
        dataFragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, dataFragment, DATA_TAG)
                .addToBackStack(null)
                .commit()
    }

    fun showMyReturnView(interSub: InterestSubject?) {

        val myReturnFragment = MyReturnFragment()
        val bundle = Bundle()
        bundle.putSerializable("item", interSub)

        myReturnFragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, myReturnFragment, MY_RETURN_TAG)
                .addToBackStack(null)
                .commit()
    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 3) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }

    private fun showProgress(strText: String) {

        if (dlgProgress == null) {
            dlgProgress = ProgressDialog(this)

            dlgProgress?.apply {
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                isIndeterminate = true
                setOnDismissListener { dlgProgress = null }
            }
        }

        dlgProgress?.let {
            it.setMessage(strText)
            if (!it.isShowing)
                it.show()
        }
    }

    private fun hideProgress() {

        dlgProgress?.let {
            if (it.isShowing)
                it.dismiss()
            dlgProgress = null
        }
    }
}
