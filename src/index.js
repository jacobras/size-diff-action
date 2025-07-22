import * as core from "@actions/core";
import * as github from "@actions/github";
import kotlin from "../action-logic/build/compileSync/js/main/productionExecutable/kotlin/actionLogic.js"

async function run() {
    try {
        await kotlin.ActionLogic.run()
    } catch (error) {
        core.setFailed(error.message);
    }
}

run();