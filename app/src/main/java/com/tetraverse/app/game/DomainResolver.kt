package com.tetraverse.app.game

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DomainResolver {
    private val client = OkHttpClient()

    /**
     * Tries to resolve a .sol domain using Bonfida's reverse lookup proxy
     */
    suspend fun resolveSolDomain(address: String): String? = withContext(Dispatchers.IO) {
        // First try .sol
        val sol = lookupSol(address)
        if (sol != null) return@withContext sol
        
        // Then try .skr
        return@withContext lookupSkr(address)
    }

    private fun lookupSol(address: String): String? {
        try {
            val request = Request.Builder()
                .url("https://sdk-proxy.sns.id/reverse-lookup/$address")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                if (json.getString("s") == "ok") {
                    return json.getString("result") + ".sol"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Tries to resolve a .skr domain using Helius DAS API filtered by TLD House Program
     */
    private fun lookupSkr(address: String): String? {
        try {
            val url = "https://mainnet.helius-rpc.com/?api-key=86f91754-0518-4e8c-9828-444855075671"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            
            // Search precisely for Seeker ID / TLD House domains
            val payload = """
                {
                    "jsonrpc": "2.0",
                    "id": "skr-lookup",
                    "method": "searchAssets",
                    "params": {
                        "ownerAddress": "$address",
                        "creatorAddress": "TLDHkysf5pCnKsVA4gXpNvmy7psXLPEu4LAdDJthT9S",
                        "tokenType": "all"
                    }
                }
            """.trimIndent()

            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val result = json.optJSONObject("result") ?: return null
                val items = result.optJSONArray("items") ?: return null

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val content = item.optJSONObject("content")
                    val metadata = content?.optJSONObject("metadata")
                    val name = metadata?.optString("name") ?: ""
                    
                    if (name.contains(".skr", ignoreCase = true)) {
                        return name.trim().lowercase()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
