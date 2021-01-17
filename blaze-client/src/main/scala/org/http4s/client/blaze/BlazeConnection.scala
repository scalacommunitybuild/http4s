/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s
package client
package blaze

import java.nio.ByteBuffer
import java.util.concurrent.TimeoutException
import org.http4s.blaze.pipeline.TailStage

private trait BlazeConnection[F[_]] extends TailStage[ByteBuffer] with Connection[F] {
  def runRequest(req: Request[F], idleTimeout: F[TimeoutException]): F[Response[F]]
}
