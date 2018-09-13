package com.example.http4s.blaze.demo.server

import cats.effect._
import fs2.{Stream}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.ExecutionContext.Implicits.global

object Server extends HttpServer

class HttpServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val s = for {
      client <- BlazeClientBuilder[IO](global).stream
      ctx <- Stream(new Module[IO](client))
      exitCode <- BlazeBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .mountService(ctx.fileHttpEndpoint, s"/${endpoints.ApiVersion}")
        .mountService(ctx.nonStreamFileHttpEndpoint, s"/${endpoints.ApiVersion}/nonstream")
        .mountService(ctx.httpServices)
        .mountService(ctx.basicAuthHttpEndpoint, s"/${endpoints.ApiVersion}/protected")
        .serve
    } yield exitCode
    s.compile.toList.map(_.head)
  }

}
