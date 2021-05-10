import com.codeborne.selenide.*;

import static com.codeborne.selenide.Condition.matchText;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.*;

public class ProfpubSatelliteTest {

    String postfixUrl = "?roistat=manager_null";

    @BeforeAll
    static void setBrowser() {
        Configuration.browser = "chrome";
        Configuration.startMaximized = true;
//        Configuration.headless = true;
//        Configuration.holdBrowserOpen = true;

        System.setProperty("webdriver.chrome", "./src/main/resources/chromedriver.exe");
    }

    @AfterAll
    static void closeBrowser() {
        close();
    }

    @DisplayName("Redirect http->https")
    @ParameterizedTest(name = "{index}. Site - {0}")
    @CsvFileSource(resources = "/sites.csv")
    // Проверяем редирект с http на https
    void testRedirectHttp(String site) {
        open("http://" + site + "/" + postfixUrl);

        assertEquals("https://" + site + "/" + postfixUrl, WebDriverRunner.url());
    }

    @DisplayName("Redirect URL with www to non-www URL")
    @ParameterizedTest(name = "{index}. Site - {0}")
    @CsvFileSource(resources = "/sites.csv")
    // Проверяем редирект с www на без www
    void testRedirectWithoutWww(String site) {
        open("https://www." + site + "/" + postfixUrl);

        assertEquals("https://" + site + "/" + postfixUrl, WebDriverRunner.url());
    }

    @DisplayName("External links that not open in new tab")
    @ParameterizedTest(name = "{index}. Site - {0}{1}")
    @CsvFileSource(resources = "/pages.csv", numLinesToSkip = 1)
    // Проверяем есть ли ссылки (http/https) которые не относятся относятся к данному сайту,
    // то должно открыватся в новом окне
    void testLinkExternalSiteOpenNewTab(String siteUrl, String pageUrl) {
        open("https://"+ siteUrl + pageUrl + postfixUrl);

        String xpath_request =
                "//a[(contains(@href,'http://') or contains(@href,'https://')) and " +
                "not (contains(@href,'" + siteUrl + "')) " +
                "and not (contains(@target,'_blank'))]";
        int no_blank_count = $$(byXpath(xpath_request)).size();
        System.out.println("no_blank_count = "+ no_blank_count);

        $$(byXpath(xpath_request)).shouldBe(CollectionCondition.empty);
    }

    @DisplayName("No site addresses in text without active link")
    @ParameterizedTest(name = "{index}. Site - {0}{1}")
    @CsvFileSource(resources = "/pages.csv", numLinesToSkip = 1)
    // Проверяем чтоб небыло http/https в тексте без активной ссылки
    void testLinkLikePlainText(String siteUrl, String pageUrl) {
        open("https://"+ siteUrl + pageUrl + postfixUrl);

        // выбираем все http в тексте
        String xpath_request =

                "//body//*[" +
                "not(self::a) and " +
                "not(self::script) and " +
                "not(name()='metadata') and " +
                "text()[" +
                "(" +
                "  contains(.,'https://') or" +
                "  contains(.,'http://')" +
                ") " +
                "and not (contains(.,'s.src = \"https://')) " +
                "and not (contains(.,'src=\"https://')) " +
                "and not (contains(.,'@import url('))]]";

        System.out.println("xpath_request = " + xpath_request);
        ElementsCollection tempResult = $$(byXpath(xpath_request));
        System.out.println("tempResult = "+ tempResult.size());

        tempResult.shouldBe(CollectionCondition.empty);
    }

    @Disabled
    @DisplayName("Phone exist")
    @ParameterizedTest(name = "{index}. Site - {0}")
    @CsvFileSource(resources = "/sites.csv")
        // Наличие телефона на сайте
    void testContactsPhoneExist(String site) {
        open("https://" + site + "/" + postfixUrl);

        $(byXpath("//a[@class='phone']")).shouldBe(Condition.exist);
    }

    @DisplayName("Standard phone mask")
    @ParameterizedTest(name = "{index}. Site - {0}")
    @CsvFileSource(resources = "/sites.csv")
    // Наличие телефона стандартного формата в шапке
    // 8(ddd) ddd-dd-dd
    void testContactsPhoneMask(String site) {
        open("https://" + site + "/" + postfixUrl);

//        String regex = "^([8][(]\\d{3}[)][\\s]\\d{3}[-]\\d{2}[-]\\d{2})$";
        String regex = "^((8|\\+7)[(]\\d{3}[)][\\s]\\d{2,3}[-]\\d{2}[-]\\d{2,3})$";
        $(byXpath("//a[@class='phone']")).shouldHave(matchText(regex));
        //
    }

    @Tag("pricelist")
    @DisplayName("No prices for previous years")
    @ParameterizedTest(name = "{index}. Site - {0}")
    @CsvFileSource(resources = "/sites-all.csv")
    // Проверяем чтоб небыло прайс-листов за прошлые года
    void testPricesForPreviousYears(String siteUrl) {
        open("https://"+ siteUrl + "/pricelist" + postfixUrl);

        // выбираем все http в тексте
        String xpath_request =

                "//body//*[" +
                        "not(self::script) and " +
                        "not(name()='metadata') and " +
                        "text()[ " +
                        "( " +
                        "(contains(.,'2018') and not (contains(.,'2018-'))) " +
                        "or " +
                        "(contains(.,'.18') and not (contains(.,'.18-')) and not (contains(.,'ул.18')) and not (contains(.,'пп.18'))) " +
                        "or " +
                        "(contains(.,'2017') and not (contains(.,'29.07.2017'))) " +
                        "or " +
                        "contains(.,'.17') " +
                        ") " +
                        "and not (contains(.,'сбор') or contains(.,'СБОР') or contains(.,'29.07.2017')) " +
                        "]]";

        System.out.println("xpath_request = " + xpath_request);
        ElementsCollection tempResult = $$(byXpath(xpath_request));
        System.out.println("tempResult = "+ tempResult.size());

        tempResult.shouldBe(CollectionCondition.empty);
    }
}
