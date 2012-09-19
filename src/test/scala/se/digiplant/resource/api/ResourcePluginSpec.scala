package se.digiplant.resource.api

import org.specs2.mutable.Specification
import java.io.{FileInputStream, File}
import org.apache.commons.io.IOUtils

object ResourcePluginSpec extends Specification {

  "Resource Plugin" should {

    "start" in new ResContext {
      res must beAnInstanceOf[ResourcePlugin]
    }

    "put a resource" in new ResContext {
      val fileuid = res.put(getTestFile)
      fileuid.isDefined must beEqualTo(true)
      new File("tmp/default/5564/ac5e/5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg").exists() must beEqualTo(true)
    }

    "put a resource into a supplied source" in new ResContext {
      val fileuid = res.put(getTestFile, "images")
      fileuid.isDefined must equalTo(true)
      new File("tmp/images/5564/ac5e/5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg").exists() must beEqualTo(true)
    }

    "put a resource with metadata" in new ResContext {
      val fileuid = res.put(getTestFile, meta = Seq("100", "100", "auto"))
      fileuid.isDefined must equalTo(true)
      new File("tmp/default/5564/ac5e/5564ac5e3968e77b4022f55a23d36630bdeb0274_100_100_auto.jpg").exists() must beEqualTo(true)
    }

    "get a resource" in new ResContext {
      val fileuid = res.put(getTestFile)
      val file = res.get(fileuid.get)
      IOUtils.contentEquals(new FileInputStream(logo), new FileInputStream(file.get)) must beEqualTo(true)
    }

    "get a resource from a supplied source" in new ResContext {
      val fileuid = res.put(getTestFile, "images")
      val file = res.get(fileuid.get, "images")

      IOUtils.contentEquals(new FileInputStream(logo), new FileInputStream(file.get)) must beEqualTo(true)
    }

    "get a resource with the supplied metadata" in new ResContext {
      val fileuid = res.put(getTestFile, meta = Seq("100", "100", "auto"))
      val file = res.get(fileuid.get)

      IOUtils.contentEquals(new FileInputStream(logo), new FileInputStream(file.get)) must beEqualTo(true)
    }

    "delete a resource" in new ResContext {
      val fileuid = res.put(getTestFile)
      var file = new File("tmp/default/5564/ac5e/5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg")
      file.exists() must beEqualTo(true)

      res.delete(fileuid.get) must beEqualTo(true)
      file.exists() must beEqualTo(false)
    }

    "delete a resource with supplied metadata" in new ResContext {
      val fileuid = res.put(getTestFile, meta = Seq("100", "100", "auto"))
      var file = new File("tmp/default/5564/ac5e/5564ac5e3968e77b4022f55a23d36630bdeb0274_100_100_auto.jpg")
      file.exists() must beEqualTo(true)

      res.delete(fileuid.get) must beEqualTo(true)
      file.exists() must beEqualTo(false)
    }

  }
}
