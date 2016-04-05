package se.digiplant.res

import javax.inject.{Singleton, Inject}

import play.api._
import play.api.mvc._
import play.api.libs._
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.DateTimeZone
import collection.JavaConverters._
import java.io.File

@Singleton
class ResAssets @Inject()(environment: Environment, res: api.Res) extends Controller {

  private val timeZoneCode = "GMT"

  //Dateformatter is immutable and threadsafe
  private val df: DateTimeFormatter = DateTimeFormat
    .forPattern("EEE, dd MMM yyyy HH:mm:ss '" + timeZoneCode + "'")
    .withLocale(java.util.Locale.ENGLISH)
    .withZone(DateTimeZone.forID(timeZoneCode))

  //Dateformatter is immutable and threadsafe
  private val dfp: DateTimeFormatter = DateTimeFormat
    .forPattern("EEE, dd MMM yyyy HH:mm:ss")
    .withLocale(java.util.Locale.ENGLISH)
    .withZone(DateTimeZone.forID(timeZoneCode))

  private val parsableTimezoneCode = " " + timeZoneCode

  def at(file: String, source: String = "default"): Action[AnyContent] = Action { request =>

    def parseDate(date: String): Option[java.util.Date] = {
      try {
        //jodatime does not parse timezones, so we handle that manually
        val d = dfp.parseDateTime(date.replace(parsableTimezoneCode, "")).toDate
        Some(d)
      } catch {
        case _: Exception => None
      }
    }

    res.get(file, source).map { file =>
      request.headers.get(IF_NONE_MATCH).flatMap {
        ifNoneMatch =>
          etagFor(file).filter(_ == ifNoneMatch)
      }.map(_ => NotModified).getOrElse {
        request.headers.get(IF_MODIFIED_SINCE).flatMap(parseDate).flatMap {
          ifModifiedSince =>
            lastModifiedFor(file).flatMap(parseDate).filterNot(lastModified => lastModified.after(ifModifiedSince))
        }.map(_ => NotModified.withHeaders(
          DATE -> df.print({
            new java.util.Date
          }.getTime)
        )).getOrElse {

          val response = Ok.sendFile(file, inline = true)

          // Add Etag if we are able to compute it
          val taggedResponse = etagFor(file).map(etag => response.withHeaders(ETAG -> etag)).getOrElse(response)
          val lastModifiedResponse = lastModifiedFor(file).map(lastModified => taggedResponse.withHeaders(LAST_MODIFIED -> lastModified)).getOrElse(taggedResponse)

          // Add Cache directive if configured

          lastModifiedResponse

        }: Result
      }
    } getOrElse {
      NotFound
    }
  }

  // Last modified
  private val lastModifieds = (new java.util.concurrent.ConcurrentHashMap[String, String]()).asScala

  private def lastModifiedFor(file: File): Option[String] = {
    lastModifieds.get(file.getName).filter(_ => environment.mode == Mode.Prod).orElse {
      val lastModified = df.print({
        new java.util.Date(file.lastModified).getTime
      })
      lastModifieds.put(file.getName, lastModified)
      Some(lastModified)
    }
  }

  // Etags
  private val etags = (new java.util.concurrent.ConcurrentHashMap[String, String]()).asScala

  private def etagFor(file: File): Option[String] = {
    etags.get(file.getName).filter(_ => environment.mode == Mode.Prod).orElse {
      val maybeEtag = lastModifiedFor(file).map(_ + " -> " + file.getName).map("\"" + Codecs.sha1(_) + "\"")
      maybeEtag.foreach(etags.put(file.getName, _))
      maybeEtag
    }
  }

}
