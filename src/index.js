import * as core from "@actions/core";
import * as github from "@actions/github";
import kotlin from "../action-logic/build/compileSync/js/main/productionExecutable/kotlin/actionLogic.js"

try {
  const path = core.getInput("path");
  const summary = kotlin.ActionLogic.buildSummary(path)
  core.setOutput("summary", summary);
} catch (error) {
  core.setFailed(error.message);
}