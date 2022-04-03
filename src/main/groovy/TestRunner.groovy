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

import static net.grinder.script.Grinder.grinder

// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3

@RunWith(GrinderRunner)
class TestRunner {

    public static GTest test
    public static HTTPRequest request
    public static Map<String, String> headers = [:]

    public static String BASE_URL = "localhost:8000"

    @BeforeProcess
    public static void beforeProcess() {
        HTTPRequestControl.setConnectionTimeout(300000)
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
        HTTPResponse response = request.GET("http://${BASE_URL}/api/v1/flavors")

        if (response.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", response.statusCode)
        } else {
            print(response.statusCode)
            assertThat(response.statusCode, is(200))
        }
    }
}
