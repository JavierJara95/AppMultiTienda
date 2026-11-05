package com.duoc.appmultitienda.gestion.export

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidCsvWriter(private val resolver: ContentResolver) : CsvWriter {
    override suspend fun guardar(uri: String, documento: DocumentoCsv): String =
        withContext(Dispatchers.IO) {
            val destino = Uri.parse(uri)
            try {
                val output =
                    resolver.openOutputStream(destino, "wt")
                        ?: throw IOException("El destino no permite escribir el archivo.")
                output.use {
                    it.write(documento.contenido.toByteArray(Charsets.UTF_8))
                    it.flush()
                }
                resolver
                    .query(destino, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { if (it.moveToFirst()) it.getString(0) else documento.nombre }
                    ?: documento.nombre
            } catch (e: Exception) {
                // Evita presentar un CSV vacío o parcial como exportación completa.
                runCatching { DocumentsContract.deleteDocument(resolver, destino) }
                throw e
            }
        }
}
