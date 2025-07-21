import * as core from "@actions/core";
import * as github from "@actions/github";

try {
  const kotlin = require("action-logic/dist/actionLogic.js")
  const file = core.getInput("file");
  const summary = kotlin.ActionLogic.buildSummary(file)
  core.setOutput("summary", summary);
} catch (error) {
  core.setFailed(error.message);
}