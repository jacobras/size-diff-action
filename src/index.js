import * as core from "@actions/core";
import * as github from "@actions/github";
import kotlin from "../action-logic/build/compileSync/js/main/productionExecutable/kotlin/actionLogic.js"

try {
  const file = core.getInput("file");
  const summary = kotlin.ActionLogic.buildSummary(file)
  core.setOutput("summary", summary);
} catch (error) {
  core.setFailed(error.message);
}