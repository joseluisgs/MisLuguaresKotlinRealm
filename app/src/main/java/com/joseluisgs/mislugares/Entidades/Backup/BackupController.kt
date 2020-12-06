package com.joseluisgs.mislugares.Entidades.Backup

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.joseluisgs.mislugares.Entidades.Fotografias.FotografiaController
import com.joseluisgs.mislugares.Entidades.Lugares.LugarController
import com.joseluisgs.mislugares.Entidades.Preferencias.PreferenciasController
import com.joseluisgs.mislugares.Entidades.Usuarios.Usuario
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


/**
 * Controllador de Backup
 */
object BackupController {

    fun fechaUltimoBackup(context: Context): String {
        val cad = leerPropiedades(context)
        Log.i("Backup", cad)
        return cad;
    }

    fun exportarDatos(context: Context): Boolean {
        // Recojo los datos
        val usuarios =  mutableListOf<Usuario>()
        usuarios.add(PreferenciasController.leerSesion(context))
        val lugares = LugarController.selectAll()!!
        val fotografias = FotografiaController.selectAll()!!
        val backup = Backup(
            usuarios = usuarios,
            lugares = lugares,
            fotografias = fotografias
        )
        // Creo el objeto JSON
        val backupJSON = Gson().toJson(backup)
        // Archivo el objeto JSON
        return archivar(context, backupJSON.toString())
    }

    fun importarDatos() {
        TODO("Not yet implemented")
    }

    fun test(context: Context) {
        Log.i("Backup", "Test")
        val user = PreferenciasController.leerSesion(context)
        val usuarios =  mutableListOf<Usuario>()
        usuarios.add(user)
        val lugares = LugarController.selectAll()!!
        val fotografias = FotografiaController.selectAll()!!

        val backup = Backup(
            usuarios = usuarios,
            lugares = lugares,
            fotografias = fotografias
        )

        val backupJSON = Gson().toJson(backup)
        Log.i("Backup", "Creado el objeto JSON Backup")
        val backupRec = Gson().fromJson(backupJSON, Backup::class.java)
        Log.i("Backup", "Recuperado el objeto JSON Backup")
        Log.i("Backup", backupRec.usuarios.size.toString())
        Log.i("Backup", backupRec.lugares.size.toString())
        Log.i("Backup", backupRec.fotografias.size.toString())

        Log.i("Backup", "Creado el fichero Backup")
        archivar(context, backupJSON.toString())
        val input = importar(context)
        Log.i("Backup", "Recuperado el fichero de Backup")
        // Log.i("Backup", input)
        val backupFile = Gson().fromJson(input, Backup::class.java)
        Log.i("Backup", "Recuperado el objeto JSON de Fichero")
        Log.i("Backup", "Recuperado el objeto JSON Backup")
        Log.i("Backup", backupRec.usuarios.size.toString())
        Log.i("Backup", backupRec.lugares.size.toString())
        Log.i("Backup", backupRec.fotografias.size.toString())

    }

    fun archivar(context: Context, datos: String): Boolean {
        val dirBackup = File((context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath) + "/MisLugaresBack")
        if(!dirBackup.exists())
            dirBackup.mkdir()
        val file = File(dirBackup, "backup.json")
        try {
            val strToBytes: ByteArray = datos.toByteArray()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Files.write(Paths.get(file.toURI()), strToBytes)
                return true
            } else
                return false
        }catch (ex: Exception) {
            return false
        }
    }

    fun importar(context: Context): String {
        val dirBackup =
            File((context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath) + "/MisLugaresBack")
        val file = File(dirBackup, "backup.json")
        var datos: String = ""
        if (file.exists()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                datos = Files.readAllLines(Paths.get(file.toURI()))[0]
            }
        }
        return datos
    }

    fun leerPropiedades(context: Context): String {
        val dirBackup =
            File((context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath) + "/MisLugaresBack")
        val file = File(dirBackup, "backup.json")
        if (file.exists()) {
            val basicfile: BasicFileAttributeView =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Files.getFileAttributeView(
                        Paths.get(file.toURI()),
                        BasicFileAttributeView::class.java, LinkOption.NOFOLLOW_LINKS
                    )
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            val attr: BasicFileAttributes = basicfile.readAttributes()
            val date: Long = attr.creationTime().toMillis()
            val instant: Instant = Instant.ofEpochMilli(date)
            val lt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            return DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss").format(lt)
        }
        return ""
    }
}
