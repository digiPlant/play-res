package se.digiplant.res.api

import play.api._
import libs.{Files, MimeTypes}
import play.api.mvc._
import java.io.{FileInputStream, File}
import org.apache.commons.io.{FilenameUtils, FileUtils}
import org.apache.commons.codec.digest.DigestUtils

class ResPlugin(app: Application) extends Plugin {

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
   * @param extension The extension of the file
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  def put(file: File, source: String = "default", filename: Option[String] = None, extension: Option[String] = None, meta: Seq[String] = Seq.empty): Option[String] = {
    val ext = extension.getOrElse(FilenameUtils.getExtension(file.getName))
    require(!ext.isEmpty, "file must have extension or extension must be specified")

    val name = filename.map(FilenameUtils.getBaseName(_)).getOrElse(DigestUtils.shaHex(new FileInputStream(file)))

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
   * Puts a filePart into the supplied source and tries to figure out it's correct mimetype and extension (Java version)
   * @param filePart A filePart that's been uploaded to play to be stored
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  def put(filePart: play.mvc.Http.MultipartFormData.FilePart, source: String, meta: Seq[String]): Option[String] = {
    put(filePart.getFile, source, None, getExtensionFromMimeType(Option(filePart.getContentType)), meta)
  }

  /**
   * Puts a filePart into the supplied source and tries to figure out it's correct mimetype and extension
   * @param filePart A filePart that's been uploaded to play to be stored
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  def put(filePart: play.api.mvc.MultipartFormData.FilePart[Files.TemporaryFile], source: String, meta: Seq[String]): Option[String] = {
    put(filePart.ref.file, source, None, getExtensionFromMimeType(filePart.contentType), meta)
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

  /**
   * Retrieves a file with the specified filepath and if specified all meta attributes
   * @param filePath The filepath relative to the play app, it can also include the meta if you don't want to specify it separately
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def fileWithMeta(filePath: String, meta: Seq[String] = Seq.empty): Option[File] = {
    val path = FilenameUtils.getPath(filePath)
    val name = FilenameUtils.getBaseName(filePath)
    val ext = FilenameUtils.getExtension(filePath)
    app.getExistingFile(path + name + meta.mkString(if (!meta.isEmpty) { "_" } else "", "_", ".") + ext)
  }

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param filePath The filepath relative to the play app
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique filePath with the metadata appended
   */
  def saveWithMeta(file: File, filePath: String, meta: Seq[String] = Seq.empty): String = {

    val path = FilenameUtils.getPath(filePath)
    val name = FilenameUtils.getBaseName(filePath)
    val ext = FilenameUtils.getExtension(filePath)

    val base = app.getFile(path)
    if (!base.exists()) {
      base.mkdirs()
    }

    val targetPath = path + name + meta.mkString(if (!meta.isEmpty) { "_" } else "", "_", ".") + ext
    val target = app.getFile(targetPath)
    if (target.exists()) {
      FileUtils.copyFile(file, target)
    } else {
      FileUtils.moveFile(file, target)
    }

    targetPath
  }

  /**
   * Gets the file extension of known file types, but because the MimeTypes map isn't sorted properly we need to override the images for play-scalr
   * otherwise it chooses the top most extension for image/jpeg = jfif
   * @param mime The mimetype
   * @return a file extension without the .
   */
  private def getExtensionFromMimeType(mime: Option[String]): Option[String] = mime match {
    case None => None
    case Some("image/jpeg") => Some("jpg")
    case Some("image/png") => Some("png")
    case Some("image/gif") => Some("gif")
    case Some(m) => MimeTypes.types.map(_.swap).get(m)
  }
}
