package se.digiplant.resource.api

import play.api._
import play.api.mvc._
import java.io.{FileInputStream, File}
import org.apache.commons.io.{FilenameUtils, FileUtils}
import org.apache.commons.codec.digest.DigestUtils

class ResourcePlugin(app: Application) extends Plugin {

  lazy val configuration = app.configuration.getConfig("res").getOrElse(Configuration.empty)

  lazy val sources: Map[String, File] = configuration.subKeys.map {
    sourceKey =>
      val path = configuration.getString(sourceKey).getOrElse(throw configuration.reportError("res." + sourceKey, "Missing res path[" + sourceKey + "]"))
      val file = app.getFile(path)
      if (file.isDirectory && !file.exists()) {
        FileUtils.forceMkdir(file)
      }
      sourceKey -> file
  }.toMap

  override def enabled = !configuration.subKeys.isEmpty

  /**
   * Retrieves a file with the specified fileuid and if specified all meta attributes
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def get(fileuid: String, source: String = "default", meta: Seq[String] = Seq.empty): Option[File] = {
    sources.get(source).flatMap { dir =>
      val filename = if (meta.isEmpty)
          fileuid
        else {
          val name = FilenameUtils.getBaseName(fileuid)
          val ext = FilenameUtils.getExtension(fileuid)
          name + meta.mkString(if (!meta.isEmpty) { "_" } else "", "_", ".") + ext
        }
      Option(FileUtils.getFile(dir, hashAsDirectories(filename), filename)).filter(_.exists)
    }
  }

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param source The configured source name
   * @param filename Override the sha1 checksum generated filename
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  def put(file: File, source: String = "default", filename: String = "", meta: Seq[String] = Seq.empty): Option[String] = {

    val (name, ext) = if (filename.isEmpty) {
      val input: FileInputStream = new FileInputStream(file)
      (DigestUtils.shaHex(input), FilenameUtils.getExtension(file.getName))
    } else {
      (FilenameUtils.getBaseName(filename), FilenameUtils.getExtension(filename))
    }

    sources.get(source).flatMap { dir =>
      val base = new File(dir, hashAsDirectories(name))
      if (!base.exists()) {
        base.mkdirs()
      }

      val fileuid = name + meta.mkString(if (!meta.isEmpty) { "_" } else "", "_", ".") + ext
      val target = new File(base, fileuid)

      if (!target.exists()) {
        FileUtils.moveFile(file, target)
      }
      Some(fileuid)
    }
  }

  /**
   * Deletes a file with the specified fileuid
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return true if file was deleted, false if it failed
   */
  def delete(fileuid: String, source: String = "default", meta: Seq[String] = Seq.empty): Boolean = get(fileuid, source, meta).map(_.delete()).getOrElse(false)

  private def hashAsDirectories(hash: String): String = hash.substring(0, 4) + '/' + hash.substring(4, 8)

}
