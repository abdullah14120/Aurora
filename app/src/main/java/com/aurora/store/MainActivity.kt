package com.aurora.store

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.lifecycle.lifecycleScope
import androidx.navigation.FloatingWindow
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.aurora.store.data.model.NetworkStatus
import com.aurora.store.data.receiver.MigrationReceiver
import com.aurora.store.databinding.ActivityMainBinding
import com.aurora.store.util.PackageUtil
import com.aurora.store.util.Preferences
import com.aurora.store.view.ui.sheets.NetworkDialogSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    // جعلنا القائمة تحتوي فقط على شاشة تطبيقاتك والتحديثات
    private val topLevelFrags = listOf(
        R.id.appsContainerFragment,
        R.id.updatesFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // تشغيل عمليات الهجرة إذا لزم الأمر
        MigrationReceiver.runMigrationsIfRequired(this)

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ضبط الحواف لعرض كامل الشاشة (Edge to Edge)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInsets ->
            val insets = windowInsets.getInsets(systemBars() or displayCutout() or ime())
            root.setPadding(insets.left, insets.top, insets.right, 0)
            windowInsets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // مراقبة حالة الشبكة
        if (!PackageUtil.isTv(this)) {
            viewModel.networkProvider.status.onEach { networkStatus ->
                when (networkStatus) {
                    NetworkStatus.AVAILABLE -> {
                        if (!supportFragmentManager.isDestroyed && isIntroDone()) {
                            val fragment = supportFragmentManager
                                .findFragmentByTag(NetworkDialogSheet.TAG)
                            fragment?.let {
                                supportFragmentManager.beginTransaction()
                                    .remove(fragment)
                                    .commitAllowingStateLoss()
                            }
                        }
                    }
                    NetworkStatus.UNAVAILABLE -> {
                        if (!supportFragmentManager.isDestroyed && isIntroDone()) {
                            supportFragmentManager.beginTransaction()
                                .add(NetworkDialogSheet.newInstance(), NetworkDialogSheet.TAG)
                                .commitAllowingStateLoss()
                        }
                    }
                }
            }.launchIn(lifecycleScope) // استخدام نطاق الحياة المناسب
        }

        // إعداد التنقل وإخفاء القائمة السفلية فوراً
        binding.navView.setupWithNavController(navController)
        binding.navView.visibility = View.GONE

        // تحديد شاشة تطبيقات عبد الله كوجهة افتراضية
        val defaultTab = R.id.appsContainerFragment

        // التعامل مع زر العودة لإغلاق التطبيق مباشرة من الصفحة الرئيسية
        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id == defaultTab) {
                finish()
            } else if (navHostFragment.childFragmentManager.backStackEntryCount == 0) {
                finish()
            } else {
                navController.navigateUp()
            }
        }

        // التأكد من أن القائمة السفلية تبقى مخفية في جميع الشاشات
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.navView.visibility = View.GONE
        }

        // تحديث إشعارات التحديثات (Badges) في الخلفية (اختياري طالما القائمة مخفية)
        lifecycleScope.launch {
            viewModel.updateHelper.updates.collectLatest { list ->
                binding.navView.getOrCreateBadge(R.id.updatesFragment).apply {
                    isVisible = !list.isNullOrEmpty()
                    number = list?.size ?: 0
                }
            }
        }
    }

    private fun isIntroDone(): Boolean = Preferences.getBoolean(this, Preferences.PREFERENCE_INTRO)
}
