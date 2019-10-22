/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.apidocumentation.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.apidocumentation.config.ApplicationConfig
import uk.gov.hmrc.apidocumentation.connectors.{ApiDefinitionConnector, PrincipalApiDefinitionConnector, SubordinateApiDefinitionConnector}
import uk.gov.hmrc.apidocumentation.models._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.metrics.{API, Metrics}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful


trait BaseApiDefinitionService {
  def fetchExtendedDefinition(serviceName: String, email: Option[String])
                            (implicit hc: HeaderCarrier): Future[Option[ExtendedAPIDefinition]]

  def fetchAllDefinitions(email: Option[String])(implicit hc: HeaderCarrier): Future[Seq[APIDefinition]]
}

trait ApiDefinitionService extends BaseApiDefinitionService {
  def raw: ApiDefinitionConnector
  def metrics: Metrics

  def api: API
  def enabled: Boolean

  def fetchExtendedDefinition(serviceName: String, email: Option[String] = None)
                            (implicit hc: HeaderCarrier): Future[Option[ExtendedAPIDefinition]] =
    if (enabled) {
      metrics.record(api) {
        raw.fetchApiDefinition(serviceName, email)
      }
    } else {
      successful(None)
    }

  def fetchAllDefinitions(email: Option[String] = None)(implicit hc: HeaderCarrier): Future[Seq[APIDefinition]] =
    if (enabled) {
      metrics.record(api) {
        raw.fetchAllApiDefinitions(email)
      }
    } else {
      successful(Seq.empty)
    }
}

@Singleton
class PrincipalApiDefinitionService @Inject()(
                                               val raw: PrincipalApiDefinitionConnector,
                                               val metrics: Metrics
) extends ApiDefinitionService {

  val api: API = API("api-definition-principal")

  val enabled: Boolean = true
}

@Singleton
class SubordinateApiDefinitionService @Inject()(
                                                 val raw: SubordinateApiDefinitionConnector,
                                                 val appConfig: ApplicationConfig,
                                                 val metrics: Metrics
   ) extends ApiDefinitionService {

  val api: API = API("api-definition-subordinate")

  val enabled: Boolean = appConfig.showSandboxAvailability

  Logger.info(s"Subordinate Api Definition Service is ${if(enabled) "enabled" else "disabled"}")

  Logger.info(s"Subordinate Api Definition Service use-proxy = ${appConfig.apiDefinitionSubordinateUseProxy}")
  Logger.info(s"Subordinate Api Definition Service bseUrl = ${appConfig.apiDefinitionSubordinateBaseUrl}")
}
