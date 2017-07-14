package se.digiplant.res

import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.specification._
import org.specs2.mutable.Around
import org.specs2.execute.AsResult
import play.api.{Application, Configuration, Environment}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import java.io.File

import org.apache.commons.io.FileUtils
import play.api.http.FileMimeTypes

import scala.concurrent.ExecutionContext
import util.Random

trait ResContext extends Around with TempFile with Mockito with MustThrownExpectations with Injecting {

  val environment = Environment.simple()
  val configuration = Configuration("res.default" -> "tmp/default", "res.images" -> "tmp/images")

  implicit val app: Application = new GuiceApplicationBuilder()
    .configure(configuration)
    .in(environment)
    .build

  implicit val executionContext = inject[ExecutionContext]

  val res = new api.ResImpl(environment, configuration, mock[FileMimeTypes])
  val resAssets = new ResAssets(Helpers.stubControllerComponents(), mock[Environment], res)

  def around[T : AsResult](t: =>T) = Helpers.running(app) {
    val result = AsResult.effectively(t)

    tmp.delete()

    result
  }
}

trait TempFile extends Scope {
  val tmp = new File("tmp")
  val logo = new File("test/resources/digiPlant.jpg")

  def testFile: File = {
    tmp.mkdir()
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('1' to '9')
    val rand = (1 to 20).map(x => chars(Random.nextInt(chars.length))).mkString
    val tmpFile = new File("tmp", rand + ".jpg")
    FileUtils.copyFile(logo, tmpFile)
    tmpFile
  }

  def anonymousTestFile: File = {
    tmp.mkdir()
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('1' to '9')
    val rand = (1 to 20).map(x => chars(Random.nextInt(chars.length))).mkString
    val tmpFile = new File("tmp", rand)
    FileUtils.copyFile(logo, tmpFile)
    tmpFile
  }
}
