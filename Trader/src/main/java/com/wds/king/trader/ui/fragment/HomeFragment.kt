package com.wds.king.trader.ui.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AlphaAnimation
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.itgen.corelib.shared.ItemMaster.ItemCode
import com.wds.king.trader.MainActivity
import com.wds.king.trader.R
import com.wds.king.trader.adapter.*
import com.wds.king.trader.callback.InterestGroupEditCB
import com.wds.king.trader.callback.InterestSubjectCB
import com.wds.king.trader.callback.InterestSubjectEditCB
import com.wds.king.trader.callback.OnStartDragListener
import com.wds.king.trader.database.*
import com.wds.king.trader.di.DaggerAppComponent
import com.wds.king.trader.disposables
import com.wds.king.trader.model.*
import com.wds.king.trader.rxextensions.runOnIoScheduler
import com.wds.king.trader.ui.pager.PagerAdapter
import com.wds.king.trader.ui.spinner.InterestGroupSpinner
import com.wds.king.trader.ui.util.SubjectComparator
import com.wds.king.trader.ui.util.optionalOf
import com.wds.king.trader.viewDisposables
import com.yuanta.smartnet.proc.SmartNetMng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import kotlinx.android.synthetic.main.nav_header_1.view.*
import kotlinx.android.synthetic.main.nav_header_2.view.*
import kotlinx.android.synthetic.main.nav_menu_1.*
import kotlinx.android.synthetic.main.nav_menu_1.view.*
import kotlinx.android.synthetic.main.nav_menu_2.*
import kotlinx.android.synthetic.main.nav_menu_2.view.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

const val HOME_TAG = "HomeFragment"

lateinit var myProfitDaoEx: MyProfitDao

class HomeFragment : BaseFragment(), InterestGroupSpinner.OnItemCodeSelectedListener,
        ViewPager.OnPageChangeListener, NavigationView.OnNavigationItemSelectedListener, OnStartDragListener {

    val UI_HANDLE_REMOVE_INTER_SUB_ITEM = 0
    val UI_HANDLE_SORT_ITEM_LIST = 1
    val UI_HANDLE_ADD_GROUP = 2

    val INIT_TOKEN = "init_token"

    @Inject
    lateinit var interSubDao: InterestedSubjectDao
    @Inject
    lateinit var interGroupDao: InterestedGroupDao
    @Inject
    lateinit var myProfitDao: MyProfitDao

    @Inject
    lateinit var viewModelFactory: SignViewModelFactory

    lateinit var viewModel: SignViewModel

    private var rootView: View? = null
    private var curNavIdx: Int = 0

    private var interSubAdapter: InterestSubjectAddAdapter? = null
    private var interSubEditAdapter: InterestSubjectEditAdapter? = null
    private var interGroupEditAdapter: InterestGroupEditAdapter? = null
    private var pagerAdapter: PagerAdapter? = null
    private var interSubEditTouchHelper: ItemTouchHelper? = null
    private var interGroupEditTouchHelper: ItemTouchHelper? = null

    private var interSubEditCommitList: ArrayList<InterestSubject> = arrayListOf()
    private var interGroupEditCommitList: ArrayList<InterestGroup> = arrayListOf()

    var interGroupSpinner: InterestGroupSpinner? = null

    private var itemCodeList: ArrayList<ItemCode> = SmartNetMng.getInstance().sortedStockCodeList
    private var init = false
    private var groupInit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onActivityCreated(savedInstanceState)

        if (rootView == null) {

            container!!.removeAllViews()
            rootView = inflater.inflate(R.layout.home_fragment, container, false) ?: return null

            DaggerAppComponent.builder().application(activity!!.application).build().inject(this)

            myProfitDaoEx = myProfitDao

            viewModel = ViewModelProviders.of(this, viewModelFactory)[SignViewModel::class.java]

            disposables?.add(viewModel.loadAccessToken())

            viewDisposables?.add(viewModel.accessToken
                    .filter { it.isEmpty }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        // make default folder
                        for (i in 0 until 3) {
                            makeNewGroup("Group $i")
                        }
                    })

            viewModel.requestAccessToken(INIT_TOKEN)

            lifecycle.addObserver(disposables!!)
            lifecycle.addObserver(viewDisposables!!)

            rootView!!.home_drawer_layout.closeDrawer(Gravity.RIGHT)
            (activity as MainActivity).setSupportActionBar(home_toolbar)
            (activity as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

            initInterSubHeader(rootView!!)
            initHomeMenu(rootView!!)
            initInterSubMenu1(rootView!!)
            initInterSubMenu2(rootView!!)

            val runnable = Runnable {
                myProfitDao.get().map {
                    optionalOf(it).apply {
                        if (isEmpty) {
                            value.map {
                                Log.d("wang", "groupId : ${it.groupId} code : ${it.code} curPrice : ${it.curPrice}")
                            }
                        } else {
                            Log.d("wang", "No myProfitDao Data..")
                        }
                    }
                }
            }
            val thread = Thread(runnable)
            thread.start()
        } else {
            val parentViewGroup = rootView!!.parent as ViewGroup?
            parentViewGroup?.removeView(rootView)

            rootView!!.home_tab_layout.visibility = GONE
            rootView!!.home_tab_layout.visibility = VISIBLE
        }
        return rootView
    }

    private fun initInterSubMenu1(rootView: View) {

        rootView.apply {

            edit_inter_group_tab_layout.let {
                it.addTab(it.newTab().setText(activity!!.getString(R.string.inter_group_inter_sub_edit)))
                it.addTab(it.newTab().setText(activity!!.getString(R.string.inter_group_edit)))
                it.tabGravity = TabLayout.GRAVITY_FILL

                it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) {

                        tab?.let {
                            when (it.position) {
                                0 -> {
                                    inter_group_sub_layout_1.visibility = VISIBLE
                                    inter_group_sub_layout_2.visibility = GONE
                                }
                                1 -> {
                                    inter_group_sub_layout_1.visibility = GONE
                                    inter_group_sub_layout_2.visibility = VISIBLE
                                }
                            }
                        }
                    }
                })
            }

            interSubEditAdapter = InterestSubjectEditAdapter(object : InterestSubjectEditCB {
                override fun onItemViewClicked(isVisible: Boolean) {
                    if (isVisible)
                        edit_inter_sub_move_item_layout.visibility = VISIBLE
                    else
                        edit_inter_sub_move_item_layout.visibility = GONE
                }

                override fun onGotoAddSubject() {
                    gotoAddInterSubMenu()
                }

                override fun onGroupChanged() {
                    groupChanged()
                }
            },
                    if (pagerAdapter!!.fragmentList.size > 0) pagerAdapter!!.fragmentList[0].group else InterestGroup()
                    , this@HomeFragment)

            edit_inter_sub_move_item_down.setOnClickListener {
                interSubEditAdapter?.swapItemsDown()

            }

            edit_inter_sub_move_item_up.setOnClickListener {
                interSubEditAdapter?.swapItemsUp()
            }

            edit_inter_sub_rv.adapter = interSubEditAdapter

            interSubEditTouchHelper = ItemTouchHelper(ItemTouchHelperCB(interSubEditAdapter!!)).apply {
                attachToRecyclerView(rootView.edit_inter_sub_rv)
            }

            inter_sub_edit_select_all.setOnClickListener {
                selectEditableInterSub()
            }

            inter_sub_edit_delete.setOnClickListener {
                deleteEditableInterSub()
            }

            inter_sub_edit_move.setOnClickListener {

                moveEditableInterSub()

            }

            inter_sub_edit_complete.setOnClickListener {

                rootView.home_toolbar!!.addView(interGroupSpinner!!)
                interSubEditComplete()
                openCloseDrawer(curNavIdx)
            }

            interGroupEditAdapter = InterestGroupEditAdapter(activity!!, object : InterestGroupEditCB {

                override fun onItemViewClicked(isVisible: kotlin.Boolean) {
                    if (isVisible)
                        edit_inter_group_move_item_layout.visibility = VISIBLE
                    else
                        edit_inter_group_move_item_layout.visibility = GONE
                }

                override fun onAddGroup() {
                    addGroup()
                }
            }, this@HomeFragment)

            edit_inter_group_move_item_down.setOnClickListener {
                interGroupEditAdapter?.swapItemsDown()
            }

            edit_inter_group_move_item_up.setOnClickListener {
                interGroupEditAdapter?.swapItemsUp()
            }

            inter_group_edit_select_all.setOnClickListener {
                selectEditableAllGroup()
            }

            inter_group_edit_delete.setOnClickListener {
                deleteEditableSelGroup()
            }

            inter_group_edit_complete.setOnClickListener {
                rootView.home_toolbar!!.addView(interGroupSpinner!!)
                interGroupEditComplete()
                openCloseDrawer(curNavIdx)
            }

            rootView.edit_inter_group_rv.adapter = interGroupEditAdapter

            interGroupEditTouchHelper = ItemTouchHelper(ItemTouchHelperCB(interGroupEditAdapter!!))
            interGroupEditTouchHelper!!.attachToRecyclerView(rootView.edit_inter_group_rv)
        }
    }

    private fun View.gotoAddInterSubMenu() {

        if (needToAddGroup()) {
            home_toolbar.addView(interGroupSpinner!!)

            nav_header_1.visibility = GONE
            nav_menu_1.visibility = GONE

            curNavIdx = 1
            add_inter_sub_sel_items_layout.removeAllViews()
            openCloseSelLayout()
            interSubAdapter?.initAllItem()

            home_toolbar.removeView(interGroupSpinner!!)
            nav_header_1.visibility = GONE
            nav_header_2.visibility = VISIBLE
            nav_menu_2.visibility = VISIBLE
            home_tab_layout.visibility = GONE
            pager.visibility = GONE
            home_drawer_layout.openDrawer(Gravity.RIGHT)
            home_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
        }
    }

    private fun groupChanged() {

        val nameList: ArrayList<String> = arrayListOf()

        pagerAdapter!!.fragmentList.map {
            nameList.add(it.group.name)
        }

        with(pagerAdapter)
        {

            this!!.fragmentList!!.forEachIndexed { i, s ->

                if (s.group.groupId == groupId) {
                    val emptyLen = 48 - (s.group.name.length*2)
                    nameList[i] += String.format("%${emptyLen}s%10s", activity!!.getString(R.string.current_selected_group),
                            "${s.homeInterSubAdapter.itemList.size} / ${s.homeInterSubAdapter.MAX_ITEM}")
                } else {
                    val emptyLen = 68 - (s.group.name.length*2)
                    nameList[i] += String.format("%${emptyLen}s", "${s.homeInterSubAdapter.itemList.size} / ${s.homeInterSubAdapter.MAX_ITEM}")
                }
            }

            MaterialDialog.Builder(activity!!)
                    .title(activity!!.getString(R.string.select_interested_group))
                    .items(nameList)
                    .neutralText(activity!!.getString(R.string.cancel))
                    .itemsCallback { dialog, view, which, text ->
                        groupId = pagerAdapter!!.fragmentList[which].group.groupId
                        interSubEditAdapter!!.setDataList(fragmentList[which].group.name, pagerAdapter!!.fragmentList[which].homeInterSubAdapter.editableItemList)
                    }
                    .show()
        }
    }

    private fun selectEditableInterSub() {

        interSubEditAdapter?.apply {
            if (itemList.size > 0)
                allSelected = !allSelected
            lockTouchSwap = allSelected
            if (allSelected) {
                edit_inter_sub_move_item_layout.visibility = VISIBLE
                selectAllItem()
            } else {
                edit_inter_sub_move_item_layout.visibility = GONE
                cancelAllSelection()
            }
        }
    }

    private fun deleteEditableInterSub() {

        interSubEditAdapter?.apply {
            itemList.filter {
                it.isChecked
            }.map {
                it.from = it.groupId
                it.to = -1

                interSubEditCommitList.add(it)
            }
        }
        interSubEditAdapter?.removeItem()
        edit_inter_sub_move_item_layout.visibility = GONE
    }

    private fun moveEditableInterSub() {

        interSubEditAdapter?.apply {
            if (isAtLeastOnSelection()) {
                val nameList: ArrayList<String> = arrayListOf()
                pagerAdapter!!.fragmentList.map {
                    nameList.add(it.group.name)
                }

                MaterialDialog.Builder(activity!!)
                        .items(nameList)
                        .itemsCallback { dialog, view, which, text ->

                            val curIdx = pagerAdapter!!.curIndex
                            if (which != curIdx) {
                                itemList.filter {
                                    it.isChecked
                                }.map {

                                    it.isChecked = false
                                    itemList.remove(it)

                                    it.from = interGroupEditAdapter!!.itemList[curIdx].group.groupId
                                    it.to = interGroupEditAdapter!!.itemList[which].group.groupId
                                    it.fragIdxFrom = curIdx
                                    it.fragIdxTo = which
                                    interSubEditCommitList.add(it)
                                }
                                notifyDataSetChanged()

                                if (isCheckedWithinItems())
                                    edit_inter_sub_move_item_layout.visibility = GONE
                            }
                            cancelAllSelection()
                        }
                        .show()
            }
        }
    }

    private fun deleteEditableSelGroup() {

        interGroupEditAdapter?.apply {
            itemList.filter {
                it.group.isChecked
            }.map {
                val groupId = it.group.groupId
                it.group.removal = true

                interGroupEditCommitList.add(it.group)
                interSubEditAdapter?.removeItem()

                edit_inter_group_move_item_layout.visibility = GONE
            }
            removeItem()
        }
    }

    private fun selectEditableAllGroup() {

        interGroupEditAdapter?.apply {
            if (itemList.size > 0)
                allSelected = !allSelected
            lockTouchSwap = allSelected
            if (allSelected) {
                edit_inter_group_move_item_layout.visibility = VISIBLE
                selectAllItem()
            } else {
                edit_inter_group_move_item_layout.visibility = GONE
                cancelAllSelection()
            }
        }
    }

    private fun addGroup() {

        MaterialDialog.Builder(activity!!)
                .customView(R.layout.interested_group_add_dlg, true)
                .positiveText(R.string.check)
                .negativeText(R.string.cancel)
                .onPositive { dialog1, which ->

                    val groupName: EditText = dialog1.customView!!
                            .findViewById(R.id.inter_group_add_dlg_input)

                    makeNewGroup(groupName.text.toString())
                }
                .show()
    }

    private fun makeNewGroup(groupName: String) {

        val interestedGroupRepo = InterestedGroupRepo().apply {
            name = groupName
        }

        disposables?.add(runOnIoScheduler
        {
            interGroupDao.add(interestedGroupRepo)

            val interestedGroup = InterestGroup().apply {
                name = groupName
                groupId = interGroupDao.getItem().groupId ?: -1
            }

            val tabFragment = TabFragment()

            val bundle = Bundle()
            bundle.putSerializable("main", activity!! as MainActivity)

            tabFragment.arguments = bundle
            tabFragment.group = interestedGroup

            val msg = Message()
            msg.what = UI_HANDLE_ADD_GROUP
            msg.obj = tabFragment
            handler.sendMessage(msg)
        }
        )
    }

    private fun interGroupEditComplete() {

        disposables?.add(runOnIoScheduler {

            interGroupEditCommitList.map {
                if (it.removal) {

                    val gid = it.groupId

                    val interGroupRepo = InterestedGroupRepo().apply {
                        groupId = gid
                    }

                    pagerAdapter!!.fragmentList.filter {
                        it.group.groupId == gid
                    }.map {
                        it.homeInterSubAdapter.itemList.map {
                            removeInterSubEditItem(it)
                        }
                    }

                    interGroupDao.delete(interGroupRepo)
                    //interSubDao.deleteItem(it.groupId)
                }
            }
        })

        pagerAdapter?.apply {
            interGroupEditCommitList.map {
                val groupId = it.groupId
                fragmentList.find {
                    it.group.groupId == groupId
                }?.let {
                    it.homeInterSubAdapter.clearAllItemList()
                    fragmentList.remove(it)
                }
            }
            notifyDataSetChanged()
            interGroupSpinner?.notifyDataSetChanged()
        }
    }

    private fun interSubEditComplete() {

        disposables?.add(runOnIoScheduler {

            interSubEditCommitList.map {

                removeInterSubEditItem(it)

                if (it.to != -1) {
                    val interSubRepo = InterestedSubjectRepo()
                    with(interSubRepo)
                    {
                        groupId = it.to
                        code = it.code
                        name = it.name
                        curPrice = it.curPrice
                        myAvgPrice = it.myAvgPrice
                        myDiffPrice = it.myDiffPrice
                        myVolume = it.myVolume
                        todayDiffPrice = it.todayDiffPrice
                        todayVolume = it.todayVolume
                        todayDiffRatio = it.todayDiffRatio
                    }
                    interSubDao.add(interSubRepo)

                    pagerAdapter?.apply {
                        if (it.fragIdxTo >= 0 && it.fragIdxTo < fragmentList.size) {

                            val interSubRepo = InterestedSubjectRepo().apply {

                                groupId = it.groupId
                                code = it.code
                                name = it.name
                                marketTypeCode = it.marketTypeCode
                                curPrice = it.curPrice
                                myAvgPrice = it.myAvgPrice
                                myDiffPrice = it.myDiffPrice
                                myVolume = it.myVolume
                                todayDiffPrice = it.todayDiffPrice
                                todayVolume = it.todayVolume
                                todayDiffRatio = it.todayDiffRatio
                            }

                            fragmentList[it.fragIdxTo].homeInterSubAdapter.addUnique(it)
                            interSubDao.replace(interSubRepo)
                            val msg = Message().apply {
                                what = UI_HANDLE_SORT_ITEM_LIST
                                arg1 = it.fragIdxTo
                            }
                            handler.sendMessage(msg)
                        }
                    }
                }
            }
        })
    }

    private fun removeInterSubEditItem(it: InterestSubject) {

        if (it.from != -1) {
            val interSubRepo = InterestedSubjectRepo().apply {
                groupId = it.from
                code = it.code
            }
            interSubDao.delete(interSubRepo)

            val myProfitRepo = MyProfitRepo().apply {
                groupId = it.from
                code = it.code
            }

            myProfitDao.delete(myProfitRepo)

            val msg = Message().apply {
                what = UI_HANDLE_REMOVE_INTER_SUB_ITEM
                arg1 = pagerAdapter!!.curIndex
                obj = it
            }
            it.clearInstance()
            handler.sendMessage(msg)
        }
    }

    private fun initInterSubMenu2(rootView: View) {

        val interSubList: ArrayList<InterestSubject> = arrayListOf()

        for (itemCode in itemCodeList) {
            val interestSubject = InterestSubject().apply {
                name = itemCode.divName
                code = itemCode.code
                marketTypeCode = itemCode.marketTypeCode
            }
            interSubList.add(interestSubject)
        }

        interSubAdapter = InterestSubjectAddAdapter(activity as Context, object : InterestSubjectCB {
            override fun onViewClicked(selectedItem: LinearLayout, isSelected: Boolean) {

                if (isSelected) {
                    add_inter_sub_sel_items_layout.addView(selectedItem)
                } else {
                    add_inter_sub_sel_items_layout.removeView(selectedItem)
                }

                openCloseSelLayout()

            }
        }, interSubList)

        rootView.add_inter_sub_search.run {
            searchInterSub()
        }

        val selectedItemTypes = activity?.resources!!.getStringArray(R.array.select_item_type)

        val interSubAddTypeAdapter = InterestSubPosSpiAdapter(activity!!, selectedItemTypes)
        rootView.add_inter_sub_sel_spi.adapter = interSubAddTypeAdapter
        interSubAdapter!!.initialiseUI()
    }

    private fun SearchView.searchInterSub() {

        queryHint = activity?.getString(R.string.search_comment)
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                add_inter_sub_search.clearFocus()
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {

                val text = s.toLowerCase(Locale.getDefault())
                interSubAdapter?.filter(text)

                return false
            }
        })
        setIconifiedByDefault(false)
    }

    private fun openCloseSelLayout() {

        if (add_inter_sub_sel_items_layout.childCount > 0) {
            add_inter_sub_sel_layout.visibility = VISIBLE
            if (add_inter_sub_sel_layout.childCount == 1) {
                val animation = AlphaAnimation(0f, 1f)
                animation.duration = 1000
                add_inter_sub_sel_layout.animation = animation
            }
        } else {
            val animation = AlphaAnimation(0f, 1f)
            animation.duration = 1000
            add_inter_sub_sel_layout.animation = animation
            add_inter_sub_sel_layout.visibility = GONE
        }
    }

    private fun initHomeMenu(rootView: View) {

        rootView.apply {
            pagerAdapter = PagerAdapter(activity!!.supportFragmentManager)

            interGroupSpinner = InterestGroupSpinner(activity!!)
            interGroupSpinner?.let {
                it.setOnItemCodeSelectedListener(this@HomeFragment)
                it.setGroupList(pagerAdapter!!.fragmentList)
                home_toolbar!!.addView(it)
            }

            pager?.apply {
                adapter = pagerAdapter
                addOnPageChangeListener(this@HomeFragment)
            }

            home_edit_subject.setOnClickListener {
                gotoEditInterestedSubject(this)
            }

            home_add_subject.setOnClickListener {
                if (needToAddGroup()) {
                    gotoAddInterestedSubject(this)
                }
            }

            home_sort_subject.setOnClickListener {
                if (needToAddGroup()) {
                    sortInterestedSubject(this)
                }
            }
        }

        bindRxInterestedGroup()
        bindRxInterestedSubject()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (rootView != null) {
            val parentViewGroup = rootView!!.parent as ViewGroup
            parentViewGroup.removeView(rootView)
        }
    }

    private fun needToAddGroup(): Boolean {
        pagerAdapter?.apply {

            if (fragmentList.size == 0) {
                Toast.makeText(activity!!, getString(R.string.need_to_add_group), Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun bindRxInterestedSubject() {

        interSubDao.get().map {
            optionalOf(it)
        }
                .filter {
                    !init
                }
                .doOnNext { optional ->
                    if (optional.value.isEmpty()) {
                        Log.d("wang", "no interSubDao data ..")
                    } else {
                        Log.d("wang", "interSubDao data exist .. ${optional.value.size}")
                    }
                    init = true
                }.doOnError {
                    Log.d("wang", "Err : ${it.message}")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    if (!items.isEmpty) {

                        items.value.filter {
                            it.groupId > -1
                        }.map {
                            val repo = it

                            pagerAdapter?.apply {

                                val adapter = getHomeFragAdapter(repo.groupId)
                                adapter?.let {

                                    adapter.addData(activity!!, InterestSubject(
                                            repo.name,
                                            repo.code,
                                            repo.marketTypeCode,
                                            repo.groupId,
                                            repo.pos,
                                            repo.curPrice,
                                            repo.todayDiffPrice,
                                            repo.myDiffRatio,
                                            repo.myDiffPrice,
                                            repo.todayVolume,
                                            repo.todayDiffRatio,
                                            repo.myVolume,
                                            repo.myAvgPrice
                                    ))
                                    adapter.sort()
                                }
                            }
                        }
                    }
                }

        myProfitDao.get().map {
            optionalOf(it)
        }.doOnNext { optional ->
            if (optional.value.isEmpty()) {
                Log.d("wang", "no myProfitDao data ..")
            } else {
                Log.d("wang", "myProfitDao data exist .. ${optional.value.size}")
            }
        }.doOnError {
            Log.d("wang", "Err : ${it.message}")
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    if (!items.isEmpty) {

                        items.value.groupBy { it.groupId }.values.map {

                            updateMyProfit(it)
                        }
                    } else {
                        Log.d("wang", "items.value is empty ..")
                    }
                }
    }

    private fun updateMyProfit(it: List<MyProfitRepo>): PagerAdapter? {

        var myTotalPrice = 0
        var curTotalPrice = 0

        it.map {
            myTotalPrice += it.myPrice * it.volume
            curTotalPrice += it.curPrice * it.volume
        }

        val temp = (1 - (myTotalPrice / curTotalPrice.toFloat())) * 100
        val profitRatio = (Math.round(temp * 100).toFloat() / 100)
        val profitPrice = curTotalPrice - myTotalPrice
        val myProfit = MyProfit().apply {
            myProfitsRatio = "$profitRatio %"
            myProfitsPrice = profitPrice.toString()
            myPurchasePrice = myTotalPrice.toString()
            myEstimationPrice = curTotalPrice.toString()
        }

        return pagerAdapter?.apply {
            fragmentList.forEachIndexed { i, s ->

                val curFragment = getItem(i)
                curFragment?.homeInterSubAdapter!!.setHeader(myProfit)
            }
        }
    }

    private fun bindRxInterestedGroup() {

        interGroupDao.get().map {
            optionalOf(it)
        }.filter {
            !groupInit
        }.doOnNext { optional ->
            if (optional.value.isEmpty()) {
                Log.d("wang", "no interGroupDao data ..")
            } else {
                Log.d("wang", "interGroupDao data exist .. ${optional.value.size}")
            }
            groupInit = true
        }.doOnError {
            Log.d("wang", "Err : ${it.message}")
        }.doOnComplete {
            Log.d("wang", "group doOnComplete")
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    if (!items.isEmpty) {
                        items.value.filter {
                            if (it.groupId != null) it.groupId!! > -1 else false
                        }
                                .forEachIndexed { i, repo ->

                                    val tabFragment = TabFragment()
                                    val bundle = Bundle()
                                    bundle.putSerializable("main", activity!! as MainActivity)
                                    tabFragment.arguments = bundle

                                    pagerAdapter?.apply {
                                        fragmentList.add(tabFragment)

                                        val group = InterestGroup().apply {
                                            groupId = repo.groupId!!
                                            name = repo.name
                                            //this.pos = repo.pos
                                            this.pos = i
                                        }
                                        fragmentList.last().group = group
                                        notifyDataSetChanged()
                                        interGroupSpinner?.notifyDataSetChanged()
                                    }
                                }
                    }
                }
    }

    private fun getHomeFragAdapter(groupId: Int): HomeInterestSubjectAdapter? {

        pagerAdapter?.apply {
            fragmentList.find {
                it.group.groupId == groupId
            }?.let {
                val curFragment = getItem(it.group.pos)
                return curFragment?.homeInterSubAdapter
            }

        }
        return null
    }

    private fun sortInterestedSubject(rootView: View) {
        curNavIdx = 2
        MaterialDialog.Builder(activity!!)
                .title(getString(R.string.home_inter_sub_sort_subject))
                .items(R.array.sort_interest_item)
                .itemsCallback { dialog, view, which, text ->

                    val curIdx: Int = rootView.pager!!.currentItem
                    pagerAdapter?.apply {
                        fragmentList[curIdx].homeInterSubAdapter.apply {
                            itemList.sortWith(SubjectComparator.getComparator(which))
                            notifyDataSetChanged()
                        }
                    }
                }
                .show()
    }

    private fun gotoEditInterestedSubject(rootView: View) {

        pagerAdapter?.apply {

            val curIdx: Int = rootView.pager!!.currentItem

            if (curIdx < fragmentList.size) {
                curNavIdx = 0
                rootView.home_toolbar!!.removeView(interGroupSpinner!!)
                openCloseDrawer(curNavIdx)

                rootView.edit_inter_group_tab_layout.getTabAt(0)?.select()

                edit_inter_sub_move_item_layout.visibility = GONE

                interSubEditCommitList.clear()
                interGroupEditCommitList.clear()

                interGroupEditAdapter?.apply {
                    clearAllItem()
                    addDataList(pagerAdapter!!.fragmentList)
                    cancelAllSelection()
                }

                val curFragment = getItem(curIdx)
                curFragment?.homeInterSubAdapter
                        ?.let {
                            fragmentList.map {
                                with(it.homeInterSubAdapter)
                                {
                                    clearEditableItemList()

                                    itemList.map {
                                        it.isChecked = false
                                    }

                                    editableItemList.addAll(itemList)
                                }
                            }

                            groupId = pagerAdapter!!.fragmentList[curIdx].group.groupId
                            curIndex = curIdx
                            interSubEditAdapter!!.setDataList(pagerAdapter!!.fragmentList[curIdx].group.name,
                                    fragmentList[curIdx].homeInterSubAdapter.editableItemList)
                        }

                with(interSubEditAdapter!!)
                {
                    lockTouchSwap = false
                    notifyDataSetChanged()
                }
                home_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
            } else {
                curNavIdx = 0
                rootView.home_toolbar!!.removeView(interGroupSpinner!!)
                openCloseDrawer(curNavIdx)

                edit_inter_sub_move_item_layout.visibility = GONE

                interSubEditCommitList.clear()
                with(interSubEditAdapter!!)
                {
                    lockTouchSwap = false
                    notifyDataSetChanged()
                }
                home_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
            }
        }
    }

    private fun gotoAddInterestedSubject(rootView: View) {

        curNavIdx = 1

        add_inter_sub_sel_items_layout.removeAllViews()
        openCloseSelLayout()
        interSubAdapter?.initAllItem()

        rootView.home_toolbar!!.removeView(interGroupSpinner!!)

        openCloseDrawer(curNavIdx)
        home_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
    }

    private fun initInterSubHeader(rootView: View) {

        rootView.apply {
            edit_inter_group_title.setOnClickListener {
                gotoHomeFromSubMenu(rootView)
            }

            add_inter_sub_title.setOnClickListener {
                add_inter_sub_sel_items_layout.removeAllViews()
                gotoHomeFromSubMenu(rootView)
            }

            add_inter_sub_complete.setOnClickListener { v ->
                addInterSubToInterGroup(rootView)
                add_inter_sub_sel_items_layout.removeAllViews()
                gotoHomeFromSubMenu(rootView)
            }
        }
    }

    private fun addInterSubToInterGroup(rootView: View) {

        pagerAdapter?.apply {

            val curIdx: Int = rootView.pager!!.currentItem

            if (curIdx < fragmentList.size) {
                interSubAdapter?.let {
                    val curFragment = getItem(curIdx)

                    curFragment?.homeInterSubAdapter
                            ?.let {

                                val copiedItemList: ArrayList<InterestSubject> = ArrayList()
                                copiedItemList.addAll(interSubAdapter!!.selItemList)
                                copiedItemList.map {

                                    it.groupId = curFragment.group.groupId
                                }

                                it.addDataList(activity!!, copiedItemList)
                                it.sort()

                                disposables?.add(runOnIoScheduler {

                                    copiedItemList.map {

                                        val interSubRepo = InterestedSubjectRepo().apply {

                                            groupId = curFragment.group.groupId
                                            pos = it.pos
                                            name = it.name
                                            code = it.code
                                            marketTypeCode = it.marketTypeCode
                                            curPrice = it.curPrice
                                            todayDiffPrice = it.todayDiffPrice
                                            myDiffRatio = it.myDiffRatio
                                            myDiffPrice = it.myDiffPrice
                                            todayVolume = it.todayVolume
                                            todayDiffRatio = it.todayDiffRatio
                                            myVolume = it.myVolume
                                            myAvgPrice = it.myAvgPrice
                                        }

                                        interSubDao.add(interSubRepo)
                                    }
                                })
                            }
                }
            }
        }
    }

    private fun gotoHomeFromSubMenu(rootView: View) {

        rootView.home_toolbar!!.addView(interGroupSpinner!!)
        home_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        openCloseDrawer(curNavIdx)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        item.expandActionView()

        return true
    }

    private fun openCloseDrawer(navMenu: Int) {

        rootView?.run {
            if (home_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                home_drawer_layout.closeDrawer(Gravity.RIGHT)
                when (navMenu) {
                    0 -> {
                        nav_header_1.visibility = GONE
                        nav_menu_1.visibility = GONE

                    }
                    1 -> {
                        nav_header_2.visibility = GONE
                        nav_menu_2.visibility = GONE
                    }
                }
                home_tab_layout.visibility = VISIBLE
                pager.visibility = VISIBLE
            } else {

                when (navMenu) {
                    0 -> {
                        nav_header_2.visibility = GONE
                        nav_header_1.visibility = VISIBLE
                        nav_menu_1.visibility = VISIBLE
                    }
                    1 -> {
                        nav_header_1.visibility = GONE
                        nav_header_2.visibility = VISIBLE
                        nav_menu_2.visibility = VISIBLE
                    }
                }
                home_tab_layout.visibility = GONE
                pager.visibility = GONE

                home_drawer_layout.openDrawer(Gravity.RIGHT)
            }
        }
    }

    override fun onItemSelected(idx: Int) {

        pager?.currentItem = if (idx > 0) idx else 0
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(pos: Int) {
        interGroupSpinner?.setSelection(pos)
    }

    private fun RecyclerView.Adapter<InterestSubjectAddAdapter.ViewHolder>.initialiseUI() {

        rootView?.run {
            add_inter_sub_sel_scroll.layoutManager = LinearLayoutManager(activity)
            add_inter_sub_sel_scroll.adapter = this@initialiseUI

            add_inter_sub_sel_scroll.setIndexTextSize(12)
            add_inter_sub_sel_scroll.setIndexBarColor("#33334c")
            add_inter_sub_sel_scroll.setIndexBarCornerRadius(0)
            add_inter_sub_sel_scroll.setIndexBarTransparentValue(0.4.toFloat())
            add_inter_sub_sel_scroll.setIndexbarMargin(0F)
            add_inter_sub_sel_scroll.setIndexbarWidth(40F)
            add_inter_sub_sel_scroll.setPreviewPadding(0)
            add_inter_sub_sel_scroll.setIndexBarTextColor("#FFFFFF")

            add_inter_sub_sel_scroll.setPreviewTextSize(60)
            add_inter_sub_sel_scroll.setPreviewColor("#33334c")
            add_inter_sub_sel_scroll.setPreviewTextColor("#FFFFFF")
            add_inter_sub_sel_scroll.setPreviewTransparentValue(0.6f)

            add_inter_sub_sel_scroll.setIndexBarVisibility(true)
            add_inter_sub_sel_scroll.setIndexbarHighLateTextColor("#33334c")
            add_inter_sub_sel_scroll.setIndexBarHighLateTextVisibility(true)
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        viewHolder.run {
            interSubEditTouchHelper?.startDrag(this)
            interGroupEditTouchHelper?.startDrag(this)
        }
    }

    val handler: Handler = Handler(Handler.Callback { msg ->
        msg?.let {
            when (msg.what) {
                UI_HANDLE_REMOVE_INTER_SUB_ITEM -> {
                    val pos = msg.arg1
                    (msg.obj as InterestSubject).let {
                        val adapter = pagerAdapter!!.fragmentList[pos].homeInterSubAdapter
                        adapter.itemList.remove(it)
                        adapter.notifyDataSetChanged()
                    }
                }
                UI_HANDLE_SORT_ITEM_LIST -> {
                    val pos = msg.arg1
                    pagerAdapter?.fragmentList!![pos].homeInterSubAdapter.sort()
                }
                UI_HANDLE_ADD_GROUP -> {
                    (msg.obj as TabFragment).let {
                        interGroupEditAdapter?.apply {
                            addData(it)
                            notifyDataSetChanged()
                        }

                        pagerAdapter?.apply {
                            fragmentList.add(it)
                            notifyDataSetChanged()
                        }
                        interGroupSpinner?.notifyDataSetChanged()
                    }
                }
                else -> {
                }
            }
        }
        true
    }
    )
}