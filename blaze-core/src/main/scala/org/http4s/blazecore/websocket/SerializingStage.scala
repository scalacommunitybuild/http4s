/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.blazecore.websocket

import org.http4s.blaze.pipeline.MidStage
import scala.concurrent.Future

private final class SerializingStage[I] extends PassThrough[I] with Serializer[I] {
  val name: String = "SerializingStage"
}

private abstract class PassThrough[I] extends MidStage[I, I] {
  def readRequest(size: Int): Future[I] = channelRead(size)

  def writeRequest(data: I): Future[Unit] = channelWrite(data)

  override def writeRequest(data: collection.Seq[I]): Future[Unit] = channelWrite(data)
}
