package com.epam.lenda.gymapp.integration;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/training_reporting.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.epam.lenda.gymapp.integration")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
class TrainingReportingIT {
}
