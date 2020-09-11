package controllers

import com.codahale.metrics.SharedMetricRegistries
import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import itutil.config.PageElementConstants.LookupPage
import play.api.i18n.Lang
import play.api.Application
import play.api.Mode.Test
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.util.Random

class LookupPageISpec extends IntegrationSpecBase {

  def longFilterValue = (1 to 257) map (_ => Random.alphanumeric.head) mkString

  // TODO: Make hint configurable as part of welsh translation
  val hardCodedFormHint = " For example, The Mill, 116 or Flat 37a"

  override lazy val app: Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .configure(fakeConfig())
      .configure("error.required" → "Postcode is required")
      .in(Test)
      .build()
  }

  "The lookup page" when {
    "when provided with no page config" should {
      "Render the default content" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForDefaultConfig(fResponse)

        doc.title shouldBe messages("lookupPage.title")
        doc.h1.text() shouldBe messages("lookupPage.heading")

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(messages("lookupPage.postcodeLabel")),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(messages("lookupPage.filterLabel") + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(messages("lookupPage.manualAddressLinkText"))
        )

        doc.submitButton.text() shouldBe "Continue"
      }

      "Show the default 'postcode not entered' error message" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = "select")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "This field is required"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.postcodeId, message)
        )

        doc.input(LookupPage.postcodeId) should have(
          errorMessage("Error: error.required"),
          value("")
        )
      }

      "Show the default 'invalid postcode' error message" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"select?postcode=QQ")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "Enter a real Postcode e.g. AA1 1AA"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.postcodeId, message)
        )

        doc.input(LookupPage.postcodeId) should have(
          errorMessage(s"Error: $message"),
          value("QQ")
        )
      }

      "Show the default 'filter invalid' error messages" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, OK)

        val filterValue = longFilterValue
        val fResponse = buildClientLookupAddress(path = s"select?filter=$filterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe BAD_REQUEST

        val message = "Your house name/number needs to be fewer than 256 characters"

        doc.errorSummary should have(
          errorSummaryMessage(LookupPage.filterId, message)
        )

        doc.input(LookupPage.filterId) should have(
          errorMessage(s"Error: $message Error: error.required"),
          value(filterValue)
        )
      }
    }

    "Provided with custom content" should {
      "Render the page with custom content" in {
        stubKeystore(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.title shouldBe fullLookupPageConfig.title.get + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(fullLookupPageConfig.postcodeLabel.get),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }

      "not display the back button if disabled" in {
        stubKeystore(testJourneyId, testDefaultLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testDefaultLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        doc.select("a[class=govuk-back-link]") should not have (
          text("Back")
          )
      }
    }

    "Provided with config with all booleans set to true" should {
      "Render the page correctly with custom elements" in {
        stubKeystore(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testCustomLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()
        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigAllTrue(fResponse, "NAV_TITLE")

        doc.title shouldBe fullLookupPageConfig.title.get + " - NAV_TITLE - GOV.UK"
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get

        doc.select("a[class=govuk-back-link]") should have(
          text("Back")
        )

        doc.input(LookupPage.postcodeId) should have(
          label(fullLookupPageConfig.postcodeLabel.get),
          value(testPostCode)
        )

        doc.input(LookupPage.filterId) should have(
          label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint),
          value(testFilterValue)
        )

        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }
    }

    "Provided with config where all the default values are overriden with the default values" should {
      "Render " in {
        stubKeystore(testJourneyId, testOtherCustomLookupPageJourneyConfigV2, OK)
        stubKeystoreSave(testJourneyId, testOtherCustomLookupPageJourneyConfigV2, OK)

        val fResponse = buildClientLookupAddress(path = s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        val doc = getDocFromResponse(res)

        res.status shouldBe OK

        testCustomPartsOfGovWrapperElementsForFullConfigWithAllTopConfigAsNoneAndAllBooleansFalse(fResponse)

        doc.title shouldBe fullLookupPageConfig.title.get
        doc.h1.text() shouldBe fullLookupPageConfig.heading.get
        doc.select("a[class=govuk-back-link]") should have(text("Back"))
        doc.input(LookupPage.postcodeId) should have(label(fullLookupPageConfig.postcodeLabel.get), value(testPostCode))
        doc.input(LookupPage.filterId) should have(label(fullLookupPageConfig.filterLabel.get + hardCodedFormHint), value(testFilterValue))
        doc.link(LookupPage.manualAddressLink) should have(
          href(routes.AddressLookupController.edit(testJourneyId).url),
          text(fullLookupPageConfig.manualAddressLinkText.get)
        )

        doc.submitButton.text() shouldBe fullLookupPageConfig.submitLabel.get
      }
    }
  }

  "technical difficulties" when {
    "the welsh content header isn't set and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }
    "the welsh content header is set to false and welsh object isn't provided in config" should {
      "render in English" in {
        stubKeystore(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, testMinimalLevelJourneyConfigV2, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to false and welsh object is provided in config" should {
      "render in English" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithWelshCookie(useWelsh = false), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages("constants.intServerErrorTitle")
        doc.h1 should have(text(messages("constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages("constants.intServerErrorTryAgain")))
      }
    }

    "the welsh content header is set to true and welsh object provided in config" should {
      "render in Welsh" in {
        val v2Config = Json.toJson(fullDefaultJourneyConfigModelV2WithAllBooleansSet(allBooleanSetAndAppropriateOptions = true, isWelsh = true))
        stubKeystore(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)
        stubKeystoreSave(testJourneyId, v2Config, INTERNAL_SERVER_ERROR)

        val fResponse = buildClientLookupAddress(s"lookup?postcode=$testPostCode&filter=$testFilterValue")
          .withHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRFAndLang(), "Csrf-Token" -> "nocheck")
          .get()

        val res = await(fResponse)
        res.status shouldBe INTERNAL_SERVER_ERROR

        val doc = getDocFromResponse(res)
        doc.title shouldBe messages(Lang("cy"), "constants.intServerErrorTitle")
        doc.h1 should have(text(messages(Lang("cy"), "constants.intServerErrorTitle")))
        doc.paras should have(elementWithValue(messages(Lang("cy"), "constants.intServerErrorTryAgain")))
      }
    }
  }
}
