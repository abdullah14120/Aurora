package com.aurora.store.view.ui.apps

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aurora.extensions.navigate
import com.aurora.store.MobileNavigationDirections
import com.aurora.store.R
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.databinding.FragmentAppsGamesBinding
import com.aurora.store.view.ui.commons.BaseFragment
import com.aurora.store.view.ui.commons.TopChartContainerFragment // سنستخدم هذا مؤقتاً لعرض القائمة
import com.aurora.store.viewmodel.apps.AppsContainerViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppsContainerFragment : BaseFragment<FragmentAppsGamesBinding>() {

    private val viewModel: AppsContainerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // إخفاء الـ TabLayout لأننا نريد واجهة واحدة بسيطة
        binding.tabLayout.visibility = View.GONE

        // ضبط زر البحث (FAB)
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchFab) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.searchFab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + resources.getDimensionPixelSize(R.dimen.margin_large)
            }
            WindowInsetsCompat.CONSUMED
        }

        // إعداد شريط العنوان (Toolbar)
        binding.toolbar.apply {
            title = "متجر عبدالله التميمي" // يمكنك تغيير الاسم هنا
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_download_manager -> {
                        requireContext().navigate(Screen.Downloads)
                    }
                    R.id.menu_more -> {
                        findNavController().navigate(
                            MobileNavigationDirections.actionGlobalMoreDialogFragment()
                        )
                    }
                }
                true
            }
        }

        // إعداد الـ ViewPager ليعرض "قائمة واحدة" فقط
        binding.pager.adapter = ViewPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle
        )
        
        // تعطيل السحب الجانبي
        binding.pager.isUserInputEnabled = false

        binding.searchFab.setOnClickListener {
            requireContext().navigate(Screen.Search)
        }
    }

    override fun onDestroyView() {
        binding.pager.adapter = null
        super.onDestroyView()
    }

    // قمنا بتبسيط الـ Adapter ليعرض فقط شاشة واحدة
    internal class ViewPagerAdapter(
        fragment: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragment, lifecycle) {

        // سنعرض شاشة الـ TopCharts حالياً لأنها الأنسب لعرض قائمة تطبيقات
        // لاحقاً سنقوم بتعديل TopChartContainerFragment ليقرأ من ملف الـ JSON الخاص بك
        override fun createFragment(position: Int): Fragment {
            return TopChartContainerFragment.newInstance(0)
        }

        override fun getItemCount(): Int = 1 // واجهة واحدة فقط
    }
}
