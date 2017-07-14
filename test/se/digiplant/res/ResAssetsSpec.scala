package se.digiplant.res

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._

object ResAssetsSpec extends Specification {

  "ResAssets Controller" should {

    "return resource" in new ResContext {
      res.put(testFile)

      val result = resAssets.at("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg")(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/octet-stream")
    }

    "return resource in supplied source" in new ResContext {
      res.put(testFile, "images")

      val result = resAssets.at("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg", "images")(FakeRequest())

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/octet-stream")
    }
  }
}
