package com.aurora.store.view.ui.apps

import android.os.Bundle
import android.view.View
import com.aurora.store.databinding.FragmentAppsContainerBinding
import com.aurora.store.view.ui.commons.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppsContainerFragment : BaseFragment<FragmentAppsContainerBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // أخفينا الـ TabLayout تماماً لضمان عدم حدوث أخطاء في المراجع
        binding.tabLayout.visibility = View.GONE
        
        // هنا نجعل العناوين ثابتة ولا تشير إلى "Downloads" أو "Search" المحذوفة
        binding.toolbar.title = "متجر عبد الله"
    }
}
