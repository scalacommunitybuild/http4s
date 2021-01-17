/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.circe

import io.circe.Encoder
import org.http4s.EntityEncoder

/**
  * Derive [[EntityEncoder]] if implicit [[Encoder]] is in the scope without need to explicitly call `jsonEncoderOf`
  */
trait CirceEntityEncoder {
  implicit def circeEntityEncoder[F[_], A: Encoder]: EntityEncoder[F, A] =
    jsonEncoderOf[F, A]
}

object CirceEntityEncoder extends CirceEntityEncoder
