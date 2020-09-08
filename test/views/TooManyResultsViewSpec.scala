/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import config.FrontendAppConfig
import controllers.routes
import model.{JourneyConfigDefaults, JourneyConfigV2, JourneyDataV2, JourneyOptions}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.TestConstants._
import views.html.v2.too_many_results

class TooManyResultsViewSpec extends ViewSpec {
  implicit val testRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val too_many_results = app.injector.instanceOf[too_many_results]


  val testHtml = Html("")

  object tooManyResultsMessages {
    val title = "No results found"
    val heading1 = "There are too many results"
    val heading2 = "We couldn't find any results for that property name or number"

    def bullet1(postcode: String) = s"$postcode for postcode"

    val bullet2NoFilter = "nothing for property name or number"

    def bullet2WithFilter(filter: String) = s"'$filter' for name or number"

    val line1 = "You entered:"
    val button = "Try a new search"
    val back = "Back"
  }

  object welshTooManyResultsMessages {
    val title = "Dim canlyniadau wedi’u darganfod"
    val heading1 = "Mae yna ormod o ganlyniadau"
    val heading2 = "Ni allem ddod o hyd i unrhyw ganlyniadau ar gyfer enw neu rif yr eiddo hwnnw"

    def bullet1(postcode: String) = s"$postcode am y cod post"

    val bullet2NoFilter = "ddim byd ar gyfer enw neu rif eiddo"

    def bullet2WithFilter(filter: String) = s"'$filter' ar gyfer enw neu rif"

    val line1 = "Nodoch:"
    val button = "Rhowch gynnig ar chwiliad newydd"
    val back = "Yn ôl"
  }

  def journeyData(showBackButtons: Boolean, ukMode: Option[Boolean] = None) = JourneyDataV2(
    JourneyConfigV2(
      2,
      JourneyOptions(
        continueUrl = testContinueUrl,
        showBackButtons = Some(showBackButtons),
        ukMode = ukMode
      )
    )
  )

  "The English 'Too Many Results' page" should {
    implicit val lang: Lang = Lang("en")

    "be rendered" when {
      "the back buttons are enabled in the journey config" when {
        "no filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = true),
              lookup = model.Lookup(
                None,
                testPostCode
              ),
              firstLookup = true,
              isWelsh = false,
              isUKMode = true
            ).body)

            doc.getBackLinkText shouldBe tooManyResultsMessages.back
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading1
            doc.paras.not(".language-select").get(1).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2NoFilter
            doc.getALinkText("anotherSearch") shouldBe tooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? EnglishConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = true),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false,
                isWelsh = false,
              isUKMode = true
            ).body)

            doc.getBackLinkText shouldBe tooManyResultsMessages.back
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(1).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("anotherSearch") shouldBe tooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? EnglishConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered with ukMode = true" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = true, ukMode = Some(true)),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false,
              isWelsh = false,
              isUKMode = true
            ).body)

            doc.getBackLinkText shouldBe tooManyResultsMessages.back
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(1).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("anotherSearch") shouldBe tooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? EnglishConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }
      }

      "the back buttons are not enabled in the journey config" when {
        "no filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = false),
              lookup = model.Lookup(
                None,
                testPostCode
              ),
              firstLookup = true,
              isWelsh = false,
              isUKMode = true
            ).body)

            doc.getBackLinkText shouldBe empty
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading1
            doc.paras.not(".language-select").get(1).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2NoFilter
            doc.getALinkText("anotherSearch") shouldBe tooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? EnglishConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = false),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false,
              isWelsh = false,
              isUKMode = true
            ).body)

            doc.getBackLinkText shouldBe empty
            doc.title shouldBe tooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe tooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(1).text shouldBe tooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe tooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe tooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("anotherSearch") shouldBe tooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? EnglishConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }
      }
    }
  }

  "The Welsh 'Too Many Results' page" should {
    implicit val lang: Lang = Lang("cy")

    "be rendered" when {
      "the back buttons are enabled in the journey config" when {
        "no filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = true),
              lookup = model.Lookup(
                None,
                testPostCode
              ),
              firstLookup = true,
              isWelsh = true
            ).body)

            doc.getBackLinkText shouldBe welshTooManyResultsMessages.back
            doc.title shouldBe welshTooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe welshTooManyResultsMessages.heading1
            doc.paras.not(".language-select").get(1).text shouldBe welshTooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe welshTooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe welshTooManyResultsMessages.bullet2NoFilter
            doc.getALinkText("anotherSearch") shouldBe welshTooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? WelshConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = true),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false,
              isWelsh = true
            ).body)

            doc.getBackLinkText shouldBe welshTooManyResultsMessages.back
            doc.title shouldBe welshTooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe welshTooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(1).text shouldBe welshTooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe welshTooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe welshTooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("anotherSearch") shouldBe welshTooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? WelshConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }
      }

      "the back buttons are not enabled in the journey config" when {
        "no filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = false),
              lookup = model.Lookup(
                None,
                testPostCode
              ),
              firstLookup = true,
              isWelsh = true
            ).body)

            doc.getBackLinkText shouldBe empty
            doc.title shouldBe welshTooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe welshTooManyResultsMessages.heading1
            doc.paras.not(".language-select").get(1).text shouldBe welshTooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe welshTooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe welshTooManyResultsMessages.bullet2NoFilter
            doc.getALinkText("anotherSearch") shouldBe welshTooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? WelshConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = false),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false,
              isWelsh = true
            ).body)

            doc.getBackLinkText shouldBe empty
            doc.title shouldBe welshTooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe welshTooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(1).text shouldBe welshTooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe welshTooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe welshTooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("anotherSearch") shouldBe welshTooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? WelshConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }

        "a filter has been entered with ukMode = true" when {
          "there are too many addresses" in {
            val doc = Jsoup.parse(too_many_results(
              id = testJourneyId,
              journeyData = journeyData(showBackButtons = false, ukMode = Some(true)),
              lookup = model.Lookup(
                Some(testFilterValue),
                testPostCode
              ),
              firstLookup = false,
              isWelsh = true
            ).body)

            doc.getBackLinkText shouldBe empty
            doc.title shouldBe welshTooManyResultsMessages.title
            doc.getH1ElementAsText shouldBe welshTooManyResultsMessages.heading2
            doc.paras.not(".language-select").get(1).text shouldBe welshTooManyResultsMessages.line1
            doc.bulletPointList.select("li").first.text shouldBe welshTooManyResultsMessages.bullet1(testPostCode)
            doc.bulletPointList.select("li").last.text shouldBe welshTooManyResultsMessages.bullet2WithFilter(testFilterValue)
            doc.getALinkText("anotherSearch") shouldBe welshTooManyResultsMessages.button
            doc.getALinkText("enterManual") shouldBe "??? WelshConstantsUkMode.SELECT_PAGE_EDIT_ADDRESS_LINK_TEXT"
            doc.getLinkHrefAsText("enterManual") shouldBe routes.AddressLookupController.edit(testJourneyId, Some(testPostCode)).url
          }
        }
      }
    }
  }

}
