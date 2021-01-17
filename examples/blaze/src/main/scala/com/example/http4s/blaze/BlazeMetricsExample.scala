/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.http4s.blaze

import cats.effect._
import com.codahale.metrics.{Timer => _, _}
import com.example.http4s.ExampleService
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.metrics.dropwizard._
import org.http4s.server.{HttpMiddleware, Router, Server}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Metrics
import scala.concurrent.ExecutionContext.global

class BlazeMetricsExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    BlazeMetricsExampleApp.resource[IO].use(_ => IO.never).as(ExitCode.Success)
}

object BlazeMetricsExampleApp {
  def httpApp[F[_]: ConcurrentEffect: ContextShift: Timer](blocker: Blocker): HttpApp[F] = {
    val metricsRegistry: MetricRegistry = new MetricRegistry()
    val metrics: HttpMiddleware[F] = Metrics[F](Dropwizard(metricsRegistry, "server"))
    Router(
      "/http4s" -> metrics(ExampleService[F](blocker).routes),
      "/http4s/metrics" -> metricsService[F](metricsRegistry)
    ).orNotFound
  }

  def resource[F[_]: ConcurrentEffect: ContextShift: Timer]: Resource[F, Server[F]] =
    for {
      blocker <- Blocker[F]
      app = httpApp[F](blocker)
      server <- BlazeServerBuilder[F](global)
        .bindHttp(8080)
        .withHttpApp(app)
        .resource
    } yield server
}
