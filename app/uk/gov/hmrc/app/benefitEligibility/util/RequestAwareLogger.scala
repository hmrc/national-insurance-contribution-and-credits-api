/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.app.benefitEligibility.util

import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}

class RequestAwareLogger(underlying: Logger) {

  def this(clazz: Class[_]) =
    this(Logger(clazz))

  def trace(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDsInMDC(underlying.trace(msg))

  def trace(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDsInMDC(underlying.trace(msg, error))

  def debug(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDsInMDC(underlying.debug(msg))

  def debug(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDsInMDC(underlying.debug(msg, error))

  def info(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDsInMDC(underlying.info(msg))

  def info(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDsInMDC(underlying.info(msg, error))

  def warn(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDsInMDC(underlying.warn(msg))

  def warn(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDsInMDC(underlying.warn(msg, error))

  def error(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDsInMDC(underlying.error(msg))

  def error(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDsInMDC(underlying.error(msg, error))

  private def withRequestIDsInMDC(f: => Unit)(implicit hc: HeaderCarrier): Unit = {

    val RequestIdKey     = "X-Request-ID"
    val CorrelationIdKey = "CorrelationId"

    val requestId = hc.requestId.getOrElse(RequestId("Undefined"))
    val correlationId = hc.otherHeaders
      .collectFirst { case (key, value) if key.equalsIgnoreCase(CorrelationIdKey) => value }

    MDC.put(RequestIdKey, requestId.value)
    correlationId.foreach(MDC.put(CorrelationIdKey, _))
    f
    MDC.remove(RequestIdKey)
    correlationId.foreach(_ => MDC.remove(CorrelationIdKey))
  }

}
