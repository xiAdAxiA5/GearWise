package com.example.gearwise.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.gearwise.data.model.ElectronicItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    private const val FILE_NAME = "GearWise_backup.json"
    private const val MIME_TYPE = "application/json"
    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    /** 导出：构建 JSON 字符串 */
    fun toJson(items: List<ElectronicItem>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "GearWise")
        root.put("exportedAt", SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(Date()))

        val jsonItems = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("name", item.name)
                put("category", item.category)
                put("brand", item.brand)
                put("model", item.model)
                put("purchaseDate", item.purchaseDate)
                put("purchasePrice", item.purchasePrice)
                put("accessoryCost", item.accessoryCost)
                put("repairCost", item.repairCost)
                put("isSold", item.isSold)
                put("soldDate", item.soldDate ?: JSONObject.NULL)
                put("soldPrice", item.soldPrice?.let { it } ?: JSONObject.NULL)
                put("notes", item.notes)
            }
            jsonItems.put(obj)
        }
        root.put("items", jsonItems)

        return root.toString(2) // pretty-print with 2-space indent
    }

    /** 导出：通过分享 Intent 发送 JSON 文件 */
    fun exportViaShare(context: Context, items: List<ElectronicItem>) {
        try {
            val json = toJson(items)
            val cacheDir = File(context.cacheDir, "exports")
            cacheDir.mkdirs()
            val file = File(cacheDir, FILE_NAME)
            file.writeText(json)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "GearWise 数据备份")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "导出备份文件")
            )
        } catch (e: Exception) {
            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** 导入：从 URI 读取并解析为设备列表 */
    fun fromUri(context: Context, uri: Uri): List<ElectronicItem>? {
        return try {
            val reader = BufferedReader(
                InputStreamReader(context.contentResolver.openInputStream(uri)!!)
            )
            val json = reader.readText()
            reader.close()
            parseJson(json)
        } catch (e: Exception) {
            Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /** 解析 JSON 字符串为设备列表 */
    fun parseJson(json: String): List<ElectronicItem> {
        val root = JSONObject(json)
        val jsonItems = root.getJSONArray("items")
        val items = mutableListOf<ElectronicItem>()

        for (i in 0 until jsonItems.length()) {
            val obj = jsonItems.getJSONObject(i)
            items.add(
                ElectronicItem(
                    name = obj.getString("name"),
                    category = obj.optString("category", "其他"),
                    brand = obj.optString("brand", ""),
                    model = obj.optString("model", ""),
                    purchaseDate = obj.getLong("purchaseDate"),
                    purchasePrice = obj.getDouble("purchasePrice"),
                    accessoryCost = obj.optDouble("accessoryCost", 0.0),
                    repairCost = obj.optDouble("repairCost", 0.0),
                    isSold = obj.optBoolean("isSold", false),
                    soldDate = if (obj.isNull("soldDate")) null else obj.optLong("soldDate", 0),
                    soldPrice = if (obj.isNull("soldPrice")) null else obj.optDouble("soldPrice", 0.0),
                    notes = obj.optString("notes", "")
                )
            )
        }

        return items
    }
}
