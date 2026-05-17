# E2E API Automation Framework

A **production-ready REST API automation framework** built with **REST Assured**, **TestNG**, and **Java 17**. Designed for comprehensive API testing with support for multiple authentication types, JSON Schema validation, WireMock mocking, and enterprise-grade reporting.

## Features

✅ **REST API Testing**
- Fluent API client with REST Assured 5.5.1
- Support for GET, POST, PUT, DELETE, PATCH operations
- JSON & XML payload handling with Jackson
- JSON Path extraction and validation

✅ **Authentication**
- Bearer Token authentication
- Basic Authentication with Base64 encoding
- Support for custom headers

✅ **Response Validation**
- Status code verification (200, 201, 204, 400, 404, etc.)
- JSON Schema validation
- Response body assertions with Hamcrest matchers
- Custom error handling and reporting

✅ **Test Data Management**
- Excel file data-driven testing
- CSV reader utilities
- POJO mapping with Lombok
- Configurable properties management

✅ **Mocking & Isolation**
- WireMock 3.13.1 for API mocking
- Standalone mock server setup
- Request/response stubs for CI/CD pipelines

✅ **Enterprise Reporting**
- Allure Framework 2.29.0 integration
- ChainTest report generation
- Detailed test execution logs
- Failure screenshots and response bodies

✅ **CI/CD Integration**
- Multiple Jenkinsfile configurations (Docker, GitHub, Local)
- Docker containerization (Dockerfile included)
- Maven parallel execution (3 fork counts)
- TestNG XML suite runners

## Prerequisites

- **Java 17** or higher
- **Maven 3.6.0+**
- **Git**

Optional:
- **Docker** (for containerized execution)
- **Jenkins** (for CI/CD automation)
- **Docker Hub** account (for registry push)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/robinsonmartinez23/E2E-API-Automation-Framework.git
cd E2E-API-Automation-Framework
```

### 2. Install Dependencies

```bash
mvn clean install
```

This will download all required dependencies:
- Rest Assured 5.5.1
- TestNG 7.7.0
- Jackson (JSON/XML parsing)
- Allure & ChainTest reporting
- WireMock for API mocking

### 3. Verify Installation

```bash
mvn -version
javac -version
```

## Project Structure

```
E2E-API-Automation-Framework/
├── src/
│   ├── main/
│   │   ├── java/com/qa/api/
│   │   │   ├── client/              # REST client & request builders
│   │   │   ├── configmanager/       # Configuration file management
│   │   │   ├── constants/           # AppConstants, AuthType, StatusCode
│   │   │   ├── errors/              # Custom error classes
│   │   │   ├── exceptions/          # Custom exception handling
│   │   │   ├── moking/              # WireMock setup & stubs
│   │   │   ├── pojo/                # Data models (User, Product, etc.)
│   │   │   └── utils/               # Excel, CSV, JSON utilities
│   │   └── resources/
│   │       ├── config/              # Application properties
│   │       ├── testdata/            # Excel & CSV test files
│   │       └── schemas/             # JSON schema files (*.json)
│   │
│   └── test/
│       ├── java/com/qa/api/
│       │   ├── amadeus/tests/       # Amadeus API test suite
│       │   ├── basicauth/tests/     # Basic Auth tests
│       │   ├── circuit/tests/       # Circuit breaker & complex flows
│       │   ├── contacts/tests/      # Contacts API tests
│       │   └── base/                # BaseTest class with setup/teardown
│       └── resources/
│           └── testrunners/         # TestNG XML suites
│
├── pom.xml                          # Maven configuration
├── Dockerfile                       # Docker image definition
├── Jenkinsfile                      # Jenkins pipeline config
├── DockerSetup.txt                  # Docker setup guide
├── AllureReportSetUp.txt            # Allure installation & usage
└── README.md                        # This file
```

## Configuration

### 1. Set Base URL & Credentials

Edit `src/main/resources/config/application.properties`:

```properties
baseurl=https://api.example.com
bearertoken=your-bearer-token-here
basicauth.username=user
basicauth.password=pass
```

### 2. Enable Debug Logging (Optional)

```properties
log.level=DEBUG
```

### 3. Configure Authentication Type

Supported auth types in `constants/AuthType.java`:
- `BEARER_TOKEN`
- `BASIC_AUTH`
- `NO_AUTH`

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Suite (XML)

```bash
mvn test -DsuiteXmlFiles=src/test/resources/testrunners/testng_regression.xml
```

### Run with Specific Configuration

```bash
mvn test -Dbaseurl=https://staging-api.example.com
```

### Run in Parallel (3 Threads)

```bash
mvn test -DforkCount=3 -DreuseForks=true
```

### Skip Tests

```bash
mvn clean install -DskipTests
```

## Example Test Case

```java
import com.qa.api.client.RestClient;
import com.qa.api.constants.AuthType;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import static org.hamcrest.Matchers.*;

public class ContactsAPITests extends BaseTest {

    @Test
    public void testGetAllContacts() {
        // Setup
        RestClient client = new RestClient();
        
        // Execute
        Response response = client.get(
            "https://api.contactapi.com",
            "/contacts",
            AuthType.BEARER_TOKEN
        );
        
        // Assert
        response.then()
            .statusCode(200)
            .body("contacts.size()", greaterThan(0))
            .body("contacts[0].id", notNullValue());
    }

    @Test(dataProvider = "contactData")
    public void testCreateContact(String name, String email) {
        RestClient client = new RestClient();
        
        String payload = String.format(
            "{\"name\": \"%s\", \"email\": \"%s\"}",
            name, email
        );
        
        Response response = client.post(
            "https://api.contactapi.com",
            "/contacts",
            payload,
            AuthType.BEARER_TOKEN
        );
        
        response.then().statusCode(201);
    }
}
```

## JSON Schema Validation

```java
import io.restassured.module.jsv.JsonSchemaValidator;

@Test
public void testResponseSchema() {
    RestClient client = new RestClient();
    
    Response response = client.get(
        "https://api.example.com",
        "/users/123",
        AuthType.BEARER_TOKEN
    );
    
    response.then()
        .assertThat()
        .body(JsonSchemaValidator.matchesJsonSchema(
            new File("src/main/resources/schemas/user-schema.json")
        ));
}
```

## WireMock Setup (Mocking APIs)

### Start WireMock Server

```java
import com.qa.api.moking.WireMockSetup;

public class APIMockingTest {
    private WireMockSetup wireMock;
    
    @BeforeMethod
    public void setupMocks() {
        wireMock = new WireMockSetup();
        wireMock.startServer(8080);
        wireMock.setupStub("/api/users", mockResponse);
    }
    
    @AfterMethod
    public void stopMocks() {
        wireMock.stopServer();
    }
}
```

### Example WireMock Stub Configuration

```json
{
  "request": {
    "method": "GET",
    "url": "/api/users/1"
  },
  "response": {
    "status": 200,
    "body": "{\"id\": 1, \"name\": \"John Doe\"}",
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

## Data-Driven Testing

### Reading Excel Files

```java
import com.qa.api.utils.ExcelUtil;

ExcelUtil excelUtil = new ExcelUtil(
    "src/test/resources/testdata/users.xlsx",
    "UserData"
);

List<Map<String, String>> testData = excelUtil.getTestData();

for (Map<String, String> row : testData) {
    String username = row.get("username");
    String email = row.get("email");
    // Use data in tests
}
```

### Reading CSV Files

```java
import com.qa.api.utils.CSVReaderUtil;

List<Map<String, String>> csvData = CSVReaderUtil.readCSV(
    "src/test/resources/testdata/products.csv"
);

csvData.forEach(row -> {
    String productId = row.get("product_id");
    String price = row.get("price");
    // Process data
});
```

## Reporting

### Allure Reports

Generate Allure report after test execution:

```bash
mvn test
mvn allure:report
mvn allure:serve
```

The report will open in your default browser at `http://localhost:4040`

### ChainTest Reports

ChainTest reports are automatically generated in the `chaintest-results/` directory:

```bash
# View ChainTest results
open chaintest-results/index.html
```

## Docker Execution

### Build Docker Image

```bash
docker build -t api-automation-framework:1.0 .
```

### Run Tests in Docker

```bash
docker run --rm api-automation-framework:1.0 mvn test
```

### Run with Custom Base URL

```bash
docker run --rm \
  -e BASEURL=https://api.staging.com \
  api-automation-framework:1.0 \
  mvn test
```

## Jenkins CI/CD Integration

### Using Jenkinsfile

The repository includes multiple Jenkinsfile configurations:

- **Jenkinsfile_github** - GitHub-based builds
- **Jenkinsfile_dockerhub** - Docker Hub integration
- **Jenkinsfile_local** - Local Jenkins setup

### Basic Jenkins Pipeline Setup

1. Create a new Pipeline job in Jenkins
2. Point to repository: `https://github.com/robinsonmartinez23/E2E-API-Automation-Framework.git`
3. Select **Jenkinsfile_github** as the script path
4. Configure webhooks for automatic triggering

### Example Jenkins Parameters

```
BASE_URL=https://api.example.com
AUTH_TOKEN=abc123xyz
FORK_COUNT=3
```

## Troubleshooting

### Maven Build Fails

```bash
# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository

# Rebuild
mvn install
```

### REST Assured Connection Timeout

Increase timeout in RestClient:

```java
RequestSpecification request = RestAssured.given()
    .connectTimeout(10000)      // 10 seconds
    .readTimeout(10000);
```

### WireMock Port Already in Use

```bash
# Find and kill process using port 8080
lsof -i :8080
kill -9 <PID>

# Or use different port
wireMock.startServer(8081);
```

### JSON Schema Validation Fails

1. Verify schema file path is correct
2. Check JSON structure matches schema
3. Enable logging:

```bash
mvn test -Dlog.level=DEBUG
```

## Best Practices

1. **Separate Test Data** - Keep test data in Excel/CSV, not hardcoded
2. **Use Base Classes** - Extend `BaseTest` for common setup/teardown
3. **Data Validation** - Always validate both status code AND response body
4. **Mock External APIs** - Use WireMock for non-critical dependencies
5. **Parallel Execution** - Use fork counts to speed up test runs
6. **Clear Assertions** - Use meaningful assert messages for failures
7. **Logging** - Enable request/response logging for debugging
8. **Reports** - Always generate and review Allure reports

## Resources

- [REST Assured Documentation](https://rest-assured.io/)
- [TestNG Official Docs](https://testng.org/doc/)
- [WireMock User Guide](https://wiremock.org/docs/)
- [Allure Report](https://docs.qameta.io/allure/)
- [JSON Schema Validation](https://json-schema.org/)

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m 'Add new feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Open a Pull Request

## License

This project is provided as-is for educational and professional use.

## Author

**Robinson Martinez** - SDET & QA Automation Engineer

- LinkedIn: [linkedin.com/in/robinsonmartinez23](https://linkedin.com/in/robinsonmartinez23)
- GitHub: [@robinsonmartinez23](https://github.com/robinsonmartinez23)

## Support

For issues, questions, or suggestions:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review test examples in `src/test/java`
3. Enable debug logging: `-Dlog.level=DEBUG`

---

**Last Updated:** May 2026  
**Framework Version:** 1.0-SNAPSHOT
