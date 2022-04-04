import HTTPClient.NVPair
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3

@RunWith(GrinderRunner)
class MainPageRunner {
    public static GTest test
    public static HTTPRequest request
    public static Map<String, String> headers = [:]

    public static String[] CATEGORIES = ["공차", "기타", "떡볶이", "라면", "마라탕", "샌드위치", "샐러드", "스타벅스", "아마스빈", "음료", "음식", "이디야", "칵테일"]
    public static String[] FALVOR_TYEPS = ['단맛', '매운맛', '짠맛', '쓴맛', '신맛', '달콤한', '담백한', '새콤한', '시원한', '짭짤한', '느끼한', '삼삼한', '매콤한', '얼큰한', '고소한', '깔끔한', '개운한']
    public static String[] TAG_TYPES = ["ADD", "EXTRACT", "MAIN"]

    Random random = new Random()

    public static String BASE_URL = "localhost:8000"

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(3000)
        test = new GTest(1, BASE_URL)
        request = new HTTPRequest()

        HTTPResponse response = request.POST("http://${BASE_URL}/mockUser/1")
        headers["Authorization"] = response.getHeader("Authorization").getValue()
        headers["Content-Type"] = "application/json"

        grinder.logger.info("before process.")
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
        grinder.statistics.delayReports = true
        grinder.logger.info("before thread.")
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        grinder.logger.info("before. init headers")
    }

    @Test
    public void test() {
        def saveFoodId = saveFood()
        findValue(saveFoodId)
        searchPage()
    }

    public String saveFood() {
        def tags = []
        random.nextInt(5).times {
            tags.add([
                    "name"      : random.nextInt(1000).toString(),
                    "tagUseType": TAG_TYPES[random.nextInt(1)]
            ])
        }
        tags.add([
                "name"      : random.nextInt(1000).toString(),
                "tagUseType": "MAIN"
        ])
        def requestBody = [:]
        requestBody.put("categoryName", CATEGORIES[random.nextInt(CATEGORIES.length - 1)])
        requestBody.put("title", "title " + random.nextInt())
        requestBody.put("price", random.nextInt())
        requestBody.put("flavors", FALVOR_TYEPS.findAll { (it.length() + random.nextInt()) % 2 == 0 })
        requestBody.put("tags", tags)
        requestBody.put("reviewMsg", "msg")
        requestBody.put("foodStatus", "SHARED")
        HTTPResponse response = request.POST("http://${BASE_URL}/api/v1/foods", requestBody)

        checkStatusCode(response.statusCode)

        return response.getHeader("location").getValue()
    }

    public void findValue(def location) {
        HTTPResponse response = request.GET(location)

        checkStatusCode(response.statusCode)
    }

    public void searchPage() {
        def tag = []
        def flavors = []
        def pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        println(LocalDateTime.now().format(pattern))
        NVPair[] param = [
                new NVPair("categoryName", CATEGORIES[random.nextInt(CATEGORIES.length - 1)]),
                new NVPair("offset", Integer.toString(random.nextInt(1000))),
                new NVPair("pageSize", Integer.toString(10)),
                new NVPair("firstSearchTime", LocalDateTime.now().format(pattern)),
                new NVPair("tags", tag.join(",")),
                new NVPair("flavors", flavors.join(","))
        ]
        HTTPResponse response = request.GET("http://${BASE_URL}/api/v1/foods", param)

        checkStatusCode(response.statusCode)
    }

    void checkStatusCode(def statusCode) {
        assertThat(((int) statusCode / 100), is(2))
    }
}
