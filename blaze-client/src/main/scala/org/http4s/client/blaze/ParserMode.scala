/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.client
package blaze

sealed abstract class ParserMode extends Product with Serializable

object ParserMode {
  case object Strict extends ParserMode
  case object Lenient extends ParserMode
}
