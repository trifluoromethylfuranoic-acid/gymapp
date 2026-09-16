Feature: Training reporting across services

  Scenario: A created training is reflected in the trainer report
    Given a registered trainer and trainee
    When the trainer creates a 120-minute training two days in the future
    Then the report eventually contains 2 hours for that training month

  Scenario: Deleting a trainee removes future training duration from the report
    Given a registered trainer and trainee
    When the trainer creates a 120-minute training two days in the future
    Then the report eventually contains 2 hours for that training month
    When the trainee is deleted
    Then the report eventually omits that training month
