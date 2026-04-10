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
                    Toast.makeText(context, "فشل جلب البيانات", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val myApps = parseJsonToAuroraApps(jsonString)
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
                // إنشاء كائن App مع تمرير قيم افتراضية لتفادي أخطاء الـ Constructor
                val app = App(
                    packageName = item.getString("package"),
                    displayName = item.getString("name"), // Aurora يستخدم displayName بدلاً من title أحياناً
                    iconUrl = item.getString("icon"),
                    versionName = item.getString("version"),
                    downloadUrl = item.getString("download_url")
                )
                auroraApps.add(app)
            }
        } catch (e: Exception) { e.printStackTrace() }
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
        activity?.runOnUiThread {
            Toast.makeText(context, "بدأ التحميل: ${app.displayName}", Toast.LENGTH_LONG).show()
        }

        // إنشاء كائن Download وتعبئته بالبيانات المطلوبة
        val download = Download().apply {
            this.packageName = app.packageName
            this.downloadUrl = app.downloadUrl
            this.name = app.displayName
            this.iconUrl = app.iconUrl
        }

        val intent = Intent(requireContext(), com.aurora.store.data.activity.InstallActivity::class.java).apply {
            putExtra(Constants.PARCEL_DOWNLOAD, download)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
