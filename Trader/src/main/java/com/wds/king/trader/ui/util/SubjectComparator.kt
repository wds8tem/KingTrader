package com.wds.king.trader.ui.util

import com.wds.king.trader.model.InterestSubject

object SubjectComparator {

    private val priceComparator: Comparator<InterestSubject>
        get() {
            return Comparator { a, b ->
                var priceA = if (a.curPrice != "") a.curPrice.toInt() else -1
                var priceB = if (b.curPrice != "") b.curPrice.toInt() else -1

                if (priceA == -1 || priceB == -1) {
                    return@Comparator 0
                }

                if (priceA > priceB)
                    return@Comparator 1
                else if (priceA < priceB)
                    return@Comparator -1
                0
            }
        }

    private val todayVolumeComparator: Comparator<InterestSubject>
        get() {
            return Comparator { a, b ->
                var todayVolumeA = if (a.todayVolume != "") a.todayVolume.toInt() else -1
                var todayVolumeB = if (b.todayVolume != "") b.todayVolume.toInt() else -1

                if (todayVolumeA == -1 || todayVolumeB == -1) {
                    return@Comparator 0
                }

                if (todayVolumeA > todayVolumeB)
                    return@Comparator 1
                else if (todayVolumeA < todayVolumeB)
                    return@Comparator -1
                0
            }
        }

    private val todayDiffPriceComparator: Comparator<InterestSubject>
        get() {
            return Comparator { a, b ->
                var todayDiffPriceA = if (a.todayDiffPrice != "") a.todayDiffPrice.toInt() else -1
                var todayDiffPriceB = if (b.todayDiffPrice != "") b.todayDiffPrice.toInt() else -1

                if (todayDiffPriceA == -1 || todayDiffPriceB == -1) {
                    return@Comparator 0
                }

                if (todayDiffPriceA > todayDiffPriceB)
                    return@Comparator 1
                else if (todayDiffPriceA < todayDiffPriceB)
                    return@Comparator -1
                0
            }
        }

    private val todayDiffRatioComparator: Comparator<InterestSubject>
        get() {
            return Comparator { a, b ->
                var todayDiffRatioA = if (a.todayDiffRatio != "") a.todayDiffRatio.toFloat() else -1.0F
                var todayDiffRatioB = if (b.todayDiffRatio != "") b.todayDiffRatio.toFloat() else -1.0F

                if (todayDiffRatioA > todayDiffRatioB)
                    return@Comparator 1
                else if (todayDiffRatioA < todayDiffRatioB)
                    return@Comparator -1
                0
            }
        }

    val sequenceComparator: Comparator<InterestSubject>
        get() {
            return Comparator { a, b ->
                var posA = a.pos
                var posB = b.pos

                if (posA == -1 || posB == -1) {
                    return@Comparator 0
                }

                if (posA > posB)
                    return@Comparator 1
                else if (posA < posB)
                    return@Comparator -1
                0
            }
        }

    fun getComparator(sort: Int): Comparator<InterestSubject> {

        return when (sort) {
            1 -> todayDiffRatioComparator
            2 -> todayDiffPriceComparator
            3 -> todayVolumeComparator
            5 -> priceComparator
            else -> priceComparator
        }
    }
}
