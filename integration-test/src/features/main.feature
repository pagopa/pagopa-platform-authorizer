Feature: All about authorizer workflow

  Background:
    Given GPD-Payments service running
    * an authorization on entity "77777777777" for the domain "gpd" related to subscription key "A" is added in the database

  Scenario: Authorizer - Not-authorized subscription key
    When the client execute a call for entity "77777777777" with subscription key "B"
    Then the client receives status code 401

  Scenario: Authorizer - Not-authorized entity
    When the client execute a call for entity "66666666666" with subscription key "A"
    Then the client receives status code 401

  Scenario: Authorizer - Complete Happy path
    When the client execute a call for entity "77777777777" with subscription key "A"
    Then the client receives status code different from 401

