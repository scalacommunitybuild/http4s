/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.http4s.blaze.demo.server.endpoints

import cats.effect.Sync
import org.http4s.{ApiVersion => _, _}
import org.http4s.dsl.Http4sDsl

class HexNameHttpEndpoint[F[_]: Sync] extends Http4sDsl[F] {
  object NameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

  val service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / ApiVersion / "hex" :? NameQueryParamMatcher(name) =>
      Ok(name.getBytes("UTF-8").map("%02x".format(_)).mkString)
  }
}
