package com.example.expensetracker.automation;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All Automation Tests")
@SelectPackages({
        "com.example.expensetracker.automation.api",
        "com.example.expensetracker.automation.ui"
})
public class AllAutomationTests {
}