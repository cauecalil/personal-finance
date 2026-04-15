#!/usr/bin/env node

import {readFileSync, writeFileSync} from "node:fs";
import {resolve} from "node:path";

const version = process.argv[2];

if (!version) {
  console.error("Usage: node .github/scripts/sync-release-version.mjs <version>");
  process.exit(1);
}

const semverPattern = /^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?(?:\+[0-9A-Za-z.-]+)?$/;
if (!semverPattern.test(version)) {
  console.error(`Invalid semver value: ${version}`);
  process.exit(1);
}

const frontendPackagePath = resolve("frontend/package.json");
const backendPomPath = resolve("backend/pom.xml");

const packageJson = JSON.parse(readFileSync(frontendPackagePath, "utf8"));
packageJson.version = version;
writeFileSync(frontendPackagePath, `${JSON.stringify(packageJson, null, "\t")}\n`, "utf8");

const pomContent = readFileSync(backendPomPath, "utf8");
const projectVersionPattern = /(<artifactId>personal-finance-backend<\/artifactId>\s*<version>)([^<]+)(<\/version>)/m;

if (!projectVersionPattern.test(pomContent)) {
  console.error("Could not locate project version in backend/pom.xml.");
  process.exit(1);
}

const updatedPomContent = pomContent.replace(projectVersionPattern, `$1${version}$3`);
writeFileSync(backendPomPath, updatedPomContent, "utf8");

console.log(`Synced backend and frontend version to ${version}.`);