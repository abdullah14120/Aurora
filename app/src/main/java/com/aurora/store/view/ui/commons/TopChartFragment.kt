package com.aurora.store.view.ui.commons

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.aurora.Constants
import com.aurora.gplayapi.data.models.App
import com.aurora.store.data.model.Download
import com.aurora.store.databinding.FragmentTopContainerBinding
import com.aurora.store.view.epoxy.views.app.AppListViewModel_
import com.aurora.store.view.epoxy.views.shimmer.AppListViewShimmerModel_
import com.aurora.store.viewmodel.topchart.TopChartViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

@AndroidEntryPoint
class TopChartFragment : BaseFragment<FragmentTopContainerBinding>() {

    private val viewModel: TopChartViewModel by activityViewModels()
    private val client = OkHttpClient()
    private val JSON_URL = "https://raw.githubusercontent.com/abdullah14120/Update/main/apps.json"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateController(null)
        loadMyApps()
    }

    private fun loadMyApps() {
        val request = Request.Builder().url(JSON_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    val myApps = parseJsonToAuroraApps(body)
                    activity?.runOnUiThread { updateController(myApps) }
                }
            }
        })
    }

    private fun parseJsonToAuroraApps(json: String): List<App> {
        val auroraApps = mutableListOf<App>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                // استخدام الحزمة فقط في البداية لتجنب تعارض الـ Constructor
                val app = App() 
                app.packageName = item.getString("package")
                app.versionName = item.getString("version")
                // جرب استخدام التسميات العامة
                try { app.title = item.getString("name") } catch (e: Exception) {}
                
                auroraApps.add(app)
            }
        } catch (e: Exception) { }
        return auroraApps
    }

    private fun updateController(apps: List<App>?) {
        binding.recycler.withModels {
            if (apps == null) {
                for (i in 1..8) { add(AppListViewShimmerModel_().id("shimmer_$i")) }
            } else {
                apps.forEach { app ->
                    add(
                        AppListViewModel_()
                            .id(app.packageName)
                            .app(app)
                            .click { _ -> startDirectInstall(app) }
                    )
                }
            }
        }
    }

    private fun startDirectInstall(app: App) {
        // استخدام الطريقة الأكثر أماناً لإنشاء كائن الـ Download
        val download = Download()
        download.packageName = app.packageName
        
        val intent = Intent(requireContext(), com.aurora.store.data.activity.InstallActivity::class.java).apply {
            putExtra(Constants.PARCEL_DOWNLOAD, download)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
