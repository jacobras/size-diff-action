import * as core from "@actions/core";
import * as github from "@actions/github";

try {
  // `who-to-greet` input defined in action metadata file
  const file = core.getInput("file");
  core.info(`Filename: ${file}!`);

  core.setOutput("summary", file);
} catch (error) {
  core.setFailed(error.message);
}