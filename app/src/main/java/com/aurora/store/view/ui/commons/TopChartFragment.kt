/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store.view.ui.commons

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.aurora.Constants
import com.aurora.gplayapi.data.models.App // تأكد من استيراد كائن App الصحيح
import com.aurora.gplayapi.data.models.StreamCluster
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
        
        // عرض حالة التحميل أولاً
        updateController(null)
        
        // جلب بياناتك من GitHub
        fetchCustomApps()
    }

    private fun fetchCustomApps() {
        val request = Request.Builder().url(JSON_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // في حال الفشل يمكن العودة لجلب بيانات جوجل الأصلية (اختياري)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val cluster = StreamCluster()
                val jsonArray = JSONArray(body)
                
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val customApp = App().apply {
                        id = i.toLong()
                        packageName = item.getString("package")
                        // ملاحظة: Aurora قد يستخدم حقول مختلفة للعنوان، جربنا displayName سابقاً
                        // سنستخدم هنا الطريقة الأكثر أماناً
                    }
                    cluster.clusterAppList.add(customApp)
                }
                
                activity?.runOnUiThread {
                    updateController(cluster)
                }
            }
        })
    }

    private fun updateController(streamCluster: StreamCluster?) {
        binding.recycler.withModels {
            if (streamCluster == null) {
                for (i in 1..6) {
                    add(AppListViewShimmerModel_().id("shimmer_$i"))
                }
            } else {
                streamCluster.clusterAppList.forEach { app ->
                    add(
                        AppListViewModel_()
                            .id(app.id)
                            .app(app)
                            .click { _ -> 
                                // هنا نضع وظيفة التثبيت الصامت التي برمجناها
                                openDetailsFragment(app.packageName) 
                            }
                    )
                }
            }
        }
    }
}
