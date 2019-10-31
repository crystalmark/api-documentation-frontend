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

package uk.gov.hmrc.apidocumentation.controllers

import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.apidocumentation.ErrorHandler
import uk.gov.hmrc.apidocumentation.config.ApplicationConfig
import uk.gov.hmrc.apidocumentation.services.{ApiDefinitionService, ProxyAwareApiDefinitionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class ProxyRamlController @Inject()(apiDefinitionService: ProxyAwareApiDefinitionService,
                                    errorHandler: ErrorHandler)(implicit val appConfig: ApplicationConfig, val ec: ExecutionContext)
  extends FrontendController
    with StreamedResponseResourceHelper {

  private val RAML_FILE_NAME = "application.raml"

  import cats.implicits._
  import cats.data.OptionT

  def downloadRaml(serviceName: String, version: String) = Action.async { implicit request =>
    OptionT(apiDefinitionService.fetchApiDocumentationResource(serviceName,version,RAML_FILE_NAME))
      .getOrElseF(failedDueToNotFoundException(serviceName,version,RAML_FILE_NAME))
      .map(handler(serviceName,version,RAML_FILE_NAME))
  }

  def downloadSchemas(serviceName: String, version: String, resource: String) = Action.async { implicit request =>
    val ultimateResource = s"schemas/$resource"
    OptionT(apiDefinitionService.fetchApiDocumentationResource(serviceName,version,ultimateResource))
      .getOrElseF(failedDueToNotFoundException(serviceName,version,RAML_FILE_NAME))
      .map(handler(serviceName,version,ultimateResource))
  }
}

