package com.wds.king.trader.ui.fragment

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import com.itgen.corelib.util.ConfigUtil
import com.wds.king.trader.R
import com.wds.king.trader.ui.spinner.CertSelectSpinner
import com.yuanta.smartnet.proc.SmartNetMng
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.android.synthetic.main.login_fragment.view.*

const val LOGIN_TAG = "LoginFragment"

class LoginFragment : BaseFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    var activity: Activity? = null

    private var mStrUserId = ""
    private var mStrUserPwd = ""
    var certDN = ""
    private var mStrAuthPwd = ""
    private var isCertLogin = false
    private var isMotuLogin = false

    protected var rootView: View? = null

    var userId: String
        get() {
            mStrUserId = login_id_editor!!.text.toString()
            return mStrUserId
        }
        set(strUserId) {
            login_id_editor!!.setText(strUserId)
            mStrUserId = strUserId
        }

    var userPwd: String
        get() {
            mStrUserPwd = login_passwd_editor!!.text.toString()
            return mStrUserPwd
        }
        set(strUserPwd) {
            mStrUserPwd = strUserPwd
            login_passwd_editor!!.setText(strUserPwd)
        }

    var certPwd: String
        get() {
            mStrAuthPwd = login_cert_passwd_editor!!.text.toString()
            return mStrAuthPwd
        }
        set(strCertPwd) = login_cert_passwd_editor!!.setText(strCertPwd)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        rootView = inflater?.inflate(R.layout.login_fragment, container, false)

        activity = getActivity()


        rootView?.run {
            val certList = SmartNetMng.getInstance().userCertList

            for (cert in certList)

            login_cert_spinner.setCertList(certList)
            certDN = ConfigUtil.getSessionCertDN()
            login_cert_spinner.setOnCertSelectedListener(object : CertSelectSpinner.OnCertSelectedListener {
                override fun onCertSelected(strDN: String) {
                    certDN = strDN
                    ConfigUtil.setSessionCertDN(strDN)
                }
            })

            setEditText(rootView.login_id_editor!!, true, false)
            setEditText(rootView.login_passwd_editor!!, false, false)
            setEditText(rootView.login_cert_passwd_editor!!, false, false)

            login_using_cert_only_check.setOnCheckedChangeListener(this@LoginFragment)
            login_mock_check.setOnCheckedChangeListener(this@LoginFragment)

            login_start.setOnClickListener(this@LoginFragment)
            login_cancel.setOnClickListener(this@LoginFragment)

            setInputState()
        }

        return rootView
    }

    private fun setInputState() {

        rootView?.run {
            login_id_editor!!.isEnabled = !isCertLogin
            login_passwd_editor!!.isEnabled = !isCertLogin
            login_cert_spinner!!.isEnabled = isCertLogin
            login_cert_passwd_editor!!.isEnabled = true
            login_mock_check!!.isEnabled = !isCertLogin
            isMotuLogin = false

            login_id_editor!!.setTextColor(if (isCertLogin) Color.LTGRAY else Color.BLACK)
            login_passwd_editor!!.setTextColor(if (isCertLogin) Color.LTGRAY else Color.BLACK)
        }
    }

    private fun setMotuInputState() {

        rootView?.run {
            login_id_editor!!.isEnabled = true
            login_passwd_editor!!.isEnabled = true
            login_cert_spinner!!.isEnabled = false
            login_cert_passwd_editor!!.isEnabled = !isMotuLogin
            login_using_cert_only_check!!.isEnabled = !isMotuLogin
            isCertLogin = false
            login_cert_passwd_editor!!.setTextColor(if (isMotuLogin) Color.LTGRAY else Color.BLACK)
        }
    }

    override fun onClick(v: View) {

        if (activity == null) return

        val nCtrlId = v.id

        when (nCtrlId) {
            R.id.login_start -> {
                if ((!isCertLogin || isMotuLogin) && userId.isEmpty()) {
                    Toast.makeText(activity, activity!!.getString(R.string.login_check_id), Toast.LENGTH_SHORT).show()
                    return
                } else if ((!isCertLogin || isMotuLogin) && userPwd.isEmpty()) {
                    Toast.makeText(activity, activity!!.getString(R.string.login_check_password), Toast.LENGTH_SHORT).show()
                    return
                } else if (isCertLogin && login_cert_spinner!!.selectedItem == null) {
                    Toast.makeText(activity, activity!!.getString(R.string.login_check_id), Toast.LENGTH_SHORT).show()
                    return
                } else if (!isMotuLogin && certPwd.isEmpty()) {
                    Toast.makeText(activity, activity!!.getString(R.string.login_check_cert_password), Toast.LENGTH_SHORT).show()
                    return
                }

                if (isCertLogin) {
                    SmartNetMng.getInstance().startLogin(certDN, certPwd)
                } else if (isMotuLogin) {
                    SmartNetMng.getInstance().startMotuLogin(userId, userPwd)
                } else {
                    SmartNetMng.getInstance().startLogin(userId, userPwd, certPwd)

                }
            }
            R.id.login_cancel ->
                activity!!.finish()
        }
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        super.onAttachFragment(childFragment)
    }

    override fun onCheckedChanged(button: CompoundButton, isChecked: Boolean) {

        if (button.id == R.id.login_using_cert_only_check) {
            isCertLogin = isChecked
            setInputState()
        }

        if (button.id == R.id.login_mock_check) {
            isMotuLogin = isChecked
            setMotuInputState()

        }
    }
}