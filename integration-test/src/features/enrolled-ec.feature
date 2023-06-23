Feature: All about Enrolled EC workflow

  Background:
    Given GPD-Payments service running
    * an authorization on entity "77777777777" for the domain "gpd" related to subscription key "A" is added in the database

  Scenario: Enrolled EC - Get valued list by existing domain
    When the client execute a call for the domain "gpd"
    Then the client receives status code 200
    Then the client receives an object with enrolled creditor institutions

  Scenario: Enrolled EC - Get an error for a non-existing domain
    When the client execute a call for the domain "xxx"
    Then the client receives status code 400

  Scenario: Enrolled EC - Get station list by existing domain and creditor institution
    When the client execute a call for the domain "gpd" for entity "77777777777"
    Then the client receives status code 200
    Then the client receives an object with enrolled stations for creditor institution

  Scenario: Enrolled EC - Get an error for a non-existing domain in station retrieve
    When the client execute a call for the domain "xxx" for entity "77777777777"
    Then the client receives status code 400

  Scenario: Enrolled EC - Get an error for a non-enrolled creditor institution in station retrieve
    When the client execute a call for the domain "gpd" for entity "xxx"
    Then the client receives status code 404

  Scenario: Enrolled EC - Get an error for non-enrolled station for enrolled creditor institution
    When the client execute a call for the domain "gpd" for entity "12345123450"
    Then the client receives status code 404