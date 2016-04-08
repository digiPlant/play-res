package se.digiplant.res

import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module

final class ResModule extends Module{

  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[api.Res].to[api.ResImpl]
    )
  }

}
