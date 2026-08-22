package com.example.interviewpractice

data class InterviewQuestion(
    val question: String,
    val answer: String,
    val category: String,
    val keywords: List<String> = emptyList(),
    val code: String = ""
)

object QuestionBank {
    fun create(): List<InterviewQuestion> {
        val q = mutableListOf<InterviewQuestion>()
        fun add(question: String, answer: String, category: String, keywords: String = "", code: String = "") {
            q += InterviewQuestion(question, answer, category, keywords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }, code)
        }

        // Core Java
        add("What is OOP?", "OOP is a programming approach based on objects. Its main principles are encapsulation, inheritance, polymorphism and abstraction.", "Core Java", "oop,object,encapsulation,inheritance,polymorphism,abstraction")
        add("What is a class?", "A class is a blueprint that defines data and behavior for objects.", "Core Java", "class,object,blueprint")
        add("What is an object?", "An object is a runtime instance of a class with state and behavior.", "Core Java", "object,instance")
        add("What is encapsulation?", "Encapsulation keeps data and related methods together and restricts direct access using access modifiers.", "Core Java", "encapsulation,data,access")
        add("What is inheritance?", "Inheritance allows a child class to reuse accessible members of a parent class and specialize its behavior.", "Core Java", "inheritance,parent,child")
        add("What is polymorphism?", "Polymorphism lets the same method contract behave differently depending on the object or implementation.", "Core Java", "polymorphism,overriding,overloading")
        add("What is abstraction?", "Abstraction exposes essential behavior while hiding implementation details, commonly using interfaces or abstract classes.", "Core Java", "abstraction,interface,abstract")
        add("Interface vs abstract class?", "An interface mainly defines a contract, while an abstract class can combine abstract behavior with shared state and concrete methods.", "Core Java", "interface,abstract,class")
        add("What is method overloading?", "Overloading uses the same method name with different parameter lists and is resolved at compile time.", "Core Java", "overloading,compile")
        add("What is method overriding?", "Overriding lets a child class provide a specific implementation of an inherited method and is resolved at runtime.", "Core Java", "overriding,runtime")
        add("Why is String immutable?", "String immutability makes strings safe to share, supports string pooling and prevents their value from changing unexpectedly.", "Core Java", "string,immutable,pool")
        add("String vs StringBuilder?", "String is immutable; StringBuilder is mutable and is preferred for repeated string modifications in a single thread.", "Core Java", "string,stringbuilder,mutable")
        add("ArrayList vs LinkedList?", "ArrayList is usually better for indexed access, while LinkedList can be useful for frequent insertions or removals through known nodes.", "Collections", "arraylist,linkedlist")
        add("HashMap vs Hashtable?", "HashMap is generally preferred because it is unsynchronized and allows a null key; Hashtable is legacy and synchronized.", "Collections", "hashmap,hashtable")
        add("How does HashMap work?", "HashMap uses a hash of the key to locate a bucket and then compares keys to find the matching entry.", "Collections", "hashmap,hash,bucket")
        add("What is an exception?", "An exception is an event that disrupts normal program flow and can be handled with Java exception mechanisms.", "Core Java", "exception,handling")
        add("Checked vs unchecked exception?", "Checked exceptions are enforced by the compiler; unchecked exceptions extend RuntimeException and are usually programming or validation errors.", "Core Java", "checked,unchecked,exception")
        add("final vs finally vs finalize?", "final restricts reassignment or inheritance, finally is a cleanup block, and finalize was a deprecated GC-related hook and should not be relied on.", "Core Java", "final,finally,finalize")
        add("What is Java 8 Stream API?", "Streams provide a declarative way to filter, transform and aggregate data from collections.", "Java 8", "stream,filter,map,java8")
        add("What is a lambda expression?", "A lambda is a concise implementation of a functional interface, useful for passing behavior as data.", "Java 8", "lambda,functional")

        // Selenium
        add("What is Selenium WebDriver?", "WebDriver is a browser automation API that sends commands to a browser driver and interacts with web elements.", "Selenium", "selenium,webdriver,browser")
        add("What are Selenium locators?", "Locators identify elements. Common choices are id, name, className, CSS selector, XPath, link text and tag name.", "Selenium", "locator,id,xpath,css")
        add("XPath vs CSS selector?", "Both locate elements. XPath is powerful for relationships and text, while CSS is concise and often easier to maintain.", "Selenium", "xpath,css,locator")
        add("Absolute vs relative XPath?", "Absolute XPath starts from the root and is fragile; relative XPath starts with // and is generally more maintainable.", "Selenium", "xpath,absolute,relative")
        add("findElement vs findElements?", "findElement returns the first matching element or throws if none exists; findElements returns a list and can return an empty list.", "Selenium", "findelement,findelements")
        add("Implicit vs explicit wait?", "Implicit wait affects element lookup globally; explicit wait waits for a specific condition and is usually more precise.", "Selenium", "implicit,explicit,wait")
        add("What is FluentWait?", "FluentWait allows a timeout, polling interval and ignored exceptions so synchronization can be customized.", "Selenium", "fluentwait,polling")
        add("How do you click?", "Locate the element and call click(). For special UI behavior, Actions or JavaScript can be used as a fallback.", "Selenium", "click", "driver.findElement(By.id(\"login\")).click();")
        add("How do you enter text?", "Use sendKeys() after locating the input and clear existing text when required.", "Selenium", "sendkeys,clear", "driver.findElement(By.id(\"user\")).sendKeys(\"admin\");")
        add("How do you hover?", "Use Actions.moveToElement() and perform the action.", "Selenium", "actions,hover", "new Actions(driver).moveToElement(element).perform();")
        add("How do you double click?", "Use Actions.doubleClick() followed by perform().", "Selenium", "actions,doubleclick", "new Actions(driver).doubleClick(element).perform();")
        add("How do you right click?", "Use Actions.contextClick() to perform a context-menu click.", "Selenium", "actions,rightclick", "new Actions(driver).contextClick(element).perform();")
        add("How do you drag and drop?", "Use Actions.dragAndDrop(source,target) and perform().", "Selenium", "actions,drag,drop", "new Actions(driver).dragAndDrop(source, target).perform();")
        add("How do you switch to a frame?", "Switch into the frame before locating elements inside it.", "Selenium", "frame,switch", "driver.switchTo().frame(element);")
        add("How do you exit a frame?", "Use defaultContent() to return to the main document.", "Selenium", "frame,default", "driver.switchTo().defaultContent();")
        add("How do you switch a window?", "Use the target window handle with switchTo().window().", "Selenium", "window,handle", "driver.switchTo().window(handle);")
        add("How do you accept an alert?", "Switch to the JavaScript alert and call accept().", "Selenium", "alert,accept", "driver.switchTo().alert().accept();")
        add("How do you dismiss an alert?", "Switch to the alert and call dismiss() to cancel it.", "Selenium", "alert,dismiss", "driver.switchTo().alert().dismiss();")
        add("How do you read an alert message?", "Switch to the alert and use getText().", "Selenium", "alert,gettext", "driver.switchTo().alert().getText();")
        add("How do you select a dropdown?", "For a standard HTML select, use Selenium Select and choose by visible text, value or index.", "Selenium", "dropdown,select", "new Select(element).selectByVisibleText(\"India\");")
        add("How do you get text?", "Use getText() to read visible text from an element.", "Selenium", "gettext", "String text = element.getText();")
        add("How do you get an attribute?", "Use getAttribute() when the value is stored in an HTML attribute.", "Selenium", "attribute", "String value = element.getAttribute(\"value\");")
        add("isDisplayed vs isEnabled vs isSelected?", "isDisplayed checks visibility, isEnabled checks whether interaction is enabled, and isSelected checks selected state of selectable controls.", "Selenium", "displayed,enabled,selected", "element.isDisplayed(); element.isEnabled(); element.isSelected();")
        add("How do you take a screenshot?", "Cast WebDriver to TakesScreenshot and capture the current browser state.", "Selenium", "screenshot", "((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);")
        add("How do you scroll?", "Use JavaScriptExecutor when normal scrolling is insufficient for the test action.", "Selenium", "scroll,javascript", "((JavascriptExecutor) driver).executeScript(\"arguments[0].scrollIntoView(true);\", element);")
        add("What is Actions class?", "Actions provides advanced user interactions such as hover, drag, double click and keyboard combinations.", "Selenium", "actions")
        add("What is JavascriptExecutor?", "It executes JavaScript in the current page and is useful for controlled operations that normal WebDriver APIs cannot perform directly.", "Selenium", "javascript,executor")
        add("How do you refresh a page?", "Use WebDriver navigation refresh to reload the current page.", "Selenium", "refresh", "driver.navigate().refresh();")
        add("How do you navigate back?", "Use browser navigation to move to the previous page.", "Selenium", "back,navigation", "driver.navigate().back();")
        add("How do you open a URL?", "Use driver.get() or navigation.to() to open the target URL.", "Selenium", "url,navigate", "driver.get(\"https://example.com\");")

        // TestNG / framework
        add("What is TestNG?", "TestNG is a Java testing framework that provides annotations, assertions, grouping, parameterization and execution control.", "TestNG", "testng,framework")
        add("What is @BeforeMethod?", "It runs before every @Test method and is useful for per-test setup.", "TestNG", "beforemethod", "@BeforeMethod")
        add("What is @AfterMethod?", "It runs after every @Test method and is useful for cleanup.", "TestNG", "aftermethod", "@AfterMethod")
        add("What is @BeforeClass?", "It runs once before the first test method of the current class.", "TestNG", "beforeclass")
        add("What is priority in TestNG?", "Priority can control the execution order of test methods when ordering is required.", "TestNG", "priority")
        add("What is an assertion?", "An assertion compares actual and expected results; a failed assertion marks the test as failed.", "TestNG", "assertion,actual,expected")
        add("What is POM?", "Page Object Model separates page locators and page actions from test logic, improving readability and maintenance.", "Framework", "pom,page,object,model")
        add("What is a reusable utility?", "A utility encapsulates common operations such as waits, screenshots, Excel access or API helpers so tests avoid duplication.", "Framework", "utility,reuse")
        add("What is a data-driven framework?", "It keeps test data separate from test logic and feeds multiple datasets into the same test flow.", "Framework", "data,driven")
        add("How do you handle flaky tests?", "First reproduce the failure, identify synchronization or environment causes, improve waits and state management, then rerun repeatedly to validate stability.", "Framework", "flaky,reliability")

        // API / SQL / testing / tools
        add("What is REST API?", "REST is an architectural style for HTTP-based services that commonly uses resources, methods and status codes.", "API", "rest,http,api")
        add("GET vs POST?", "GET normally retrieves data, while POST normally submits data for creation or processing.", "API", "get,post")
        add("PUT vs PATCH?", "PUT generally replaces a resource representation; PATCH applies a partial update.", "API", "put,patch")
        add("What is HTTP 200?", "200 means the request succeeded and the response contains the expected successful result.", "API", "http,200,status")
        add("What is HTTP 401?", "401 indicates that authentication is required or the supplied authentication is invalid.", "API", "http,401,authentication")
        add("What is HTTP 404?", "404 means the requested resource could not be found.", "API", "http,404,notfound")
        add("What is API validation?", "Validate status code, response body, headers, schema, business rules and important response values.", "API", "validation,status,response")
        add("What is SQL JOIN?", "A JOIN combines rows from tables using a related condition.", "SQL", "sql,join")
        add("What is INNER JOIN?", "INNER JOIN returns only rows where the join condition matches in both tables.", "SQL", "inner,join")
        add("What is LEFT JOIN?", "LEFT JOIN keeps every row from the left table and matching rows from the right table; unmatched right values become null.", "SQL", "left,join")
        add("What is GROUP BY?", "GROUP BY combines rows into groups so aggregate functions such as COUNT or SUM can be calculated per group.", "SQL", "groupby,aggregate")
        add("WHERE vs HAVING?", "WHERE filters rows before grouping; HAVING filters grouped results after aggregation.", "SQL", "where,having")
        add("What is regression testing?", "Regression testing checks that existing functionality remains correct after changes.", "Testing", "regression")
        add("What is smoke testing?", "Smoke testing is a quick build-level check to confirm critical functionality is working enough for deeper testing.", "Testing", "smoke")
        add("What is sanity testing?", "Sanity testing is a focused check of changed or related functionality after a small fix or change.", "Testing", "sanity")
        add("What is STLC?", "STLC is the testing life cycle covering planning, analysis, design, environment readiness, execution and closure.", "Testing", "stlc")
        add("What is SDLC?", "SDLC describes the software development life cycle from requirements through development, testing, release and maintenance.", "Testing", "sdlc")
        add("Severity vs priority?", "Severity describes business or technical impact; priority describes how urgently the issue should be fixed.", "Testing", "severity,priority")
        add("What is Maven?", "Maven manages Java builds, dependencies and standard project lifecycle tasks through the pom.xml file.", "Maven", "maven,pom,dependency")
        add("What is Git?", "Git is distributed version control used to track changes, collaborate and manage branches.", "Git", "git,version,control")
        add("What is a Git branch?", "A branch is an independent line of development used to isolate changes before merging them.", "Git", "branch,merge")
        add("What is CI/CD?", "CI/CD automates build, test and delivery so changes can be validated and released consistently.", "CI/CD", "ci,cd,pipeline")
        add("What is Jenkins?", "Jenkins is an automation server commonly used to run builds, tests and deployment pipelines.", "CI/CD", "jenkins,pipeline")

        val templates = mapOf(
            "Core Java" to listOf(
                "Explain how you would choose the right Java collection for a test framework.",
                "How would you debug a NullPointerException in automation code?",
                "How would you make a utility class reusable across test classes?",
                "How would you handle duplicate data while processing test results?"
            ),
            "Selenium" to listOf(
                "An element is present but click fails. What would you check first?",
                "A test passes locally but fails in CI. How would you investigate it?",
                "An XPath works today but breaks after UI changes. How would you improve it?",
                "A page loads slowly. How would you synchronize the automation?"
            ),
            "TestNG" to listOf(
                "How would you run setup and cleanup for every test?",
                "How would you run only a selected group of tests?",
                "How would you make a failed test easy to diagnose from the report?",
                "How would you control execution order when tests have a dependency?"
            ),
            "Framework" to listOf(
                "How would you structure a maintainable Selenium framework?",
                "How would you avoid duplicate locators and actions?",
                "How would you add screenshots automatically when a test fails?",
                "How would you separate test data from test logic?"
            ),
            "API" to listOf(
                "How would you validate a REST response beyond status code?",
                "An API returns 200 but the business result is wrong. What would you validate?",
                "How would you test authentication failure scenarios?",
                "How would you validate a response schema?"
            ),
            "SQL" to listOf(
                "How would you verify data created by a UI test in the database?",
                "A query returns duplicate rows. How would you investigate it?",
                "How would you find records that exist in one table but not another?",
                "How would you validate aggregated test data?"
            ),
            "Testing" to listOf(
                "A production defect escaped testing. How would you improve coverage?",
                "How would you decide whether a build is ready for regression?",
                "How would you design smoke tests for a new release?",
                "How would you prioritize automation candidates?"
            ),
            "Maven" to listOf(
                "A dependency version causes a build conflict. How would you diagnose it?",
                "How would you run a specific TestNG suite from Maven?",
                "How would you keep dependencies consistent in a team project?",
                "How would you troubleshoot a clean-build failure?"
            ),
            "Git" to listOf(
                "You have local changes and need the latest main branch. What would you do?",
                "A merge conflict occurs in a test file. How would you resolve it?",
                "How would you create a feature branch for automation work?",
                "How would you review changes before pushing them?"
            ),
            "CI/CD" to listOf(
                "How would you run Selenium tests automatically after a code push?",
                "A CI build fails only in the runner. How would you diagnose it?",
                "How would you publish test reports from a pipeline?",
                "How would you prevent broken builds from being released?"
            )
        )
        val answerTemplates = mapOf(
            "Core Java" to "I would reproduce the issue, inspect the object state and stack trace, apply the relevant Java concept, add a focused check or unit test, and rerun the affected automation to confirm the result.",
            "Selenium" to "I would inspect the element state and locator, verify synchronization and browser context, use the appropriate WebDriver or Actions API, then rerun the scenario and confirm the expected UI state.",
            "TestNG" to "I would use the appropriate TestNG annotation, group or dependency, keep setup and cleanup deterministic, execute the affected suite, and verify the generated test result.",
            "Framework" to "I would keep the change inside the correct framework layer, reuse existing utilities and page objects, add validation at the test level, and run both the changed test and relevant regression tests.",
            "API" to "I would send the request with controlled test data, validate status, headers, body and business fields, cover negative cases, and compare the result with the API contract.",
            "SQL" to "I would identify the expected records, write a focused query using the required filter or join, compare actual database values with expected values, and document the validation result.",
            "Testing" to "I would assess business impact and risk, select the appropriate smoke, sanity or regression coverage, execute it, analyze failures and confirm the final release decision with evidence.",
            "Maven" to "I would inspect pom.xml and the dependency tree, identify the conflicting or missing artifact, align the version, run a clean build and confirm that tests execute successfully.",
            "Git" to "I would first protect my local work, synchronize with the target branch, resolve conflicts carefully, review the final diff and run relevant tests before pushing the change.",
            "CI/CD" to "I would inspect the pipeline logs and environment, reproduce the failing step if possible, fix configuration or code, rerun the pipeline and verify that tests and artifacts are produced successfully."
        )
        var scenarioNo = 1
        while (q.size < 420) {
            for ((category, questions) in templates) {
                for (question in questions) {
                    if (q.size >= 420) break
                    add("Scenario ${scenarioNo++}: $question", answerTemplates.getValue(category), category, category.lowercase())
                }
            }
        }
        return q.take(420)
    }
}
