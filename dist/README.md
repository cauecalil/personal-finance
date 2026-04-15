# Distribution packaging

This directory contains helper scripts for desktop distribution artifacts.

## Prerequisites

- JDK 21 with `jpackage` available
- `JAVA_HOME` configured
- Built backend JAR in `backend/target`

## Usage

### Windows (portable)

```powershell
./dist/scripts/package-windows.ps1 -AppVersion 1.0.0
```

### macOS (portable)

```bash
./dist/scripts/package-macos.sh 1.0.0
```

### Linux (portable)

```bash
./dist/scripts/package-linux.sh 1.0.0
```

Output artifacts:

- Windows app-image folder in `dist/output/Personal Finance`
- Windows zip archive in `dist/output/Personal-Finance-<version>-windows-portable.zip`
- macOS app bundle in `dist/output/Personal Finance.app`
- macOS zip archive in `dist/output/Personal-Finance-<version>-macos-portable.zip`
- Linux app-image folder in `dist/output/Personal Finance`
- Linux tar.gz archive in `dist/output/Personal-Finance-<version>-linux-portable.tar.gz`
