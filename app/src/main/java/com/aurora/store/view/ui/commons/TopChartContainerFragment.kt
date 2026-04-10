package com.aurora.store.view.ui.commons

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.aurora.Constants
import com.aurora.store.R
import com.aurora.store.databinding.FragmentTopChartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopChartContainerFragment : BaseFragment<FragmentTopChartBinding>() {
    companion object {
        @JvmStatic
        fun newInstance(chartType: Int): TopChartContainerFragment =
            TopChartContainerFragment().apply {
                arguments = Bundle().apply {
                    putInt(Constants.TOP_CHART_TYPE, chartType)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. إخفاء أزرار التبديل العلوية (Top Free, Top Paid...) تماماً
        binding.topTabGroup.visibility = View.GONE

        var chartType = 0
        val bundle = arguments
        if (bundle != null) {
            chartType = bundle.getInt(Constants.TOP_CHART_TYPE, 0)
        }

        // 2. إعداد الـ ViewPager ليعرض صفحة واحدة فقط
        binding.pager.adapter =
            ViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, chartType)
        
        // تعطيل السحب اليدوي بين الصفحات لضمان الثبات
        binding.pager.isUserInputEnabled = false
    }

    override fun onDestroyView() {
        binding.pager.adapter = null
        super.onDestroyView()
    }

    internal class ViewPagerAdapter(
        fragment: FragmentManager,
        lifecycle: Lifecycle,
        chartType: Int
    ) : FragmentStateAdapter(fragment, lifecycle) {
        
        // 3. جعل القائمة تحتوي على Fragment واحد فقط لعرض تطبيقاتك
        private val tabFragments: MutableList<TopChartFragment> = mutableListOf(
            TopChartFragment.newInstance(chartType, 0) 
        )

        override fun createFragment(position: Int): Fragment = tabFragments[position]

        override fun getItemCount(): Int = 1 // واجهة واحدة فقط
    }
}
