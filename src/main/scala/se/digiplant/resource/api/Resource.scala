package se.digiplant.resource.api

import play.api._
import java.io.File

object Resource {

  private def resourceAPI(implicit app: Application): ResourcePlugin = {
    app.plugin[ResourcePlugin] match {
      case Some(plugin) => plugin
      case None => sys.error("The Resource Plugin is not registered in conf/play.plugins")
    }
  }

  /**
   * Retrieves a file with the specified fileuid and if specified all meta attributes
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return A File
   */
  def get(fileuid: String, source: String = "default", meta: Seq[String] = Seq.empty)(implicit app: Application): Option[File] = {
    resourceAPI.get(fileuid, source, meta)
  }

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param source The configured source name
   * @param filename Override the sha1 checksum generated filename
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique file name with the metadata appended
   */
  def put(file: File, source: String = "default", filename: String = "", meta: Seq[String] = Seq.empty)(implicit app: Application): Option[String] = {
    resourceAPI.put(file, source, filename, meta)
  }

  /**
   * Deletes a file with the specified fileuid
   * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
   * @param source The configured source name
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return true if file was deleted, false if it failed
   */
  def delete(fileuid: String, source: String = "default", meta: Seq[String] = Seq.empty)(implicit app: Application): Boolean = {
    resourceAPI.delete(fileuid, source, meta)
  }

  /**
   * Retrieves a file with the specified filepath and if specified all meta attributes
   * @param filePath The filepath relative to the play app, it can also include the meta if you don't want to specify it separately
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return Option[File]
   */
  def fileWithMeta(filePath: String, meta: Seq[String] = Seq.empty)(implicit app: Application): Option[File] = {
    resourceAPI.fileWithMeta(filePath, meta)
  }

  /**
   * Puts a file into the supplied source
   * @param file A file to be stored
   * @param filePath The filepath relative to the play app
   * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
   * @return The unique filePath with the metadata appended
   */
  def saveWithMeta(file: File, filePath: String, meta: Seq[String] = Seq.empty)(implicit app: Application): String = {
    resourceAPI.saveWithMeta(file, filePath, meta)
  }
}
